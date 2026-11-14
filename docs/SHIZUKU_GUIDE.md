# IsleBridge - Hướng dẫn Tích hợp Shizuku (Shizuku Setup Guide)

## 1. Giới thiệu về Shizuku trong IsleBridge

**Shizuku** là công cụ cho phép các ứng dụng bên thứ ba sử dụng các API hệ thống cấp cao thông qua ADB mà **KHÔNG CẦN ROOT**.

Trong IsleBridge, Shizuku là tính năng **TÙY CHỌN (OPTIONAL)**:
- Nếu **không có Shizuku**: Mọi tính năng cốt lõi (Origin Island, Notification Mirroring, OTP 1-chạm, Bluetooth Earbuds battery) vẫn hoạt động bình thường qua Android API chuẩn.
- Nếu **kích hoạt Shizuku**:
  1. Tự động đưa IsleBridge vào danh sách trắng Doze Whitelist cấp hệ thống (`dumpsys deviceidle whitelist +com.tuan.islebridge`).
  2. Hỗ trợ backend nâng cao để truy vấn chẩn đoán AppOps khi giám sát Camera/Microphone.

---

## 2. Các bước cài đặt và kích hoạt Shizuku (Không cần máy tính)

### Bước 1: Cài đặt Shizuku
- Tải ứng dụng Shizuku chính thức từ Google Play Store hoặc GitHub:
  - https://github.com/RikkaApps/Shizuku/releases

### Bước 2: Kích hoạt Shizuku qua Gỡ lỗi không dây (Wireless Debugging)
1. Kết nối điện thoại vào mạng Wi-Fi.
2. Vào **Cài đặt > Tùy chọn nhà phát triển (Developer options)**.
3. Bật **Gỡ lỗi không dây (Wireless debugging)**.
4. Mở ứng dụng **Shizuku**, chọn **Ghép nối (Pairing)**.
5. Làm theo hướng dẫn trên màn hình để nhập mã ghép nối 6 chữ số từ thông báo hệ thống.
6. Sau khi ghép nối thành công, bấm **Bắt đầu (Start)** trong Shizuku.

### Bước 3: Ủy quyền cho IsleBridge
1. Mở **IsleBridge** > chuyển đến tab **Quyền hạn** hoặc **Cài đặt**.
2. Nhấn nút **Ủy quyền Shizuku**.
3. Hộp thoại xác nhận của Shizuku sẽ xuất hiện > Chọn **Cho phép luôn (Allow all the time)**.
4. IsleBridge sẽ tự động nhận diện và cập nhật trạng thái kết nối!

