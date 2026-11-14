# IsleBridge – Origin Island Android System Companion

<p align="center">
  <strong>Ứng dụng đồng hành hệ thống thông minh, tích hợp trực tiếp với giao thức Origin Island / Super Island trên vivo và iQOO.</strong>
</p>

---

## 🌟 Tổng quan dự án

**IsleBridge** (`com.tuan.islebridge`) là ứng dụng Android hiện đại, cung cấp khả năng tích hợp gốc với giao diện thông báo đảo động **Origin Island / Super Island** trên điện thoại thông minh vivo và iQOO (ưu tiên **OriginOS 6**, thử nghiệm **OriginOS 7**) và cung cấp chế độ dự phòng thông minh (Heads-up Notifications & Custom Floating Overlay) cho các thiết bị Android khác.

### 🛡️ Nguyên tắc cốt lõi:
1. **KHÔNG Root:** Toàn bộ tính năng hoạt động an toàn không đòi hỏi can thiệp hệ thống sâu.
2. **KHÔNG Accessibility Service:** Ứng dụng không sử dụng dịch vụ hỗ trợ tiếp cận nhằm đảm bảo an toàn tuyệt đối, không chiếm dụng tài nguyên hoặc đọc trộm màn hình.
3. **KHÔNG giả mạo Package:** Sử dụng duy nhất Application ID hợp lệ `com.tuan.islebridge`, không mạo danh AutoNavi/AMap (`com.autonavi.minimap`).
4. **Bảo mật Tài chính & Ngân hàng:** Tự động phát hiện và loại trừ tất cả ứng dụng ngân hàng, ví điện tử khỏi quá trình đọc nội dung thông báo.
5. **Dữ liệu pin trung thực:** Chỉ hiển thị phần trăm pin tai nghe khi thiết bị thực sự truyền dữ liệu qua Bluetooth API chuẩn, tuyệt đối không bịa số liệu giả mạo.

---

## 🚀 Các tính năng chính

1. **Tích hợp Native Origin Island:**
   - Triển khai độc lập bộ tạo payload chuẩn theo giao thức Super Island của OriginOS (`notification.superx.*`).
   - Hỗ trợ đầy đủ các mẫu thẻ: Thẻ thông tin hai dòng (Base Info), Thẻ tiến trình (Progress bar cho tải file và phát nhạc), Viên thuốc thu nhỏ (Capsule layout).
   - Thao tác Unmount thẻ độc lập mà không hủy thông báo gốc của ứng dụng thứ ba.

2. **Nhận diện mã xác thực OTP thông minh & Sao chép 1-chạm:**
   - Quét mã OTP 4–8 chữ số từ thông báo SMS và ứng dụng chat.
   - Loại bỏ triệt để các trường hợp dương tính giả (năm, số điện thoại, giá tiền, chuỗi số lặp lại).
   - Tự động sao chép vào bộ nhớ đệm (Clipboard) và tự động hủy sau 30 giây để bảo vệ quyền riêng tư.
   - Lịch sử mã OTP chỉ lưu trên RAM tạm thời (Volatile memory), không bao giờ ghi xuống bộ nhớ flash hay gửi qua Internet.

3. **Giám sát pin tai nghe Bluetooth:**
   - Tự động nhận diện khi tai nghe Bluetooth kết nối hoặc ngắt kết nối.
   - Đọc mức pin tổng quan, tai nghe trái (L), tai nghe phải (R) và hộp sạc (Case).
   - Cung cấp hai chế độ hiển thị: Chế độ A (Luôn hiển thị khi kết nối) và Chế độ B (Tự động ẩn sau thời gian quy định).

4. **Chuyển tiếp thông báo ứng dụng (Notification Mirroring):**
   - Danh sách chọn lọc ứng dụng cho phép / chặn chuyển tiếp.
   - Hỗ trợ trích xuất thanh tiến trình trực tiếp từ thông báo đang phát nhạc, tải file, hẹn giờ.
   - Cam kết: `KEEP ORIGINAL NOTIFICATIONS = ON` (không bao giờ can thiệp hoặc xóa thông báo gốc).

5. **Trung tâm quản lý quyền hạn tập trung:**
   - Bảng điều khiển trực quan theo dõi trạng thái các quyền: Notification Listener, Overlay, Bluetooth, Bỏ qua tối ưu hóa pin, Tự khởi chạy trên OriginOS iManager.

6. **Tích hợp Shizuku tùy chọn (Optional Shizuku Enhancement):**
   - Không bắt buộc. Nếu người dùng bật Shizuku, ứng dụng hỗ trợ tự động đưa IsleBridge vào danh sách trắng Doze Whitelist và chẩn đoán AppOps nâng cao.

7. **Ô cài đặt nhanh (Quick Settings Tile):**
   - Tile `IsleBridge Island` trên bảng điều khiển trung tâm giúp bật/tắt dịch vụ tức thì mà không cần giữ app trong danh sách ứng dụng gần đây.

