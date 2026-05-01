// ============================================================
// popup.js — Web Manipulation Detector
// ============================================================

const BACKEND_URL = "http://localhost:8080/analyze";

const toggleSwitch = document.getElementById("toggleSwitch");
const countDisplay = document.getElementById("countDisplay");
const statusDot    = document.getElementById("statusDot");
const statusText   = document.getElementById("statusText");
const flagsList    = document.getElementById("flagsList");
const scanBtn      = document.getElementById("scanBtn");
const clearBtn     = document.getElementById("clearBtn");

// ── Startup ──────────────────────────────────────────────────
chrome.storage.local.get(["enabled", "flags", "count"], (result) => {
  const enabled = result.enabled !== false; // default ON
  toggleSwitch.checked = enabled;
  const count = result.count || 0;
  countDisplay.textContent = count;
  setCountColor(count);
  renderFlags(result.flags || []);
});

checkBackendHealth();

// ── Toggle ────────────────────────────────────────────────────
toggleSwitch.addEventListener("change", () => {
  const enabled = toggleSwitch.checked;
  chrome.storage.local.set({ enabled });
  sendToContentScript({ type: "TOGGLE", value: enabled });
  if (!enabled) {
    countDisplay.textContent = "0";
    setCountColor(0);
    renderFlags([]);
    chrome.storage.local.set({ count: 0, flags: [] });
  }
});

// ── Scan ─────────────────────────────────────────────────────
scanBtn.addEventListener("click", () => {
  scanBtn.disabled = true;
  scanBtn.textContent = "Scanning…";

  chrome.storage.local.set({ flags: [], count: 0 });
  renderFlags([]);
  countDisplay.textContent = "0";
  setCountColor(0);

  sendToContentScript({ type: "RESCAN" });

  // Poll until content script reports scan is done
  let attempts = 0;
  const poll = setInterval(() => {
    attempts++;
    sendToContentScript({ type: "GET_RESULT" }, (response) => {
      if (response && response.done) {
        clearInterval(poll);
        const count = response.count || 0;
        const flags = response.flags || [];
        countDisplay.textContent = count;
        setCountColor(count);
        renderFlags(flags);
        chrome.storage.local.set({ count, flags });
        scanBtn.disabled = false;
        scanBtn.textContent = "🔍 Scan Page";
      }
    });
    if (attempts > 20) { // give up after 10s
      clearInterval(poll);
      scanBtn.disabled = false;
      scanBtn.textContent = "🔍 Scan Page";
    }
  }, 500);
});

// ── Clear ─────────────────────────────────────────────────────
clearBtn.addEventListener("click", () => {
  sendToContentScript({ type: "CLEAR" });
  countDisplay.textContent = "0";
  setCountColor(0);
  renderFlags([]);
  chrome.storage.local.set({ count: 0, flags: [] });
});

// ── Helpers ───────────────────────────────────────────────────
function setCountColor(count) {
  countDisplay.classList.remove("safe", "medium");
  if (count === 0) countDisplay.classList.add("safe");
  else if (count <= 3) countDisplay.classList.add("medium");
}

function renderFlags(flags) {
  if (!flags || flags.length === 0) {
    flagsList.innerHTML = `<span class="no-flags">None detected yet</span>`;
    return;
  }
  const unique = [...new Set(flags)];
  flagsList.innerHTML = unique
    .map((f) => `<span class="flag-tag ${f}">${f}</span>`)
    .join("");
}

async function checkBackendHealth() {
  try {
    const res = await fetch("http://localhost:8080/health", {
      signal: AbortSignal.timeout(3000),
    });
    setStatus(res.ok, res.ok ? "Backend connected" : "Backend error — using local rules");
  } catch {
    setStatus(false, "Backend offline — using local rules");
  }
}

function setStatus(online, message) {
  statusDot.className = `status-dot${online ? "" : " offline"}`;
  statusText.textContent = message;
}

function sendToContentScript(message, callback) {
  chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
    if (tabs[0]) chrome.tabs.sendMessage(tabs[0].id, message, callback || (() => {}));
  });
}