// ============================================================
// background.js — Web Manipulation Detector
// Service worker. Updates the extension badge icon count.
// ============================================================

chrome.runtime.onMessage.addListener((msg, sender) => {
  if (msg.type === "UPDATE_BADGE") {
    const count = msg.count || 0;
    const tabId = sender.tab?.id;

    if (!tabId) return;

    // Show count on the extension icon
    chrome.action.setBadgeText({
      text: count > 0 ? String(count) : "",
      tabId,
    });

    // Red badge background
    chrome.action.setBadgeBackgroundColor({
      color: count > 0 ? "#ff3b3b" : "#666666",
      tabId,
    });
  }
});