8. **Cập nhật OTA an toàn qua GitHub Releases:**
   - Tự động kiểm tra bản cập nhật mới từ kho lưu trữ GitHub.
   - Tải file APK kèm hiển thị phần trăm tiến trình.
   - Kiểm tra tính toàn vẹn bằng mã băm SHA-256 và xác thực chứng chỉ chữ ký số (Certificate Fingerprint) trước khi khởi chạy trình cài đặt hệ thống.

9. **Giao diện người dùng hiện đại với Material 3:**
   - Jetpack Compose, hỗ trợ Dynamic Color trên Android 12+, chuyển đổi mượt mà giữa Chế độ Sáng, Tối và Theo hệ thống.

---

## 📂 Cấu trúc dự án (Architecture)

```text
app-android-dynamic-island/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/tuan/islebridge/
│       │   │   ├── IsleBridgeApp.kt
│       │   │   ├── MainActivity.kt
│       │   │   ├── core/
│       │   │   │   ├── common/           # Hằng số, tương thích thiết bị, Resource wrapper
│       │   │   │   ├── datastore/        # Jetpack DataStore Preferences
│       │   │   │   ├── originisland/     # Super Island payload builder, Priority Queue, Adapter
│       │   │   │   ├── notifications/    # NotificationListenerService, Parser, AppFilterManager
│       │   │   │   ├── bluetooth/        # Giám sát tai nghe & đọc pin Bluetooth
│       │   │   │   ├── otp/              # Bộ nhận diện mã OTP regex & Clipboard helper
│       │   │   │   ├── privacy/          # Giám sát AppOps Camera & Mic
│       │   │   │   ├── shizuku/          # Trình quản lý Shizuku & Binder listeners
│       │   │   │   ├── updater/          # Cập nhật GitHub OTA & Kiểm tra SHA-256
│       │   │   │   ├── overlay/          # Chế độ dự phòng Dynamic Island Overlay
│       │   │   │   └── tile/             # Quick Settings TileService
│       │   │   └── ui/
│       │   │       ├── theme/            # Material 3 Color, Type, Theme
│       │   │       ├── navigation/       # Điều hướng 8 màn hình chính
│       │   │       ├── components/       # Các thẻ trạng thái, preview card, hàng phân quyền
│       │   │       └── screens/          # Home, Island, Bluetooth, OTP, Apps, Permissions, Privacy, Settings
│       │   └── res/                  # Tài nguyên drawable, values, mipmap, xml
│       └── test/                     # Unit Tests bao phủ đầy đủ các module
├── docs/                             # Tài liệu hướng dẫn chuyên sâu
│   ├── DEVICE_COMPATIBILITY.md       # Tài liệu tương thích chi tiết OriginOS 6/7
│   ├── PERMISSION_GUIDE.md           # Hướng dẫn thiết lập phân quyền
│   ├── SHIZUKU_GUIDE.md              # Hướng dẫn ghép nối và ủy quyền Shizuku
│   ├── OTA_UPDATE_GUIDE.md           # Hướng dẫn phát hành cập nhật GitHub OTA
│   └── INTEGRATION_TEST_GUIDE.md     # Hướng dẫn kiểm thử thực tế trên máy thật
├── gradle/
│   └── libs.versions.toml            # Gradle Version Catalog
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🛠️ Hướng dẫn Biên dịch (Build Instructions)

### Yêu cầu môi trường:
- JDK 17 trở lên.
- Android SDK Platform 35, Build-Tools 35.0.0.
- Android Studio Ladybug hoặc mới hơn (khuyên dùng).

### Các lệnh biên dịch bằng Gradle:

1. **Biên dịch bản Debug APK:**
   - Trên Windows:
     ```cmd
     gradlew.bat assembleDebug
     ```
   - Trên Linux / macOS:
     ```bash
     ./gradlew assembleDebug
     ```
   *File APK xuất tại:* `app/build/outputs/apk/debug/app-debug.apk`

2. **Chạy toàn bộ Unit Tests:**
   ```bash
   ./gradlew test
   ```

3. **Biên dịch bản Release APK:**
   ```bash
   ./gradlew assembleRelease
   ```
   *File APK xuất tại:* `app/build/outputs/apk/release/app-release-unsigned.apk`

---

## 📖 Tài liệu đính kèm
- Chi tiết tương thích OriginOS 6 và 7: [docs/DEVICE_COMPATIBILITY.md](docs/DEVICE_COMPATIBILITY.md)
- Hướng dẫn cấp quyền hệ thống: [docs/PERMISSION_GUIDE.md](docs/PERMISSION_GUIDE.md)
- Hướng dẫn kết nối Shizuku không cần root: [docs/SHIZUKU_GUIDE.md](docs/SHIZUKU_GUIDE.md)
- Hướng dẫn phát hành OTA qua GitHub: [docs/OTA_UPDATE_GUIDE.md](docs/OTA_UPDATE_GUIDE.md)
- Hướng dẫn kiểm thử thực tế: [docs/INTEGRATION_TEST_GUIDE.md](docs/INTEGRATION_TEST_GUIDE.md)

