# IsleBridge — GitHub APK Release & CI/CD Guide

This guide explains how to build, test, sign, and publish **IsleBridge** APKs automatically via GitHub Actions, and how to download and install them directly onto vivo and iQOO devices running OriginOS.

---

## 1. Overview of the CI/CD Pipeline

The project uses GitHub Actions (`.github/workflows/android-release.yml`) with two distinct distribution pathways:

| Trigger Event | Target | Produced Output | Distribution Location |
| :--- | :--- | :--- | :--- |
| **Push to `main` branch** | Development / QA | `app-debug.apk` | **GitHub Actions Artifacts** (Private, 14-day retention) |
| **Push a Git tag (`v*`)** | Production Release | `IsleBridge-vX.Y.Z.apk` | **GitHub Releases** (Public permanent asset + SHA-256) |

---

## 2. Difference Between Actions Artifacts and GitHub Releases

It is important to understand when to use an Actions Artifact versus a GitHub Release:

- **GitHub Actions Artifacts (Debug Builds):**
  - Built automatically whenever you push regular commits to the `main` branch.
  - Signed with the standard Android debug keystore.
  - Intended solely for developer testing and regression verification.
  - Automatically expire after 14 days and are only accessible by logged-in repository collaborators.
- **GitHub Releases (Production Releases):**
  - Created only when a formal version tag (e.g., `v1.0.0`) is pushed.
  - Signed with your stable, persistent production keystore.
  - Publicly accessible permanently directly from mobile browsers.
  - Enables smooth in-place updates over previous versions without data loss.

---

## 3. How to Configure GitHub Actions Secrets for Production Signing

To produce installable production releases, GitHub Actions requires access to your signing credentials. **Never commit keystore files or passwords directly to git.**

### Required GitHub Secrets

| Secret Name | Description | Example Format |
| :--- | :--- | :--- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded string of your `.jks` or `.keystore` file | `MIIKqwIBAzCCCm...` (single line) |
| `ANDROID_KEYSTORE_PASSWORD` | Password used to encrypt the keystore | `YourStrongKeystorePass123` |
| `ANDROID_KEY_ALIAS` | Alias name of the key inside the keystore | `islebridge_key` |
| `ANDROID_KEY_PASSWORD` | Password protecting the specific key alias | `YourStrongKeyPass123` |

### Step-by-Step Keystore Generation & Secret Setup

#### Step 3.1: Generate a Production Keystore (if you do not have one yet)
Run the following command in PowerShell or Bash (replace passwords and alias as desired):

```bash
keytool -genkeypair -v \
  -keystore islebridge-release.jks \
  -alias islebridge_release \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass "YourStrongKeystorePass123" \
  -keypass "YourStrongKeyPass123" \
  -dname "CN=IsleBridge, OU=Android, O=IsleBridge, L=Hanoi, C=VN"
```

> **IMPORTANT:** Back up `islebridge-release.jks` in a secure password manager or offline storage. If you lose this keystore, you will **not** be able to deliver updates to existing users without requiring them to uninstall the app and lose their data.

#### Step 3.2: Convert the Keystore to a Base64 String
- **On Windows (PowerShell):**
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("islebridge-release.jks")) | Set-Clipboard
  ```
  *(The Base64 string is now copied to your clipboard).*

- **On macOS / Linux:**
  ```bash
  base64 -w 0 islebridge-release.jks | pbcopy   # macOS: pbcopy, Linux: xclip -sel clip
  ```

#### Step 3.3: Add the Secrets to GitHub
1. Open your repository on GitHub: `https://github.com/blct15102004/dynamic_island_origin`
2. Navigate to **Settings** > **Secrets and variables** > **Actions**.
3. Under **Repository secrets**, click **New repository secret**.
4. Add the four secrets:
   - `ANDROID_KEYSTORE_BASE64` (paste the Base64 string)
   - `ANDROID_KEYSTORE_PASSWORD`
   - `ANDROID_KEY_ALIAS`
   - `ANDROID_KEY_PASSWORD`

---

## 4. How to Push Code to GitHub

When you make modifications to the application:

```bash
# 1. Stage all changes
git add .

# 2. Commit your changes
git commit -m "feat: Add new notification parser for delivery apps"

# 3. Push to main
git push origin main
```

Pushing to `main` triggers the **Build Debug APK** workflow.

---

## 5. How to Create and Push a Version Tag (Publish a Release)

When you are ready to publish an official version:

1. Update `versionCode` and `versionName` in `app/build.gradle.kts`:
   - `versionCode` must always be strictly greater than the previous release (e.g., `1` -> `2`).
   - `versionName` should follow Semantic Versioning (e.g., `"1.0.0"` -> `"1.0.1"`).
2. Commit and push the version bump to `main`.
3. Create and push the Git tag:

```bash
git add .
git commit -m "chore: Prepare release v1.0.0"
git push origin main

git tag v1.0.0
git push origin v1.0.0
```

> **WARNING:**
> - **Never recreate or overwrite an existing tag.** Once a tag (e.g., `v1.0.0`) is published, Android package managers and users expect immutable binaries.
> - If you need to issue a bug fix or new build, increment the tag to a higher version (e.g., `v1.0.1` or `v1.1.0`).

---

## 6. How to Monitor GitHub Actions

1. In your browser, open: `https://github.com/blct15102004/dynamic_island_origin/actions`
2. You will see the running workflow:
   - **For pushes to `main`:** The `Build Debug APK` job will compile and upload the artifact.
   - **For tag pushes (`v*`):** The `Build & Publish Release APK` job will decode your keystore, compile the release APK, verify its signature, generate release notes, and publish the GitHub Release.
3. Click on the active workflow run to view real-time build logs.

---

## 7. Where to Find Debug Build Artifacts

1. Go to the **Actions** tab on GitHub.
2. Select the completed workflow run from the `main` branch.
3. Scroll down to the **Artifacts** section at the bottom of the summary page.
4. Click on **`IsleBridge-debug-apk`** to download the zip file containing `app-debug.apk`.

---

## 8. How to Download the APK on an Android Phone (vivo / iQOO)

1. On your phone (vivo/iQOO), open Chrome, Vivo Browser, or any mobile browser.
2. Visit the repository releases page:
   `https://github.com/blct15102004/dynamic_island_origin/releases`
3. Tap on the latest release (e.g., **IsleBridge v1.0.0**).
4. Under the **Assets** heading, tap the APK file:
   **`IsleBridge-v1.0.0.apk`**
5. If the browser displays a warning stating *"File might be harmful"*, tap **Download anyway**.

---

## 9. How to Install the APK on vivo / iQOO (OriginOS 6 / OriginOS 7)

GitHub cannot install the APK directly into Android system memory; Android's package installer must be granted permission to perform the installation:

1. Once the download finishes, tap **Open** from the download notification or locate `IsleBridge-v1.0.0.apk` in your **Files** (Tệp) app under **Downloads**.
2. Tap the APK file to open Android's Package Installer.
3. **If this is your first time sideloading from this browser/file manager:**
   - Android will display a security prompt: *"For your security, your phone is not allowed to install unknown apps from this source."*
   - Tap **Settings** (Cài đặt).
   - Toggle the switch for **Allow from this source** (Cho phép từ nguồn này) to **ON**.
   - Press the Back button to return to the installer.
4. Tap **Install** (Cài đặt).
5. Once installation finishes, tap **Open** (Mở).
6. Complete the built-in **IsleBridge Permission Activation Dialog** to enable:
   - Notification Listener Service
   - Post Notifications (Android 13+)
   - Bluetooth Connect
   - Overlay permission (for Fallback Island)

---

## 10. Updating an Existing Installation Without Losing App Data

To allow in-place updates without uninstalling the existing app:

1. **Identical Package Identity:** The `applicationId` remains fixed as `com.tuan.islebridge`.
2. **Monotonically Increasing Version:** Every new release must have a higher `versionCode` in `app/build.gradle.kts`.
3. **Identical Signing Certificate:** Both the installed APK and the new APK must be signed by the **exact same keystore**.

When you download a newer release APK (e.g., `IsleBridge-v1.0.1.apk`) and open it on your phone:
- Android will recognize the existing installation and prompt: **"Do you want to update this app? Existing data will not be lost."**
- Tap **Update**.
- All your settings, allowed/blocked applications, and preferences remain fully intact!

---

## 11. Troubleshooting Common Failures

### 1. Workflow fails with: `"Release signing secrets are missing"`
- **Cause:** You pushed a `v*` tag before setting up the 4 GitHub repository secrets.
- **Fix:** Navigate to **Settings > Secrets and variables > Actions** in your GitHub repo and add `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`. Delete the failed tag or push a new tag once secrets are configured.

### 2. Workflow fails with: `"Failed to decode keystore or file is empty"`
- **Cause:** `ANDROID_KEYSTORE_BASE64` contains whitespace, line breaks, or invalid Base64 data.
- **Fix:** Regenerate the Base64 string as a single continuous line (without line wrapping) and update the secret.

### 3. Android phone shows: `"App not installed as package appears to be corrupt"` or `"Signature mismatch"`
- **Cause:** The phone has a version installed that was signed with a different key (for example, a debug build installed previously).
- **Fix:** Uninstall the debug build from your phone once, then install the official production release signed with your permanent keystore. All future production releases will update smoothly.

### 4. Local Build output paths
For reference, local builds on your workstation generate APKs at:
- **Debug:** `app/build/outputs/apk/debug/app-debug.apk`
- **Release:** `app/build/outputs/apk/release/app-release.apk`

