# IMPLEMENTATION AUDIT REPORT — IsleBridge
**Project:** IsleBridge (OriginOS Companion for vivo & iQOO)  
**Date:** 2026-11-14  
**Author:** Antigravity Senior Android Systems & Engineering Team  
**Audit Scope:** Full codebase audit, root-cause diagnosis, architecture review, and roadmap for Phase 2–7.

---

## 1. Project Specifications & Environment

- **Module Structure:** Single-module application (`:app`).
- **Application ID:** `com.tuan.islebridge`
- **Version Code:** `1`
- **Version Name:** `1.0.0`
- **Compile SDK:** 34 (Android 14)
- **Target SDK:** 34 (Android 14)
- **Min SDK:** 26 (Android 8.0 Oreo)
- **Kotlin Version:** `2.0.21` (with Compose Compiler 2.0.21 plugin)
- **Android Gradle Plugin (AGP):** `8.7.3`
- **Gradle Wrapper:** `8.10.2`
- **JDK Target:** Java 17 (`sourceCompatibility` & `targetCompatibility` = 17, `jvmTarget` = "17")
- **UI Framework:** Jetpack Compose with Material 3 (`androidx.compose.material3:1.3.1`, Compose BOM `2024.12.01`)
- **Navigation:** Jetpack Compose Navigation (`2.8.5`)
- **Asynchronous Architecture:** Kotlin Coroutines (`1.9.0`) & StateFlow / Flow
- **Persistence:** Jetpack DataStore Preferences (`1.1.1`)
- **Privileged System Integration:** Shizuku API (`dev.rikka.shizuku:api:13.1.5` & `provider:13.1.5`)
- **Remote Repository:** `https://github.com/blct15102004/dynamic_island_origin`

---

## 2. Architecture & Subsystem Inspection

### 2.1 Island, Overlay, Floating-Window & Notification Implementation
- **Current State:**
  - `OriginIslandAdapter.kt`: Prepares SuperX notification extras and routes notifications via `SuperIslandPayloadBuilder`. However, it also branches to `OverlayService.showCard(context, card)`.
  - `OverlayService.kt`: A foreground service (`android:foregroundServiceType="specialUse"`) that attaches an `IslandOverlayView` directly to the `WindowManager` with `LayoutParams.TYPE_APPLICATION_OVERLAY`.
  - `IslandOverlayView.kt`: Custom Android `FrameLayout` drawing a dark pill shape with progress bar and text over the top status bar / camera punch hole.
  - `AndroidManifest.xml`: Declares `android.permission.SYSTEM_ALERT_WINDOW`, `FOREGROUND_SERVICE_SPECIAL_USE`, and property `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`.
- **Finding against Master Prompt:** The prompt explicitly states in Section 3.1:
  > *"Remove the application's custom Dynamic Island implementation... IsleBridge must not draw a custom floating Island, a fake notification pill, a replacement Island interface, or overlay-based fallback... Do not use SYSTEM_ALERT_WINDOW for custom Island rendering."*
- **Action Required:** Completely remove `IslandOverlayView`, decommission `OverlayService`, remove `SYSTEM_ALERT_WINDOW` and `FOREGROUND_SERVICE_SPECIAL_USE` declarations, and clean up fallback toggle settings across the UI and preferences.

### 2.2 Device & OriginOS Detection Logic
- **Current State:**
  - File: `CompatibilityDetector.kt`
  - Evaluates standard Android properties: `Build.MANUFACTURER`, `Build.BRAND`, `Build.MODEL`, `Build.VERSION.SDK_INT`, `Build.VERSION.RELEASE`.
  - Attempts to read `ro.vivo.os.version`, `ro.vivo.os.build.display.id`, `ro.vivo.product.version` via `Runtime.getRuntime().exec("getprop ...")`.
  - Hardcoded heuristic: Checks if the raw string contains `"OriginOS 6"` or `"OriginOS 7"`.
- **Root Cause of `vivo (Legacy Fallback)` on vivo V2403A (Android 16):**
  1. `Runtime.getRuntime().exec("getprop")` can be blocked or return blank on modern Android versions due to SELinux untrusted app domain restrictions.
  2. Alternative vivo properties (`ro.vivo.os.name`, `ro.vivo.rom`, `ro.vivo.rom.version`, `ro.build.version.vivo`, `ro.vivo.product.model`) are not queried.
  3. If the property string format on Android 16 does not literally match `"OriginOS 6"` or `"OriginOS 7"` (e.g. build id, internal numbering, or development preview), the status immediately degrades to `VIVO_LEGACY_UNSUPPORTED` with a negative badge.
  4. There is no concept of **Confidence Level** (`Confirmed`, `Estimated`, `Unknown`).
- **Action Required:** Expand property probing (standard Android reflection on `android.os.SystemProperties`, multi-property fallback list, Shizuku `getprop` fallback when authorized), implement 3-tier confidence level, and avoid labeling a device as incompatible simply because its exact OriginOS version tag isn't hardcoded.

