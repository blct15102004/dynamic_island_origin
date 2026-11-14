# IsleBridge - Tài liệu Tương thích Thiết bị (Device Compatibility)

## 1. Tổng quan kiến trúc tương thích

IsleBridge (`com.tuan.islebridge`) được thiết kế ưu tiên tích hợp sâu với giao thức **Origin Island / Super Island** trên hệ điều hành **OriginOS** của vivo và iQOO, đồng thời cung cấp chế độ dự phòng thông minh (Native Notifications + Custom Overlay) cho các thiết bị Android khác.

---

## 2. Bảng ma trận hỗ trợ phần cứng & ROM

| Hệ điều hành / Dòng máy | Mức độ hỗ trợ | Trạng thái tích hợp | Phương thức hiển thị |
| :--- | :--- | :--- | :--- |
| **vivo / iQOO OriginOS 6** | **Chính thức (Primary)** | Hỗ trợ đầy đủ | Native Super Island Protocol (`notification.superx.*`) |
| **vivo / iQOO OriginOS 7** | **Thử nghiệm (Experimental)** | Đang xác thực | Native Protocol với fallback kiểm tra lỗi |
| **vivo FuntouchOS / OriginOS cũ** | Không hỗ trợ Native Island | Fallback | Android Heads-up Notifications + Floating Overlay |
| **Samsung, Xiaomi, Pixel, OPPO...**| Tương thích mở rộng | Fallback | Android Heads-up Notifications + Floating Overlay |

---

## 3. Chi tiết Giao thức Origin Island Super Island Protocol

IsleBridge triển khai độc lập payload builder chuẩn theo giao thức Super Island của OriginOS:

### Các khóa Extras quan trọng (`notification.superx.*`):
- `notification.superx.operation`: Thao tác thẻ:
  - `0`: Mount (Hiển thị thẻ lên đảo)
  - `1`: Unmount (Gỡ bỏ thẻ khỏi đảo mà không hủy thông báo gốc)
  - `2`: Update (Cập nhật nội dung thẻ đã có)
- `notification.superx.showNotify`: Bật/tắt hiển thị kèm thông báo tiêu chuẩn.
- `notification.superx.template`: Mã giao diện thẻ:
  - `1`: Base Info Template (Thẻ thông tin hai dòng)
  - `2`: Progress Template (Thẻ tiến trình tải xuống / media)
  - `3`: Capsule Only Template (Viên thuốc thu nhỏ trên đục lỗ camera)
  - `6`: Media Progress Template
- `notification.superx.scene`: Ngữ cảnh hiển thị (`bluetooth`, `otp`, `media`, `progress`, `privacy`, `app_mirror`).
- `notification.superx.capsule`: Bundle chứa thông số hiển thị ở trạng thái thu nhỏ:
  - `text`: Tiêu đề ngắn gọn
  - `subText`: Dòng chữ phụ
  - `iconResId`: Icon biểu tượng
- `notification.superx.baseInfos`: Bundle danh sách thông tin chi tiết.
- `notification.superx.shortInfos`: Chuỗi tóm tắt cho giao diện đảo.
- `notification.superx.island`: Boolean flag xác định tính hợp lệ của đảo.

---

## 4. Nguyên tắc định danh Package và An toàn bảo mật

- **Không giả mạo package hệ thống:** Nhiều bản mod bên ngoài giả mạo `com.autonavi.minimap` (AMap) để lọt whitelist OriginOS. IsleBridge sử dụng duy nhất ID hợp lệ: `com.tuan.islebridge`.
- **Nếu firmware chặn package ngoài:** Ứng dụng tự động chuyển đổi sang **Chế độ Overlay Fallback**, đảm bảo an toàn, không xung đột với ứng dụng bản đồ hoặc dịch vụ hệ thống khác.

