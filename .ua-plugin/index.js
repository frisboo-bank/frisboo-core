/**
 * @understand-anything/core — minimal stub
 *
 * Provides the functions needed by the understand-anything skill scripts
 * when the full plugin monorepo is not installed.
 */

import { existsSync, readFileSync, mkdirSync, writeFileSync } from 'node:fs';
import { join, resolve } from 'node:path';
import { createHash } from 'node:crypto';

// ---------------------------------------------------------------------------
// Default ignore patterns (mirrors core's built-in defaults)
// ---------------------------------------------------------------------------

const DEFAULT_IGNORE_PATTERNS = [
  'node_modules',
  '.git',
  '.svn',
  '.hg',
  '__pycache__',
  '*.pyc',
  '.DS_Store',
  'Thumbs.db',
  '.ua',
  '.understand-anything',
];

// ---------------------------------------------------------------------------
// resolveUaDir — mirrors core's resolveUaDir
// Legacy .understand-anything/ wins if present; otherwise .ua/
// ---------------------------------------------------------------------------

export function resolveUaDir(projectRoot) {
  const legacy = join(projectRoot, '.understand-anything');
  if (existsSync(legacy)) return legacy;
  return join(projectRoot, '.ua');
}

// ---------------------------------------------------------------------------
// Simple .gitignore-style matcher
// ---------------------------------------------------------------------------

function compilePatterns(patterns, root) {
  const compiled = [];
  for (const raw of patterns) {
    const p = raw.trim();
    if (!p || p.startsWith('#')) continue;
    const negated = p.startsWith('!');
    const pattern = negated ? p.slice(1).trim() : p;
    if (!pattern) continue;
    compiled.push({ negated, pattern, isDir: pattern.endsWith('/') });
  }
  return compiled;
}

function matchPattern(rel, pattern, isDir) {
  // Normalize: remove leading ./
  let r = rel.replace(/^\.\//, '');
  let p = pattern.replace(/^\.\//, '');
  if (isDir) p = p.replace(/\/$/, '');

  // Exact match
  if (r === p) return true;

  // Directory prefix match (e.g., "node_modules" matches "node_modules/foo")
  if (r.startsWith(p + '/')) return true;

  // Glob match
  // Simple ** support
  if (p.includes('**')) {
    const parts = p.split('**');
    if (parts.length === 2) {
      const [prefix, suffix] = parts;
      const pre = prefix.replace(/\/$/, '');
      const suf = suffix.replace(/^\//, '');
      if (pre && !r.startsWith(pre)) return false;
      if (suf) {
        const rest = pre ? r.slice(pre.length).replace(/^\//, '') : r;
        if (suf.endsWith('*')) {
          return rest.startsWith(suf.slice(0, -1));
        }
        return rest === suf || rest.startsWith(suf + '/');
      }
      return true;
    }
  }

  // Simple * glob
  if (p.includes('*')) {
    const regex = new RegExp(
      '^' + p.replace(/[.+^${}()|[\]\\]/g, '\\$&').replace(/\*/g, '.*') + '$'
    );
    return regex.test(r);
  }

  // Basename match (e.g., "Dockerfile" matches any file named Dockerfile)
  if (!p.includes('/')) {
    const basename = r.split('/').pop();
    return basename === p;
  }

  return false;
}

function isIgnoredBy(rel, compiled, root) {
  let ignored = false;
  for (const { negated, pattern, isDir } of compiled) {
    if (matchPattern(rel, pattern, isDir)) {
      ignored = !negated;
    }
  }
  return ignored;
}

// ---------------------------------------------------------------------------
// createIgnoreFilter — returns { isIgnored(rel) }
// ---------------------------------------------------------------------------

export function createIgnoreFilter(projectRoot, excludePatterns = []) {
  // Read user .understandignore if present
  const uaDir = resolveUaDir(projectRoot);
  const userPatterns = [];
  const ignorePath = join(uaDir, '.understandignore');
  if (existsSync(ignorePath)) {
    try {
      const content = readFileSync(ignorePath, 'utf-8');
      userPatterns.push(...content.split('\n'));
    } catch { /* ignore read errors */ }
  }

  // Also check project-root .understandignore
  const rootIgnore = join(projectRoot, '.understandignore');
  if (existsSync(rootIgnore)) {
    try {
      const content = readFileSync(rootIgnore, 'utf-8');
      userPatterns.push(...content.split('\n'));
    } catch { /* ignore read errors */ }
  }

  const allPatterns = [...DEFAULT_IGNORE_PATTERNS, ...userPatterns, ...excludePatterns];
  const compiled = compilePatterns(allPatterns, projectRoot);

  return {
    isIgnored(rel) {
      return isIgnoredBy(rel, compiled, projectRoot);
    },
  };
}

// ---------------------------------------------------------------------------
// generateStarterIgnoreFile — generates starter .understandignore content
// ---------------------------------------------------------------------------

export function generateStarterIgnoreFile(projectRoot) {
  const lines = [
    '# Understand Anything — ignore patterns',
    '# Add patterns here to exclude files from analysis.',
    '',
    '# Dependencies',
    'node_modules/',
    '',
    '# Version control',
    '.git/',
    '',
    '# Build output',
    'dist/',
    'build/',
    '',
    '# OS files',
    '.DS_Store',
    'Thumbs.db',
    '',
  ];
  return lines.join('\n');
}

// ---------------------------------------------------------------------------
// Tree-sitter stubs — the full plugin uses web-tree-sitter WASM grammars.
// When tree-sitter is unavailable, scripts fall back to empty symbol maps
// and content-only fingerprints, which is acceptable for analysis.
// ---------------------------------------------------------------------------

export class TreeSitterPlugin {
  constructor(configs) {
    this.configs = configs || [];
  }
  async init() { /* no-op */ }
  parseFile(_filePath, _content) { return null; }
}

export class PluginRegistry {
  constructor() {
    this.plugins = [];
  }
  register(plugin) {
    this.plugins.push(plugin);
  }
}

export const builtinLanguageConfigs = [];

export function registerAllParsers(_registry) {
  // no-op — no tree-sitter parsers registered
}

// ---------------------------------------------------------------------------
// Fingerprint stubs — content-only fingerprints when tree-sitter is unavailable
// ---------------------------------------------------------------------------

export function buildFingerprintStore(projectRoot, filePaths, _registry, gitCommitHash, _options) {
  const store = {
    projectRoot,
    gitCommitHash,
    files: {},
  };

  for (const fp of filePaths) {
    const absPath = join(projectRoot, fp);
    let content;
    try {
      content = readFileSync(absPath);
    } catch {
      continue;
    }
    const hash = createHash('sha256');
    hash.update(content);
    const contentHash = hash.digest('hex');

    store.files[fp] = {
      path: fp,
      contentHash,
      structuralHash: contentHash, // content-only fallback
      sizeBytes: content.length,
    };
  }

  return store;
}

export function saveFingerprints(projectRoot, store) {
  const uaDir = resolveUaDir(projectRoot);
  if (!existsSync(uaDir)) {
    mkdirSync(uaDir, { recursive: true });
  }
  const outPath = join(uaDir, 'fingerprints.json');
  writeFileSync(outPath, JSON.stringify(store, null, 2));
}