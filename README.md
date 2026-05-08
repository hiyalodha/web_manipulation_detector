# 🛡️ Web Manipulation Detection System
Detects dark patterns on websites in real-time. Highlights manipulative text like fake urgency, false scarcity, and social pressure tactics directly on the page.
Built with a **Chrome Extension** (JavaScript) + **Spring Boot REST API** (Java).

---

## How it works
1. Extension scans all visible text on the page
2. Sends it to the backend (`POST /analyze`)
3. Backend runs 30+ regex rules and returns detected pattern types
4. Extension highlights matching elements in red with a tooltip

---

## Detection Categories
| Category | Example |
| Urgency | "Hurry!", "Act now", "Limited time" |
| Scarcity | "Only 3 left", "Almost gone" |
| Pressure | "Selling fast", "47 people watching" |

---

## Tech Stack
Java 23 · Spring Boot 3.3 · JavaScript · Chrome Manifest V3 · JUnit 5

---

## How to Use

1. Start the backend — `mvn spring-boot:run`
2. Open Chrome → `chrome://extensions`
3. Enable **Developer mode** (top right toggle)
4. Click **Load unpacked** → select the `extension/` folder
5. Pin the extension to your toolbar
6. Visit any website → click the shield icon → click **Scan Page**