### 2.3 Shizuku Integration & Authorization Flow
- **Current State:**
  - `ShizukuManager.kt`: Registers sticky binder listeners and permission result listeners.
  - `requestPermission()` method:
    ```kotlin
    fun requestPermission(requestCode: Int = 1001) {
        try {
            if (Shizuku.pingBinder() && !statusFlow.value.hasPermission) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (_: Throwable) {}
    }
    ```
- **Root Cause of Inactive / Broken Authorization Button:**
  1. When Shizuku is not installed or when the Shizuku background service is not running, `Shizuku.pingBinder()` returns `false`.
  2. The function silently does nothing (`catch (_: Throwable) {}` and `if (Shizuku.pingBinder())` check).
  3. No Intent is launched to open the Shizuku app, no toast is displayed, and no user guidance is provided.
  4. In `PermissionsScreen.kt`, the Shizuku card only has a single generic "Ủy quyền" button which triggers this silent no-op.
- **Action Required:** 
  - Comprehensive status resolution: `NOT_INSTALLED`, `SERVICE_STOPPED`, `UNAUTHORIZED`, `AUTHORIZED`.
  - Contextual button actions: If not installed -> Open official download / GitHub link; if service stopped -> Launch Shizuku app with Wireless Debugging instructions; if service running -> Trigger `Shizuku.requestPermission()`.
  - Step-by-step in-app setup guide dialog with copyable adb commands and wireless debugging pairing steps.

### 2.4 Test & Unmount Functions
- **Current State (`IslandScreen.kt` & `OriginIslandAdapter.kt`):**
  - Test buttons trigger `IslandPreviewCard.onTestClick`, which sends a notification with `SuperIslandPayload` and then calls `OverlayService.showCard(context, card)`.
  - The notification is marked `.setOngoing(true)`! Because of `.setOngoing(true)`, the notification is persistent and cannot be dismissed.
  - The custom overlay pill remains on screen indefinitely until manually killed.
  - `onClearAllCards` clears `priorityQueue.clear()`, but does not systematically cancel the posted Android notification IDs or stop active foreground overlays.
- **Action Required:**
  - Standardize test event: Trigger exactly ONE test event targeting the native Origin Island.
  - Make test events strictly transient: Auto-dismiss after 3 seconds (`CoroutineScope(Dispatchers.Main).launch { delay(3000); adapter.unmountCard(testId) }`).
  - Never use `.setOngoing(true)` for test events.
  - "Unmount All Test Island Cards": Cancels all IsleBridge-owned test notification channels/IDs, clears local test state, and displays an accurate diagnostic Toast or Snackbar ("Đã gỡ tất cả thẻ thử nghiệm" or "Không có thẻ thử nghiệm nào đang hoạt động").

### 2.5 Installed-App Management & Forwarding Logic
- **Current State (`ApplicationsScreen.kt` & `MainActivity.kt`):**
  - Apps are queried via `packageManager.getInstalledApplications()`.
  - `AppItem` data class only holds `packageName`, `appName`, `isSystemApp`, `isFinancial`.
  - UI only displays plain text names — **NO app icon is rendered** (Section 8 violation).
  - No differentiation between Origin Player whitelist, Atom Island, and SuperX compatibility.
  - Filters are limited to just a "Show system apps" checkbox.
- **Action Required:**
  - Load real app icons asynchronously using `PackageManager.getApplicationIcon(packageName)` converted to Compose `ImageBitmap`.
  - Add search bar with real-time debounce.
  - Add filter chips: "Tất cả", "Đã bật", "Bị chặn", "Ứng dụng media".
  - Display granular integration indicators per app:
    * Origin Player Media Support (e.g. QQ Music, NetEase, Kugou, Spotify, Apple Music).
    * Native Atom Island / SuperX compatibility status.
    * Real-time enabled/disabled toggle with immediate preference persistence.

### 2.6 UI / UX Layout & Responsive Failures
- **Root Cause of Broken Badges & Overlapping in Screenshot:**
  - `StatusCard.kt` uses a `Row` containing another `Row(title)` and `Surface(statusText)`. Neither has `Modifier.weight(1f)` or constraint boundaries.
  - When title is "Khả năng tương thích thiết bị" and badge is `vivo (Legacy Fallback)`, the badge width is crushed to ~20dp, wrapping letters vertically into `v-i-v-o...`.
  - `HomeScreen.kt` groups 3 feature cards (Bluetooth, OTP, Privacy) in a single horizontal `Row(Modifier.weight(1f))`. On standard phone portrait widths (360dp–390dp), each card is squeezed below minimum touch and readability thresholds.
  - Bottom Navigation bar icons and labels lack proper window insets handling and padding, causing labels to collide or feature cards to clip at the bottom.
