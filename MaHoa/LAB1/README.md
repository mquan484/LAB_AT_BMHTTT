# LAB 1: Mã hóa cổ điển

- **Họ tên:** Phạm Minh Quân
- **MSSV:** 1150080154
- **Lớp:** 11THMT

## Nội dung thực hiện

- Xây dựng ứng dụng Java Swing mã hóa và giải mã với 6 thuật toán: Caesar, Vigenere, Rail Fence, Playfair, Transposition và Autokey.
- Tích hợp giao diện tổng hợp để chọn thuật toán, nhập dữ liệu và khóa; hỗ trợ đọc, ghi tệp văn bản.
- Minh họa vét cạn khóa Caesar, thống kê tần suất chữ cái và tìm chuỗi khóa Vigenere từ cặp bản rõ – bản mã.
- Kết nối SQL Server qua JDBC để lưu và xem lịch sử mã hóa, giải mã.

## Môi trường và cách chạy

1. Cài JDK 17, Maven và SQL Server; có thể mở dự án `LAB1_MaHoaCoDien` bằng NetBeans.
2. Chạy `03_SQL_SERVER/01_Tao_CSDL.sql` để tạo cơ sở dữ liệu và bảng lịch sử. Tham khảo `02_Tao_Login_MAU.sql` để cấu hình tài khoản phù hợp với máy thực hành.
3. Mở dự án, tải các thư viện Maven và chạy lớp `week_01.frm_Menu`.
4. Nhập thông tin tại giao diện kết nối SQL Server, bấm **Test Connection** trước khi sử dụng chức năng lịch sử. Dùng `03_Kiem_Tra_Du_Lieu.sql` để kiểm tra dữ liệu đã lưu.

## Tệp bài làm

- `LAB1_MaHoaCoDien/`: mã nguồn ứng dụng Java.
- `03_SQL_SERVER/`: các script tạo và kiểm tra cơ sở dữ liệu.
- `*_ciphertext.txt`: các tệp bản mã mẫu.
- [Báo cáo thực hành](LAB1_11THMT_1150080154_PhamMinhQuan.docx).

## Lưu ý

Nhập khóa đúng định dạng của từng thuật toán; ưu tiên văn bản không dấu khi thử nghiệm. Các thuật toán cổ điển chỉ phục vụ học tập, không dùng để bảo vệ dữ liệu thực tế.
