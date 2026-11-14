# IsleBridge - Hướng dẫn Cấu hình Cập nhật GitHub OTA (OTA Update Guide)

## 1. Cơ chế Cập nhật OTA An toàn (Security Architecture)

IsleBridge tích hợp trình cập nhật OTA tự động thông qua GitHub Releases API với quy trình kiểm tra bảo mật nghiêm ngặt gồm 5 bước:

```
[1. Kiểm tra Bản phát hành mới] -> HTTPS GitHub Releases API
          ↓
[2. Tải xuống APK]             -> Báo cáo phần trăm tiến trình
          ↓
[3. Xác minh SHA-256]          -> Kiểm tra hash chống can thiệp file
          ↓
[4. Xác minh Chứng chỉ & ID]   -> Kiểm tra Package ID: com.tuan.islebridge & Chữ ký số
          ↓
[5. Khởi chạy PackageInstaller]-> Yêu cầu người dùng xác nhận cài đặt
```

Nếu bất kỳ bước kiểm tra bảo mật nào thất bại, tệp APK tải về sẽ bị **xóa ngay lập tức** khỏi bộ nhớ đệm và từ chối cài đặt.

---

## 2. Cách tạo Bản phát hành trên GitHub (GitHub Release Workflow)

### Bước 1: Build APK Release
Build tệp APK release và ký bằng keystore chính thức của bạn:
```bash
./gradlew assembleRelease
```

### Bước 2: Tạo mã băm SHA-256 cho file APK
Trên máy tính:
- Windows PowerShell:
  ```powershell
  Get-FileHash -Algorithm SHA256 app-release.apk
  ```
- Linux / macOS:
  ```bash
  sha256sum app-release.apk > app-release.apk.sha256
  ```

### Bước 3: Tạo Release trên Repository GitHub
1. Vào trang GitHub Repository của dự án: `https://github.com/{owner}/{repo}/releases/new`
2. Đặt **Tag version**: ví dụ `v1.0.1` (IsleBridge sẽ phân tích mã version này để so sánh với versionCode hiện tại).
3. Đặt **Release title**: ví dụ `IsleBridge v1.0.1 - Cập nhật tối ưu hóa Origin Island`.
4. Viết nội dung **Changelog**: Ghi chú các điểm mới hoặc sửa lỗi.
5. Đính kèm các tệp vào mục **Attach binaries**:
   - Tệp APK: `IsleBridge-v1.0.1.apk`
   - Tệp Checksum (tùy chọn): `IsleBridge-v1.0.1.apk.sha256`
6. Bấm **Publish release**.

---

## 3. Tùy biến Cấu hình trong Dự án

Để thay đổi chủ sở hữu hoặc tên repository GitHub, chỉnh sửa trong `app/build.gradle.kts`:
```kotlin
defaultConfig {
    buildConfigField("String", "GITHUB_OWNER", "\"your-github-username\"")
    buildConfigField("String", "GITHUB_REPO", "\"your-repo-name\"")
    buildConfigField("String", "EXPECTED_SIGNING_CERT_SHA256", "\"SHA256_FINGERPRINT_CUA_BAN\"")
}
```
Sau đó ứng dụng sẽ tự động truy vấn cập nhật từ kho lưu trữ của bạn.

