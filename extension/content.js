// ============================================================
// content.js — Web Manipulation Detector
// Scans DOM, calls backend, highlights, saves to DB.
// ============================================================

const BACKEND_URL = "http://localhost:8080/analyze";
const SAVE_URL    = "http://localhost:8080/scan-log";

// ── State ────────────────────────────────────────────────────
let isEnabled         = true;
let detectedCount     = 0;
let detectedFlags     = [];
let highlightedNodes  = [];
let scanDone          = false;
let collectedPatterns = [];

// ── Entry point ──────────────────────────────────────────────
chrome.storage.local.get(["enabled"], (result) => {
  isEnabled = result.enabled !== false;
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
  if (msg.type === "GET_RESULT") {
    sendResponse({ done: scanDone, count: detectedCount, flags: detectedFlags });
  }
  return true;
});

// ── Main scan ────────────────────────────────────────────────
async function runScan() {
  scanDone          = false;
  detectedCount     = 0;
  detectedFlags     = [];
  collectedPatterns = [];

  const startTime = Date.now();
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
        collectedPatterns.push({
          patternText: text.substring(0, 500),
          category:    result.flags[0],
          score:       result.score,
          elementTag:  node.parentElement?.tagName?.toLowerCase() || "unknown",
          regexUsed:   result.regexUsed || "",
          description: getCategoryDescription(result.flags[0]),
          weight:      getCategoryWeight(result.flags[0]),
          confidence:  result.confidence || (result.score / 100),
        });
      }
    } catch {
      const result = localFallbackCheck(text);
      if (result.flags.length > 0) {
        highlightNode(node, result);
        detectedCount++;
        for (const f of result.flags) {
          if (!detectedFlags.includes(f)) detectedFlags.push(f);
        }
        collectedPatterns.push({
          patternText: text.substring(0, 500),
          category:    result.flags[0],
          score:       result.score,
          elementTag:  node.parentElement?.tagName?.toLowerCase() || "unknown",
          regexUsed:   "",
          description: getCategoryDescription(result.flags[0]),
          weight:      getCategoryWeight(result.flags[0]),
          confidence:  result.score / 100,
        });
      }
    }
  }

  scanDone = true;
  const duration = Date.now() - startTime;

  if (detectedCount > 0) saveScanToDB(duration);

  chrome.runtime.sendMessage({ type: "UPDATE_BADGE", count: detectedCount });
}

// ── Save to DB ────────────────────────────────────────────────
async function saveScanToDB(durationMs) {
  try {
    const payload = {
      url:                window.location.href.substring(0, 2048),
      domain:             window.location.hostname,
      pageTitle:          document.title.substring(0, 512),
      totalPatternsFound: detectedCount,
      scanDurationMs:     durationMs,
      browser:            "Chrome",
      pageScore:          Math.min(detectedCount * 15, 100),
      categoriesFound:    detectedFlags.join(","),
      patterns:           collectedPatterns,
    };

    await fetch(SAVE_URL, {
      method:  "POST",
      headers: { "Content-Type": "application/json" },
      body:    JSON.stringify(payload),
    });
  } catch {
    // Silently fail — DB save is non-critical
  }
}

// ── Backend call ─────────────────────────────────────────────
async function analyzeText(text) {
  const response = await fetch(BACKEND_URL, {
    method:  "POST",
    headers: { "Content-Type": "application/json" },
    body:    JSON.stringify({ text }),
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
    urgency:  [/hurry[\s!?]/i, /act\s+now/i, /limited\s+time/i,
               /offer\s+ends/i, /expires?\s+soon/i, /don'?t\s+miss/i,
               /last\s+chance/i, /today\s+only/i, /flash\s+sale/i],
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

// ── Helpers ───────────────────────────────────────────────────
function getCategoryDescription(category) {
  const desc = {
    urgency:  "Time-based pressure tactic to rush user decisions",
    scarcity: "Quantity-based pressure to create fear of missing out",
    pressure: "Social proof manipulation to influence user behavior",
  };
  return desc[category] || "Manipulative UI pattern";
}

function getCategoryWeight(category) {
  return { urgency: 35, scarcity: 30, pressure: 25 }[category] || 25;
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
  highlightedNodes  = [];
  detectedCount     = 0;
  detectedFlags     = [];
  collectedPatterns = [];
  scanDone          = false;
}
