# IsleBridge - Hướng dẫn Kiểm thử Tích hợp Thực tế (Integration Test Guide)

Tài liệu này hướng dẫn các bước kiểm thử toàn diện IsleBridge trên thiết bị thật và máy ảo Android.

---

## 1. Danh sách kịch bản kiểm thử (Test Scenarios)

### Kịch bản 1: Kiểm thử Native Origin Island trên vivo / iQOO (OriginOS 6)
1. Cài đặt file APK lên điện thoại vivo/iQOO chạy OriginOS 6.
2. Mở IsleBridge và cấp quyền **Notification Listener**.
3. Chuyển sang tab **Origin Island** > Bấm nút **Kiểm tra hiển thị** tại mục *Mẫu 1: Tiến trình Nhạc*.
4. **Kết quả mong đợi:** 
   - Viên thuốc trên đảo nốt ruồi camera của OriginOS mở rộng hiển thị bài hát và tiến trình 68%.
   - Chạm vào đảo để mở rộng thẻ đầy đủ.
   - Bấm *Gỡ tất cả thẻ Island (Unmount)*: Đảo thu gọn và trở về trạng thái bình thường.

---

### Kịch bản 2: Kiểm thử Nhận diện OTP & Sao chép 1 chạm
1. Bật tính năng **Nhận diện mã xác thực OTP** và **Tự động sao chép vào Clipboard** trong tab **Mã OTP**.
2. Gửi một tin nhắn SMS thử nghiệm từ máy khác vào điện thoại:
   `"Ma xac thuc OTP cua ban la 739201. Khong chia se cho nguoi khac."`
3. **Kết quả mong đợi:**
   - Đảo Origin Island hiển thị ngay lập tức thẻ ưu tiên khẩn cấp: `OTP: 739201`.
   - Mã `739201` được tự động sao chép vào bộ nhớ đệm (Clipboard).
   - Dán thử vào ứng dụng ghi chú hoặc ô nhập liệu để xác nhận mã đã sao chép.
   - Thử gửi tin nhắn chứa số tiền: `"So du tai khoan tang 500.000 VND"` -> Xác nhận hệ thống bỏ qua, không nhận nhầm thành OTP.

---

### Kịch bản 3: Kiểm thử Giám sát Pin Tai nghe Bluetooth
1. Cấp quyền **Thiết bị ở gần (Bluetooth)** cho IsleBridge.
2. Mở hộp và kết nối tai nghe Bluetooth (ví dụ: vivo TWS, AirPods, Sony, Galaxy Buds).
3. **Kết quả mong đợi:**
   - IsleBridge nhận diện sự kiện `ACTION_ACL_CONNECTED` và hiển thị thẻ tai nghe trên Origin Island.
   - Tab Bluetooth hiển thị tên thiết bị và mức pin (L, R, Case) nếu tai nghe hỗ trợ gửi thông số qua Bluetooth API. Nếu không hỗ trợ, hiển thị trung thực `Unknown` (không bịa phần trăm ảo).
   - Ngắt kết nối tai nghe -> Thẻ tự động unmount khỏi đảo.

---

### Kịch bản 4: Kiểm thử Quick Settings Control Tile
1. Vuốt thanh thông báo xuống 2 lần để mở rộng Quick Settings.
2. Chọn biểu tượng cây bút để sửa đổi các ô điều khiển (Tiles).
3. Tìm ô **IsleBridge Island** và kéo lên danh sách ô hiển thị chính.
4. Chạm vào ô:
   - Nếu dịch vụ đang chạy: Chuyển sang trạng thái Bật (Active).
   - Chạm lần nữa: Tạm dừng chuyển tiếp (Inactive).

---

### Kịch bản 5: Kiểm thử Bảo mật Ứng dụng Ngân hàng
1. Mở một ứng dụng ngân hàng (ví dụ: Vietcombank, Techcombank, BIDV, MoMo, ZaloPay).
2. Tạo một thông báo giao dịch giả lập.
3. **Kết quả mong đợi:**
   - IsleBridge hoàn toàn bỏ qua thông báo từ package ngân hàng, không quét nội dung, không gửi lên đảo, đảm bảo tuân thủ nguyên tắc an toàn dữ liệu tài chính của người dùng.

