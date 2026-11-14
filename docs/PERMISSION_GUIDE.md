# IsleBridge - Hướng dẫn Thiết lập Quyền hạn (Permission Setup Guide)

## 1. Nguyên tắc An toàn & Quyền riêng tư

IsleBridge tuân thủ nghiêm ngặt các quy định bảo mật:
- **KHÔNG yêu cầu Accessibility Service (Hỗ trợ tiếp cận).**
- **KHÔNG yêu cầu quyền ROOT.**
- **KHÔNG yêu cầu quyền đọc SMS riêng tư (`READ_SMS`, `RECEIVE_SMS`).** Việc đọc OTP hoàn toàn dựa trên thông báo đẩy từ ứng dụng tin nhắn mà người dùng cho phép.
- **Tự động loại trừ ứng dụng ngân hàng & tài chính:** Ngăn chặn tuyệt đối việc đọc thông báo biến động số dư hoặc giao dịch ngân hàng.

---

## 2. Danh sách các quyền hệ thống và cách cấp

### 1. Quyền Truy cập Thông báo (Notification Listener Access)
- **Mục đích:** Đọc thông báo từ các ứng dụng được người dùng chọn để hiển thị lên Origin Island và trích xuất mã OTP.
- **Cách cấp:**
  1. Mở màn hình **Quyền hạn** trong IsleBridge.
  2. Bấm **Cấp quyền** tại mục *Truy cập thông báo*.
  3. Hệ thống mở trang *Quyền truy cập thông báo* trong Cài đặt Android.
  4. Tìm **IsleBridge** và gạt bật công tắc Cho phép.

### 2. Quyền Thiết bị ở gần (Bluetooth Connect / Scan)
- **Mục đích:** Giám sát tai nghe Bluetooth kết nối và đọc phần trăm pin tai nghe / hộp sạc.
- **Cách cấp:** Bấm nút cấp quyền trong ứng dụng và chọn *Cho phép* trên hộp thoại chuẩn của Android.

### 3. Quyền Hiển thị đè lên ứng dụng khác (Overlay Permission)
- **Mục đích:** Dành cho chế độ Fallback mô phỏng viên thuốc Dynamic Island nổi trên màn hình nếu không sử dụng Origin Island gốc.
- **Cách cấp:** Bấm *Cấp quyền* tại mục *Hiển thị đè lên ứng dụng khác*, tìm IsleBridge và bật quyền.

### 4. Bỏ qua Tối ưu hóa Pin (Doze Exemption)
- **Mục đích:** Giữ cho tiến trình theo dõi thông báo và Bluetooth chạy mượt mà dưới nền mà không bị hệ thống tắt khi tắt màn hình.
- **Cách cấp:** Chọn *Cấp quyền* tại mục *Bỏ qua tối ưu hóa pin* và chọn *Cho phép*.

### 5. Cấu hình Tự khởi chạy trên vivo / iQOO (OriginOS)
- **Cách thực hiện trên OriginOS:**
  1. Mở ứng dụng **iManager (Quản lý điện thoại)**.
  2. Vào mục **Quản lý quyền (App Manager) > Quản lý tự khởi chạy (Autostart)**.
  3. Tìm **IsleBridge** và bật cho phép tự khởi chạy cùng hệ thống.
  4. Vào **Cài đặt > Pin > Quản lý pin dưới nền > IsleBridge**, chọn **Cho phép sử dụng pin nền cao**.

