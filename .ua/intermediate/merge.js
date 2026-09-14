const fs = require("fs");
const path = require("path");

const INTER = ".ua/intermediate";
const batches = JSON.parse(fs.readFileSync(path.join(INTER, "batches.json"), "utf8"));
const disk = new Set(fs.readFileSync("/tmp/ondisk.txt", "utf8").split("\n").filter(Boolean));

// ---------- 1. Load all batch outputs ----------
const allNodes = [];
const allEdges = [];
const nodeByFile = new Map(); // filePath -> file node id
const nodeById = new Map();   // id -> node

for (let i = 0; i <= 15; i++) {
  const p = path.join(INTER, `batch-${i}.json`);
  let j;
  try { j = JSON.parse(fs.readFileSync(p, "utf8")); }
  catch (e) { console.log("SKIP batch-" + i + " (invalid JSON): " + e.message); continue; }

  for (const n of (j.nodes || [])) {
    if (!n || typeof n.id !== "string") continue;
    // Normalize file node id to canonical "file:<filePath>"
    if (n.type === "file" && n.filePath) n.id = "file:" + n.filePath;
    if (nodeById.has(n.id)) continue; // dedupe by canonical id
    nodeById.set(n.id, n);
    allNodes.push(n);
    if (n.type === "file" && n.filePath) nodeByFile.set(n.filePath, n.id);
  }
  for (const e of (j.edges || [])) {
    if (!e || !e.source || !e.target) continue;
    // Remap edge endpoints to canonical file ids
    if (e.source.startsWith("file:") && nodeById.has(e.source)) { /* ok */ }
    else {
      // Try to find canonical id by matching filePath
      for (const [fp, id] of nodeByFile) {
        if (e.source === "file:" + fp || e.source.endsWith(fp)) e.source = id;
        if (e.target === "file:" + fp || e.target.endsWith(fp)) e.target = id;
      }
    }
    allEdges.push(e);
  }
}

console.log("Loaded: " + allNodes.length + " nodes, " + allEdges.length + " edges");

// ---------- 2. Ensure every on-disk file has a file node ----------
const covered = new Set(nodeByFile.keys());
let addedFileNodes = 0;
for (const fp of disk) {
  if (covered.has(fp)) continue;
  const id = "file:" + fp;
  nodeById.set(id, { id, type: "file", name: path.basename(fp), filePath: fp, summary: "Source file (no analyzer output)", tags: ["unanalyzed"] });
  allNodes.push(nodeById.get(id));
  nodeByFile.set(fp, id);
  addedFileNodes++;
}
console.log("Added " + addedFileNodes + " file nodes for uncovered files");

// ---------- 3. Drop dangling edges ----------
const validIds = new Set(allNodes.map(n => n.id));
const before = allEdges.length;
const edges = [];
const dropped = { dangling: 0, dup: 0 };
const seenEdge = new Set();
for (const e of allEdges) {
  const s = e.source, t = e.target;
  if (!validIds.has(s) || !validIds.has(t)) { dropped.dangling++; continue; }
  const key = s + "|" + e.type + "|" + t;
  if (seenEdge.has(key)) { dropped.dup++; continue; }
  seenEdge.add(key);
  edges.push(e);
}
console.log("Edges: " + before + " -> " + edges.length + " (dropped " + dropped.dangling + " dangling, " + dropped.dup + " dup)");

// ---------- 4. Build tested_by linker ----------
const testFiles = allNodes.filter(n => n.type === "file" && /\/test\//.test(n.filePath || ""));
const importsByFile = new Map();
for (const e of edges) {
  if (e.type !== "imports") continue;
  const sn = nodeById.get(e.source), tn = nodeById.get(e.target);
  if (!sn || !tn || sn.type !== "file" || tn.type !== "file") continue;
  if (!importsByFile.has(e.source)) importsByFile.set(e.source, new Set());
  importsByFile.get(e.source).add(e.target);
}
let testedAdded = 0;
for (const tf of testFiles) {
  const imported = importsByFile.get(tf.id) || new Set();
  for (const prodId of imported) {
    const prod = nodeById.get(prodId);
    if (!prod || prod.type !== "file" || /\/test\//.test(prod.filePath || "")) continue;
    const key = prodId + "|tested_by|" + tf.id;
    if (seenEdge.has(key)) continue;
    seenEdge.add(key);
    edges.push({ source: prodId, target: tf.id, type: "tested_by", weight: 0.8 });
    testedAdded++;
  }
}
console.log("tested_by edges added: " + testedAdded);

// ---------- 5. Add module nodes ----------
const moduleNames = new Set();
for (const n of allNodes) {
  const fp = n.filePath;
  if (!fp) continue;
  const m = fp.match(/^([^/]+)\//);
  if (m && m[1].startsWith("frisboo-")) moduleNames.add(m[1]);
}
const moduleNodes = [];
for (const mn of moduleNames) {
  const id = "module:" + mn;
  if (!nodeById.has(id)) {
    const node = { id, type: "module", name: mn, summary: "Gradle module: " + mn, tags: ["module"] };
    nodeById.set(id, node);
    moduleNodes.push(node);
    allNodes.push(node);
  }
}
let fileModuleEdges = 0;
for (const n of allNodes) {
  if (n.type !== "file" || !n.filePath) continue;
  const m = n.filePath.match(/^([^/]+)\//);
  if (m && m[1].startsWith("frisboo-")) {
    const mid = "module:" + m[1];
    if (nodeById.has(mid)) {
      const key = mid + "|contains|" + n.id;
      if (!seenEdge.has(key)) {
        seenEdge.add(key);
        edges.push({ source: mid, target: n.id, type: "contains", weight: 1.0 });
        fileModuleEdges++;
      }
    }
  }
}
console.log("Module nodes: " + moduleNodes.length + ", file→module edges: " + fileModuleEdges);

// ---------- 6. Write assembled-graph.json ----------
const assembled = {
  meta: {
    project: "frisboo-core",
    generatedAt: new Date().toISOString(),
    totalNodes: allNodes.length,
    totalEdges: edges.length,
    nodeTypes: {},
    edgeTypes: {}
  }
};
for (const n of allNodes) assembled.meta.nodeTypes[n.type] = (assembled.meta.nodeTypes[n.type] || 0) + 1;
for (const e of edges) assembled.meta.edgeTypes[e.type] = (assembled.meta.edgeTypes[e.type] || 0) + 1;
assembled.nodes = allNodes;
assembled.edges = edges;
fs.writeFileSync(path.join(INTER, "assembled-graph.json"), JSON.stringify(assembled));
console.log("Wrote assembled-graph.json: " + allNodes.length + " nodes, " + edges.length + " edges");