- **Action Required:** Redesign responsive layouts using adaptive grid/wrapping columns, ensure badges never wrap vertically, separate title and status badge when width < 380dp, apply Material 3 spacing tokens (4dp, 8dp, 12dp, 16dp, 24dp), and enforce 48dp touch targets.

---

## 3. Detailed Issue Audit & Resolution Matrix

| Issue ID | Existing Behavior | Relevant Files | Root Cause | Proposed Change | Expected Result | Physical Device Required? |
|---|---|---|---|---|---|---|
| **ISSUE-01** | UI Badge crushed into narrow vertical text; overlapping header | `StatusCard.kt`, `HomeScreen.kt` | Unconstrained `Row` children; title consumes full horizontal width, crushing badge `Surface` | Wrap badge in flexible column/row layout, use `IntrinsicSize` or vertical stack if width is constrained | Badge text remains on single horizontal line with proper padding; zero overlap | No (validated in Compose preview & layout tests) |
| **ISSUE-02** | Custom floating Island / overlay drawn over camera hole | `IslandOverlayView.kt`, `OverlayService.kt`, `OriginIslandAdapter.kt`, `AndroidManifest.xml` | Custom overlay code attempts to simulate Dynamic Island via `SYSTEM_ALERT_WINDOW` | Remove `IslandOverlayView`, decommission `OverlayService`, remove `SYSTEM_ALERT_WINDOW` permission | IsleBridge ceases drawing any custom UI overlay; relies 100% on native Origin Island | No |
| **ISSUE-03** | Phone detected as `vivo (Legacy Fallback)` despite being modern vivo (V2403A) | `CompatibilityDetector.kt` | Only checks for literal string `"OriginOS 6"` / `"OriginOS 7"`; fails on getprop restriction or varied build tags | Multi-tier property inspection, reflection on `SystemProperties`, confidence levels (`Confirmed`, `Estimated`, `Unknown`) | Modern vivo devices are recognized with explicit confidence score; no premature legacy fallback | Yes (for final vivo build tag confirmation) |
| **ISSUE-04** | Shizuku authorization button produces no response | `ShizukuManager.kt`, `PermissionsScreen.kt`, `MainActivity.kt` | `requestPermission()` fails silently if service is not running (`pingBinder() == false`) | Contextual handling: check install status, launch Shizuku activity with guide if service stopped, request permission if binder active | Button opens Shizuku app or permission prompt with immediate user feedback | Yes (to test live Shizuku binder) |
| **ISSUE-05** | Test Island card stays visible forever | `IslandScreen.kt`, `OriginIslandAdapter.kt`, `SuperIslandPayloadBuilder.kt` | Test notification is `.setOngoing(true)` and launches persistent overlay with no auto-dismissal | Strip `.setOngoing(true)` on test events; add 3-second auto-unmount timer | Test notification appears on native Island and vanishes automatically after 3s | Yes (for native Island animation) |
| **ISSUE-06** | "Unmount All" button produces no visible result | `IslandScreen.kt`, `OriginIslandAdapter.kt` | Only clears queue list; does not cancel notification IDs or provide user feedback | Cancel all IsleBridge notification IDs, send explicit unmount payload, display Snackbar/Toast | Active test notifications disappear immediately with clear confirmation | No |
| **ISSUE-07** | Applications list shows no icons and generic text | `ApplicationsScreen.kt`, `MainActivity.kt` | `AppItem` lacks drawable icon; UI uses plain text rows | Asynchronously load and cache `applicationInfo.loadIcon(pm)` as `ImageBitmap`; add search and filter chips | Real app icons displayed crisply with fast scrolling and search | No |
| **ISSUE-08** | Bottom feature cards (Bluetooth, OTP, Privacy) clipped/cramped | `HomeScreen.kt` | Fixed 3-column `Row` regardless of screen width | Responsive flow layout or 2-column grid adaptive to screen width | All card titles, icons, and status indicators visible without truncation | No |

---

## 4. Phase Execution Plan

1. **Phase 1 — Audit (Complete):** Documented root causes and established technical roadmap.
2. **Phase 2 — UI Repair:** Refactor `StatusCard`, `HomeScreen`, `NavigationBar`, and components to be 100% responsive, beautiful, and compliant with Material 3.
3. **Phase 3 — Device Detection:** Overhaul `CompatibilityDetector` with confidence levels and generate `DEVICE_COMPATIBILITY_REPORT.md`.
4. **Phase 4 — Shizuku Subsystem:** Implement robust status machine, setup guide, and responsive permission requests.
5. **Phase 5 — Native Origin Island Integration & Overlay Removal:** Purge custom Island views, remove overlay permissions, refine SuperX protocol builder.
6. **Phase 6 — Test, Unmount, App Management & Permissions:** Auto-expiring tests, real app icon loading, granular compatibility badges.
7. **Phase 7 — Verification & Build:** Comprehensive review, create `FIX_REPORT.md`, compile APK.

