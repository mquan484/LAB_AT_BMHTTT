# LAB 3: Mã hóa khóa công khai

- **Họ tên:** Phạm Minh Quân
- **MSSV:** 1150080154
- **Lớp:** 11THMT

## Nội dung thực hiện

- **Bài 3.1 - RSA (Console):** Triển khai thuật toán RSA với cơ chế đệm OAEP (SHA-256), hỗ trợ tùy chọn kích thước khóa 2048 hoặc 3072 bit, đo thời gian sinh khóa, kiểm soát giới hạn độ dài bản rõ, mã hóa và giải mã trực tiếp trên cửa sổ Console (`RSADemo.java`).
- **Bài 3.2 - RSA (Swing):** Xây dựng giao diện đồ họa `frm_RSA` cho RSA-OAEP, hỗ trợ sinh cặp khóa, lưu/mở khóa từ tệp (Base64 X.509 / PKCS#8), mã hóa và giải mã chuỗi văn bản hoặc tệp tin.
- **Bài 3.3 - ECC (ECIES):** Triển khai mô hình mã hóa tích hợp đường cong Elliptic secp256r1: thỏa thuận khóa ECDH, phái sinh khóa phiên bằng HKDF-SHA256 và mã hóa dữ liệu xác thực bằng AES-256-GCM kèm cặp khóa tạm (ephemeral key pair).
- **Bài 3.4 - Hệ mã lai RSA + AES:** Xây dựng hệ mã lai kết hợp RSA-2048 (OAEP) để bọc/mã hóa khóa phiên AES 256-bit ngẫu nhiên và AES-256-GCM để mã hóa khối dữ liệu hoặc tệp văn bản lớn với tốc độ cao.
- **Bài 3.5 - Chữ ký số ECDSA:** Hiện thực cơ chế ký số và xác thực tính toàn vẹn thông điệp bằng thuật toán `SHA256withECDSA` trên đường cong secp256r1; hỗ trợ kiểm tra tính hợp lệ của chữ ký với dữ liệu văn bản và tệp tin.
- **Cơ sở dữ liệu & Nhật ký thao tác:** Kết nối Microsoft SQL Server qua JDBC ghi nhận lịch sử các thao tác (`ENCRYPT`, `DECRYPT`, `SIGN`, `VERIFY`) vào bảng `dbo.History` và hiển thị trên giao diện `frm_LichSu`.
- **Giao diện điều hướng:** Tích hợp menu chính `frm_Menu` làm trung tâm kết nối mở toàn bộ các form chức năng và quản lý kết nối cơ sở dữ liệu.

## Môi trường và cách chạy

1. Cài đặt JDK 17 (hoặc mới hơn), Maven và Microsoft SQL Server; có thể mở dự án `LAB3_MaHoaKhoaCongKhai` bằng Apache NetBeans hoặc IDE hỗ trợ Maven.
2. Chạy `03_SQL_SERVER/01_Tao_CSDL.sql` để tạo cơ sở dữ liệu `LAB3_MaHoaKhoaCongKhai` và bảng `dbo.History`. Chạy `02_Tao_TaiKhoan_LAB.sql` nếu cần tạo tài khoản phân quyền mẫu `lab3_user` cho máy thực hành.
3. Mở dự án `LAB3_MaHoaKhoaCongKhai`, cập nhật thư viện Maven (`mssql-jdbc`) và chạy lớp chính `week_03.frm_Menu` (phím `F6`). Riêng bài 3.1, nhấp chuột phải vào `RSADemo.java` chọn **Run File** (`Shift + F6`).
4. Nhập thông tin kết nối tại form **Kết nối SQL Server** (`frm_KetNoiSQL`), bấm **Test Connection** trước khi thực hiện các chức năng mã hóa/ký số để ghi nhận lịch sử. Sử dụng script `03_KiemTra_LichSu.sql` trong SSMS để đối soát dữ liệu.

## Tệp bài làm

- `LAB3_MaHoaKhoaCongKhai/`: mã nguồn ứng dụng Java Swing và Console (Maven Project).
- `03_SQL_SERVER/`: các script tạo database, tài khoản và kiểm tra nhật ký lịch sử.
- [Báo cáo thực hành](LAB3_11THMT_1150080154_PhamMinhQuan.docx).

## Lưu ý

- RSA thuần túy (OAEP-SHA256) chỉ mã hóa được thông điệp có kích thước giới hạn (tối đa 190 byte với khóa 2048-bit và 318 byte với khóa 3072-bit); với dữ liệu hoặc tệp tin lớn, sử dụng hệ mã lai RSA + AES (Bài 3.4) hoặc ECC ECIES (Bài 3.3).
- Chữ ký số ECDSA đảm bảo tính toàn vẹn và xác thực nguồn gốc: khóa riêng (Private Key) dùng để ký và phải được bảo mật tuyệt đối; khóa công khai (Public Key) dùng để xác minh chữ ký.
