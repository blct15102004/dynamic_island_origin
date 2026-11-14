# DEVICE COMPATIBILITY REPORT — IsleBridge
**Target Devices:** vivo & iQOO Smartphones  
**Target Operating Systems:** OriginOS 6 & OriginOS 7  
**Date:** 2026-11-14  
**Author:** Antigravity Senior Android Systems Engineering Team  

---

## 1. Executive Summary

This report establishes the hardware and platform detection architecture for **IsleBridge**, specifically addressing the issue observed on modern vivo hardware (such as `vivo V2403A` on Android 16/15) where the application previously misclassified the phone as `vivo (Legacy Fallback)` with degraded functionality.

The detection engine has been redesigned to support standard Android properties first, multi-property vendor inspection via reflection, and an explicit **3-tier confidence classification model**:
- **CONFIRMED:** OriginOS version explicitly identified from verified system properties (`ro.vivo.os.version`, `ro.vivo.os.build.display.id`, etc.).
- **ESTIMATED:** Verified as genuine vivo/iQOO hardware with modern Android runtime (API 34/35/36) where vendor properties are restricted to untrusted sandbox callers.
- **UNKNOWN:** Non-vivo/non-iQOO hardware or indeterminate platform.

---

## 2. Root Cause Analysis: The `vivo V2403A (Android 16)` Misclassification

In previous builds, the user's test device (`vivo V2403A` running Android 16) exhibited two critical failures:
1. **Property Access Failure:** Modern Android releases (starting with Android 14 and heavily enforced in Android 15/16) restrict third-party apps from executing `/system/bin/getprop` via `Runtime.getRuntime().exec()`. The command returned empty or was blocked by SELinux untrusted app domain policies.
2. **Hardcoded String Check:** The previous implementation strictly tested `osVersionRaw.contains("OriginOS 6")` or `"OriginOS 7"`. If the vendor ROM string was named differently (e.g. preview build tags, internal numbering `PD2403_...`, or `OriginOS Ocean / 4 / 5`), the parser immediately defaulted to `VIVO_LEGACY_UNSUPPORTED`.
3. **UI Container Collapse:** The resulting badge string `vivo (Legacy Fallback)` was shoved into an unconstrained `Row` in `StatusCard.kt`, crushing the badge width down to ~20dp and splitting the letters vertically into single-character columns that overlapped the card title.

### Solution Applied:
- Added multi-property fallback (`ro.vivo.os.version`, `ro.vivo.os.build.display.id`, `ro.vivo.os.name`, `ro.vivo.rom.version`, `ro.build.version.vivo`, `ro.vivo.product.version`).
- Prioritized reflection on hidden platform API `android.os.SystemProperties.get(String)` before CLI process invocation.
- Added heuristic estimation: when `Build.MANUFACTURER` / `Build.BRAND` is `vivo` or `iQOO` on Android 14+ (SDK >= 34), it is designated as `NATIVE_ORIGIN_OS_SUPPORTED` with `ESTIMATED` confidence, granting native Super Island integration immediately rather than disabling it.

---

## 3. Platform Detection Matrix & Property Reference

| Property Key | Source | Typical Value | Purpose |
|---|---|---|---|
| `Build.MANUFACTURER` | Android SDK | `vivo` / `iQOO` | Primary manufacturer validation |
| `Build.BRAND` | Android SDK | `vivo` / `iQOO` | Brand identity validation |
| `Build.MODEL` | Android SDK | `V2403A`, `V2324A`, etc. | Device commercial model number |
| `Build.DEVICE` | Android SDK | Codename (e.g. `PD2403`) | Internal hardware board codename |
| `Build.PRODUCT` | Android SDK | Product line identifier | Target build configuration |
| `Build.VERSION.SDK_INT` | Android SDK | `34`, `35`, `36` | Android platform API level |
| `Build.VERSION.RELEASE` | Android SDK | `"14"`, `"15"`, `"16"` | User-facing Android version |
| `Build.DISPLAY` | Android SDK | Build number string | Displays vendor ROM name if exposed |
| `Build.FINGERPRINT` | Android SDK | `vivo/...:user/release-keys` | Signed firmware fingerprint |
| `ro.vivo.os.version` | vivo Property | `OriginOS 6.0`, `OriginOS 7.0` | Primary OriginOS marketing release |
| `ro.vivo.os.build.display.id` | vivo Property | `OriginOS 6.0 PD2403_A_...` | Full OriginOS build display identifier |
| `ro.vivo.os.name` | vivo Property | `OriginOS` | OS family naming (`OriginOS` vs `Funtouch`) |
| `ro.vivo.rom.version` | vivo Property | Rom version identifier | Internal vivo ROM release track |
| `ro.build.version.vivo` | vivo Property | Sub-version number | vivo base layer version |

---

## 4. Origin Player vs. Atom Island / SuperX Differentiation

As specified in Master Prompt Section 3.3, media whitelist and Island integration are **distinct subsystems**:

### 4.1 Origin Player Media Recognition
- **Scope:** Allows media playback metadata and control widgets to appear inside OriginOS's built-in "Origin Player" component.
- **Whitelist Investigation Candidates:**
  - `com.kugou.android` (Kugou Music)
  - `com.tencent.qqmusic` (QQ Music)
  - `com.netease.cloudmusic` (NetEase Cloud Music)
  - `com.ximalaya.ting.android` (Ximalaya FM)
  - `com.lrts.me` (Lan Ren Ting Shu)
  - `bubei.tingshu` (Bubei Ting Shu)
  - `cn.kuwo.player` (Kuwo Music)
  - `com.spotify.music` (Spotify)
  - `com.apple.android.music` (Apple Music)
- **Status:** Evaluated and flagged per-app in IsleBridge App Manager as `Origin Player Compatible`.

### 4.2 Native Atom Island / Super Island Integration
- **Scope:** Real-time capsule pill and expanded cards rendered natively by `com.vivo.upslide` / `com.vivo.superx` around the punch-hole camera.
- **Protocol:** Standard Android notification with `notification.superx.*` extras:
  - `notification.superx.operation` (0 = Mount, 1 = Unmount, 2 = Update)
  - `notification.superx.showNotify` (boolean)
  - `notification.superx.template` (1 = Base Info, 2 = Progress, 3 = Capsule Only, 6 = Media Progress)
  - `notification.superx.scene` (String scene identifier)
  - `notification.superx.capsule` (Bundle containing `text`, `subText`, `iconResId`)
  - `notification.superx.island` (boolean = true)
- **Status:** Supported on confirmed OriginOS builds. No custom floating window or replacement overlay is permitted.

---

## 5. Shizuku Optional Privileged Diagnostics

When Shizuku is connected and authorized:
1. `ShizukuShellExecutor` can query `/system/bin/getprop` directly with shell UID (`2000`), bypassing SELinux untrusted third-party app domain boundaries.
2. Can inspect `dumpsys deviceidle whitelist` to verify background Doze exemption without root.
3. Can query `cmd appops` for diagnostic verification of active Camera/Mic usage indicators.
4. **Safety Guarantee:** IsleBridge does NOT modify system APKs, overwrite system lists blindly, or impersonate other package IDs.

