// ============================================================
// content.js — Web Manipulation Detector
// Runs on every page. Scans text, calls backend, highlights.
// ============================================================

const BACKEND_URL = "http://localhost:8080/analyze";

// ── State ────────────────────────────────────────────────────
let isEnabled        = true;
let detectedCount    = 0;
let detectedFlags    = [];
let highlightedNodes = [];
let scanDone         = false;

// ── Entry point ──────────────────────────────────────────────
chrome.storage.local.get(["enabled"], (result) => {
  isEnabled = result.enabled !== false; // default ON
  if (isEnabled) runScan();
});

// ── Message listener ─────────────────────────────────────────
chrome.runtime.onMessage.addListener((msg, _sender, sendResponse) => {

  if (msg.type === "TOGGLE") {
    isEnabled = msg.value;
    if (isEnabled) { runScan(); } else { clearHighlights(); }
    sendResponse({ ok: true });
  }

  if (msg.type === "RESCAN") {
    clearHighlights();
    runScan();
    sendResponse({ ok: true });
  }

  if (msg.type === "CLEAR") {
    clearHighlights();
    sendResponse({ ok: true });
  }

  // Popup polls this to get results after a scan
  if (msg.type === "GET_RESULT") {
    sendResponse({ done: scanDone, count: detectedCount, flags: detectedFlags });
  }

  return true; // keep message channel open for async
});

// ── Main scan ────────────────────────────────────────────────
async function runScan() {
  scanDone = false;
  detectedCount = 0;
  detectedFlags = [];

  const textNodes = getAllTextNodes(document.body);

  for (const node of textNodes) {
    const text = node.textContent.trim();
    if (!text || text.length < 3) continue;

    try {
      const result = await analyzeText(text);
      if (result && result.flags && result.flags.length > 0) {
        highlightNode(node, result);
        detectedCount++;
        for (const f of result.flags) {
          if (!detectedFlags.includes(f)) detectedFlags.push(f);
        }
      }
    } catch {
      // Backend unreachable — use local fallback
      const result = localFallbackCheck(text);
      if (result.flags.length > 0) {
        highlightNode(node, result);
        detectedCount++;
        for (const f of result.flags) {
          if (!detectedFlags.includes(f)) detectedFlags.push(f);
        }
      }
    }
  }

  scanDone = true;
  chrome.runtime.sendMessage({ type: "UPDATE_BADGE", count: detectedCount });
}

// ── Backend call ─────────────────────────────────────────────
async function analyzeText(text) {
  const response = await fetch(BACKEND_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ text }),
  });
  if (!response.ok) throw new Error("Backend error");
  return response.json();
}

// ── Client-side fallback ──────────────────────────────────────
function localFallbackCheck(text) {
  const flags = [];
  let score = 0;

  const patterns = {
    scarcity: [/only\s+\d+\s+left/i, /limited\s+stock/i, /almost\s+gone/i,
               /\d+\s+left\s+in\s+stock/i, /low\s+stock/i,
               /\d+\s+(rooms?|seats?|tickets?)\s+left/i],
    urgency:  [/hurry[\s!?]/i, /act\s+now/i, /limited\s+time/i, /offer\s+ends/i,
               /expires?\s+soon/i, /don'?t\s+miss/i, /last\s+chance/i,
               /today\s+only/i, /flash\s+sale/i],
    pressure: [/selling\s+fast/i, /\d+\s+people\s+(are\s+)?watching/i,
               /\d+\s+people\s+(are\s+)?viewing/i, /most\s+popular/i,
               /best\s+seller/i, /trending\s+(now|today)/i],
  };

  const weights = { scarcity: 30, urgency: 35, pressure: 25 };

  for (const [category, regexList] of Object.entries(patterns)) {
    for (const regex of regexList) {
      if (regex.test(text)) {
        if (!flags.includes(category)) flags.push(category);
        score += weights[category];
      }
    }
  }

  return { flags, score: Math.min(score, 100), source: "local" };
}

// ── DOM helpers ──────────────────────────────────────────────
function getAllTextNodes(root) {
  const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
    acceptNode(node) {
      const parent = node.parentElement;
      if (!parent) return NodeFilter.FILTER_REJECT;
      const tag = parent.tagName.toLowerCase();
      if (["script", "style", "noscript", "iframe"].includes(tag))
        return NodeFilter.FILTER_REJECT;
      const style = window.getComputedStyle(parent);
      if (style.display === "none" || style.visibility === "hidden")
        return NodeFilter.FILTER_REJECT;
      return NodeFilter.FILTER_ACCEPT;
    },
  });
  const nodes = [];
  let node;
  while ((node = walker.nextNode())) nodes.push(node);
  return nodes;
}

function highlightNode(textNode, result) {
  const parent = textNode.parentElement;
  if (!parent || parent.dataset.wmdTagged) return;

  parent.dataset.wmdTagged = "true";
  parent.classList.add("wmd-highlight");

  const flagLabel = result.flags.join(", ");
  const raw = result.score ?? result.confidence ?? 0;
  const scoreDisplay = typeof raw === "number" && raw <= 1
    ? Math.round(raw * 100) : Math.round(raw);

  const tooltip = document.createElement("span");
  tooltip.className = "wmd-tooltip";
  tooltip.innerHTML = `
    <span class="wmd-icon">⚠️</span>
    <span class="wmd-text">
      <strong>Dark pattern detected</strong><br>
      Type: <em>${flagLabel}</em><br>
      Score: <strong>${scoreDisplay}/100</strong>
    </span>
  `;

  if (window.getComputedStyle(parent).position === "static")
    parent.style.position = "relative";

  parent.appendChild(tooltip);
  highlightedNodes.push(parent);
}

function clearHighlights() {
  for (const node of highlightedNodes) {
    node.classList.remove("wmd-highlight");
    node.removeAttribute("data-wmd-tagged");
    node.style.position = "";
    const tooltip = node.querySelector(".wmd-tooltip");
    if (tooltip) tooltip.remove();
  }
  highlightedNodes = [];
  detectedCount = 0;
  detectedFlags = [];
  scanDone = false;
}