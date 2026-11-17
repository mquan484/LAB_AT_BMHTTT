# LAB 2: Mã hóa đối xứng hiện đại

- **Họ tên:** Phạm Minh Quân
- **MSSV:** 1150080154
- **Lớp:** 11_ĐH_THMT

## Nội dung thực hiện

- **Bài 2.1 - DES:** Triển khai mã hóa khối DES chế độ CBC, đệm PKCS5Padding, sinh vector khởi tạo IV ngẫu nhiên 8 byte kèm đầu bản mã (Base64). Khóa đúng 8 byte ASCII.
- **Bài 2.2 - TripleDES (3DES):** Triển khai mã khối DESede chế độ CBC, đệm PKCS5Padding, khóa 24 byte ASCII (K1 || K2 || K3), sinh IV ngẫu nhiên 8 byte.
- **Bài 2.3 - AES-256 (GCM) & Quản lý người dùng:**
  - Mã hóa AES-256 chế độ GCM (Galois/Counter Mode) xác thực toàn vẹn bằng tag 16 byte chống giả mạo và phát hiện chỉnh sửa bản mã.
  - Khóa mã hóa AES 256-bit được phái sinh từ mật khẩu nhập vào thông qua hàm PBKDF2-HMAC-SHA256 kết hợp Salt ngẫu nhiên 16 byte (120.000 vòng lặp) và Nonce 12 byte ngẫu nhiên.
  - Tích hợp Đăng ký / Đăng nhập người dùng (`dbo.app_users`), lưu trữ Salt và Password Hash bằng PBKDF2. Cấp vé phiên (Ticket 128-bit) sau khi đăng nhập thành công để cho phép thao tác Encrypt/Decrypt.
- **Bài 2.4 - RC4:** Triển khai mã dòng RC4 với KSA (Key-Scheduling Algorithm) và PRGA (Pseudo-Random Generation Algorithm), khóa từ 1 - 256 byte, xuất bản mã dạng chuỗi HEX.
- **Bài 2.5 - Twofish:** Triển khai thuật toán Twofish (sử dụng thư viện BouncyCastle) chế độ CBC, đệm PKCS7. Khóa 256-bit tạo từ SHA-256(Key), sinh và khôi phục IV 16 byte (32 ký tự HEX). Đọc/ghi tệp theo định dạng dòng 1 là `IV=...`, dòng 2 là bản mã Base64.
- **Bài 2.6 - Bài tập nâng cao:**
  - *Nâng cao 01 (Java):* Mã hóa dòng AES-256 chế độ CTR (Counter Mode, NoPadding), sinh khóa ngẫu nhiên 32 byte và Nonce 12 byte (Base64), bộ đếm 4 byte bắt đầu từ 1. Độ dài bản mã bằng đúng độ dài bản rõ.
  - *Nâng cao 02 (Python Tkinter):* Xây dựng ứng dụng `aes_cbc_gui.py` sử dụng thư viện `cryptography` thực hiện mã hóa AES-256-CBC với đệm PKCS7, hỗ trợ giao diện đồ họa Tkinter và chế độ tự kiểm thử qua tham số dòng lệnh `--self-test`.
- **Cơ sở dữ liệu & Quản lý phiên làm việc:** Kết nối Microsoft SQL Server Express (JDBC, cổng 14333) ghi nhận toàn bộ nhật ký thao tác mã hóa/giải mã vào bảng `dbo.crypto_history` và hiển thị trên giao diện `frm_LichSu`.
- **Menu điều hướng:** Tích hợp `frm_Menu` làm giao diện chính liên kết mở toàn bộ các form chức năng và kết nối SQL.

## Môi trường và công nghệ

- **Java Development Kit:** OpenJDK / Oracle JDK 17 hoặc 21.
- **IDE:** Apache NetBeans (bản 20 trở lên) với Maven Project.
- **Python:** Python 3.10+ (thư viện `cryptography`, `tkinter`).
- **Cơ sở dữ liệu:** Microsoft SQL Server Express (cổng 14333, cấu hình TCP/IP, Mixed Mode Authentication).
- **Thư viện phụ thuộc (Maven pom.xml):**
  - `com.microsoft.sqlserver:mssql-jdbc:12.8.1.jre11` (kết nối SQL Server).
  - `org.bouncycastle:bcprov-jdk18on:1.78.1` (thuật toán Twofish).

## Hướng dẫn cài đặt và chạy chương trình

### 1. Chạy ứng dụng Java Swing
1. Mở Apache NetBeans, mở dự án Maven `LAB2_MaHoaHienDai` (thư mục `C:\BaoMat\LAB2_1150080154_PhamMinhQuan\LAB2_MaHoaHienDai`).
2. Thực hiện **Clean and Build** (`Shift + F11`) để Maven tự động tải các dependencies từ internet.
3. Chạy chương trình chính bằng phím `F6` (hoặc nhấp chuột phải vào `frm_Menu.java` chọn **Run File** / `Shift + F6`).
4. Tại Menu chính:
   - Bấm **Kết nối SQL Server**, nhập thông tin, bấm **Test Connection** để kết nối.
   - Chọn các bài thực hành từ 2.1 đến 2.6 để mã hóa, giải mã và lưu/mở tệp bản mã.
   - Riêng bài **2.3 AES**: cần Đăng ký tài khoản (User/Pass) rồi Đăng nhập để nhận vé phiên (Ticket) trước khi thực hiện mã hóa.
   - Bấm **Xem lịch sử** (`frm_LichSu`) để theo dõi các thao tác đã ghi nhận trong cơ sở dữ liệu. Dùng tệp `03_Kiem_Tra_Du_Lieu.sql` trong SSMS để đối soát.

### 2. Chạy bài nâng cao Python (AES-CBC)
1. Cài đặt thư viện phụ thuộc:
   ```cmd
   py -m pip install cryptography
   ```
2. Kiểm tra chế độ tự kiểm thử (Self-test):
   ```cmd
   cd C:\BaoMat\LAB2_1150080154_PhamMinhQuan\LAB2_Python
   py aes_cbc_gui.py --self-test
   ```
3. Chạy giao diện đồ họa Tkinter:
   ```cmd
   py aes_cbc_gui.py
   ```

## Lưu ý

- **Độ dài khóa:**
  - DES: đúng 8 ký tự ASCII (8 byte).
  - TripleDES: đúng 24 ký tự ASCII (24 byte).
  - Twofish: khóa tự do phái sinh qua SHA-256; IV bắt buộc đủ 16 byte HEX (32 ký tự).
  - AES-CTR: Key 32 byte Base64, Nonce 12 byte Base64 (tạo tự động bằng nút bấm).
- **Bảo mật và toàn vẹn:**
  - AES-GCM tự động phát hiện mọi thay đổi trên Ciphertext (Authentication Tag Mismatch).
  - Mật khẩu người dùng được băm qua PBKDF2 kết hợp Salt ngẫu nhiên, không lưu mật khẩu dạng rõ trên hệ thống.
