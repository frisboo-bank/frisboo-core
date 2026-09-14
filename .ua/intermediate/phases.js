const fs = require("fs");
const path = require("path");

const INTER = ".ua/intermediate";
const assembled = JSON.parse(fs.readFileSync(path.join(INTER, "assembled-graph.json"), "utf8"));
const nodes = assembled.nodes;
const edges = assembled.edges;

const nodeById = new Map(nodes.map(n => [n.id, n]));

// ---------- Phase 3: Assemble-Review ----------
// Validate: no dangling edges, every file node has a path, node types in whitelist
const VALID_TYPES = new Set(["file","function","class","module","concept","config","document","service","table","endpoint","pipeline","schema","resource"]);
const VALID_EDGES = new Set(["contains","imports","inherits","implements","tested_by","depends_on","calls","documents","configures","related","extends","exports","uses","similar_to","provisions","triggers","deploys","defines_schema"]);

const issues = [];
for (const n of nodes) {
  if (!VALID_TYPES.has(n.type)) issues.push("Unknown node type: " + n.type + " in " + n.id);
  if (!n.id || typeof n.id !== "string") issues.push("Node missing id");
  if (n.type === "file" && !n.filePath) issues.push("File node missing filePath: " + n.id);
}
for (const e of edges) {
  if (!VALID_EDGES.has(e.type)) issues.push("Unknown edge type: " + e.type);
  if (!nodeById.has(e.source)) issues.push("Edge source missing: " + e.source + " -> " + e.target);
  if (!nodeById.has(e.target)) issues.push("Edge target missing: " + e.target + " (from " + e.source + ")");
}

console.log("=== Phase 3: Assemble-Review ===");
console.log("Nodes: " + nodes.length + " | Edges: " + edges.length);
console.log("Node types: " + JSON.stringify(assembled.meta.nodeTypes));
console.log("Edge types: " + JSON.stringify(assembled.meta.edgeTypes));
console.log("Issues: " + issues.length);
if (issues.length) issues.slice(0, 20).forEach(i => console.log("  - " + i));

// ---------- Phase 4: Architecture ----------
// Derive layer info from module structure + tags
const moduleNodes = nodes.filter(n => n.type === "module");
const fileNodes = nodes.filter(n => n.type === "file");
const classNodes = nodes.filter(n => n.type === "class");
const fnNodes = nodes.filter(n => n.type === "function");

// Build module dependency graph from imports
const moduleDeps = new Map();
for (const e of edges) {
  if (e.type !== "imports") continue;
  const sn = nodeById.get(e.source), tn = nodeById.get(e.target);
  if (!sn || !tn || sn.type !== "file" || tn.type !== "file") continue;
  const sm = (sn.filePath || "").match(/^([^/]+)\//);
  const tm = (tn.filePath || "").match(/^([^/]+)\//);
  if (sm && tm && sm[1] !== tm[1]) {
    if (!moduleDeps.has(sm[1])) moduleDeps.set(sm[1], new Set());
    moduleDeps.get(sm[1]).add(tm[1]);
  }
}

console.log("\n=== Phase 4: Architecture ===");
console.log("Modules: " + moduleNodes.length);
for (const m of moduleNodes) {
  const deps = moduleDeps.get(m.name) || new Set();
  const fileCount = fileNodes.filter(f => (f.filePath || "").startsWith(m.name + "/")).length;
  console.log("  " + m.name + ": " + fileCount + " files, deps: " + [...deps].join(", ") || "none");
}

// ---------- Phase 5: Tour ----------
// Identify key entry points: auto-configuration classes, factory impls, public APIs
const entryPoints = nodes.filter(n =>
  (n.tags || []).includes("autoconfigure") ||
  (n.tags || []).includes("factory") ||
  (n.name || "").endsWith("AutoConfiguration") ||
  (n.name || "").endsWith("FactoryImpl")
);
console.log("\n=== Phase 5: Tour ===");
console.log("Entry points: " + entryPoints.length);
entryPoints.slice(0, 15).forEach(n => console.log("  " + n.id + " (" + n.type + ")"));

// ---------- Phase 6: Validation ----------
// Check: every file has a node, no orphan modules, reasonable edge density
const orphanFiles = fileNodes.filter(f => {
  const hasContains = edges.some(e => e.source === f.id && e.type === "contains");
  const hasModule = edges.some(e => e.source.includes("module:") && e.target === f.id && e.type === "contains");
  return !hasContains && !hasModule;
});
console.log("\n=== Phase 6: Validation ===");
console.log("Files with no contains edges: " + orphanFiles.length + " / " + fileNodes.length);
const avgEdgesPerNode = (edges.length / nodes.length).toFixed(2);
console.log("Avg edges per node: " + avgEdgesPerNode);

// ---------- Phase 7: Save ----------
// Write knowledge-graph.json + meta.json
const outDir = path.join(INTER, ".."); // .ua/
const kg = {
  meta: {
    ...assembled.meta,
    review: { issues, issuesCount: issues.length },
    architecture: {
      modules: moduleNodes.map(m => ({
        name: m.name,
        fileCount: fileNodes.filter(f => (f.filePath || "").startsWith(m.name + "/")).length,
        deps: [...(moduleDeps.get(m.name) || [])]
      }))
    },
    tour: { entryPoints: entryPoints.map(n => ({ id: n.id, name: n.name, type: n.type })) },
    validation: {
      orphanFiles: orphanFiles.length,
      avgEdgesPerNode: parseFloat(avgEdgesPerNode)
    }
  },
  nodes,
  edges
};

fs.writeFileSync(path.join(outDir, "knowledge-graph.json"), JSON.stringify(kg));
fs.writeFileSync(path.join(outDir, "meta.json"), JSON.stringify(kg.meta, null, 2));
console.log("\n=== Phase 7: Save ===");
console.log("Wrote knowledge-graph.json: " + nodes.length + " nodes, " + edges.length + " edges");
console.log("Wrote meta.json");