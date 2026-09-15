# BÁO CÁO THỰC HÀNH LAB 1

## ▪ Họ và tên sinh viên:
Phạm Minh Quân

## ▪ Mã số sinh viên:
1150080154

## ▪ Lớp / Học phần:
- **Lớp:** 11THMT
- **Học phần:** An toàn Hệ thống thông tin (`LAB_AT_BMHTTT`)
- **Link Video thực hành YouTube:** [https://youtu.be/9wAA8LGe87c](https://youtu.be/9wAA8LGe87c) *(quay lại toàn bộ quá trình thực hiện bài lab)*

---

## ▪ Tên bài Lab:
**LAB 1: Bắt gói tin Telnet – SSH (Examining SSH & Telnet in Wireshark)**

---

## ▪ Nội dung đã thực hiện:

1. **Thiết lập môi trường mạng & Kiểm tra kết nối:**
   - Xây dựng mô hình 3 máy trên dải mạng Host-Only `192.168.56.0/24`:
     - **Client (Windows 11):** `192.168.56.1` - sử dụng PuTTY 0.85, Wireshark trên interface `Ethernet 2`.
     - **Attacker (Kali Linux):** `192.168.56.101`.
     - **Server (Ubuntu 26.04.1 LTS):** `192.168.56.103`.
   - Kiểm tra kết nối 2 chiều giữa 3 máy bằng lệnh `ping` (đạt 0% packet loss, TTL đúng chuẩn OS).

2. **Khởi tạo tài khoản thực nghiệm trên Server:**
   - Tạo tài khoản `phamminhquan` trên Ubuntu Server bằng lệnh: `sudo adduser phamminhquan`.
   - Mật khẩu ban đầu thiết lập theo MSSV: `1150080154`.

3. **Thực nghiệm giao thức Telnet (TCP Port 23):**
   - Cài đặt dịch vụ Telnet: `sudo apt install inetutils-telnetd -y`.
   - Kiểm tra port 23 đang lắng nghe: `sudo ss -ltnp | grep ":23"`.
   - Kiểm tra kết nối từ Windows Client: `Test-NetConnection 192.168.56.103 -Port 23` (`TcpTestSucceeded : True`).
   - Lắng nghe trên Wireshark với bộ lọc `tcp.port == 23`.
   - Dùng PuTTY kết nối Telnet tới `192.168.56.103:23`, đăng nhập tài khoản `phamminhquan`.
   - Bắt các gói tin Telnet (mỗi ký tự gõ gửi 1 gói tin `1 byte data`).
   - Sử dụng **Follow TCP Stream** (`tcp.stream eq 0`) để khôi phục toàn bộ luồng dữ liệu thô.
   - Thử nghiệm đổi mật khẩu sang chuỗi phức tạp hơn bằng lệnh `sudo passwd phamminhquan` và bắt gói tin lại để đánh giá.

4. **Thực nghiệm giao thức SSH (TCP Port 22):**
   - Cài đặt dịch vụ OpenSSH: `sudo apt install openssh-server -y`.
   - Kích hoạt và khởi chạy: `sudo systemctl enable --now ssh`.
   - Kiểm tra dịch vụ `ssh.service` hoạt động active (running) trên cổng 22.
   - Lắng nghe trên Wireshark với bộ lọc `tcp.port == 22`.
   - Dùng PuTTY kết nối SSH tới `192.168.56.103:22`, xác thực host key và đăng nhập tài khoản `phamminhquan`.
   - Thu thập các gói tin SSH và phân tích **Follow TCP Stream** đối chiếu.
---

## ▪ Kết quả thực hiện:

| Tiêu chí | Giao thức Telnet (Port 23) | Giao thức SSH (Port 22) | Đánh giá & Kết luận |
| :--- | :--- | :--- | :--- |
| **Dữ liệu đường truyền** | Văn bản thuần (Plaintext / Cleartext), không mã hóa. | Dữ liệu nhị phân đã được mã hóa đối xứng (Ciphertext). | Telnet làm lộ 100% nội dung trao đổi; SSH bảo mật dữ liệu tuyệt đối. |
| **Thông tin xác thực** | Bị lộ nguyên văn: Password `1150080154` hiển thị rõ trong Follow TCP Stream. | Hoàn toàn ẩn giấu, không thể đọc được username hay password. | SSH ngăn chặn hoàn toàn nguy cơ nghe lén (Sniffing/Eavesdropping). |
| **Thao tác phím** | Gửi từng ký tự theo thời gian thực (`1 byte data`/gói tin). | Đóng gói và mã hóa an toàn trong các SSH Encrypted Packets. | Telnet làm lộ nhịp gõ phím và nội dung nhập; SSH bảo vệ toàn diện. |
| **Độ dài/phức tạp mật khẩu** | **Hoàn toàn vô hiệu** trên Telnet do kênh truyền không mã hóa. | Kết hợp chặt chẽ với mã hóa kênh truyền để chống tấn công brute-force. | Chứng minh thực nghiệm: Cần bảo mật kênh truyền chứ không chỉ dựa vào mật khẩu phức tạp. |
| **Tính toàn vẹn (Integrity)** | Không có MAC; dễ bị tấn công chèn/sửa gói tin (Packet Injection). | Có mã xác thực thông điệp MAC (HMAC-SHA256) / AEAD bảo vệ. | SSH phát hiện và ngắt kết nối ngay khi gói tin bị can thiệp. |
| **Xác thực máy chủ (Server Identity)** | Không có xác thực Server, dễ bị dựng máy chủ giả mạo. | Máy chủ có Host Key, xác thực với Client qua Host Key Fingerprint. | SSH ngăn chặn tấn công Man-in-the-Middle (MITM). |

---

## ▪ Các lưu ý cần thiết để giảng viên có thể kiểm tra hoặc chạy lại bài làm:

1. **Cấu hình mạng máy ảo:**
   - Cả 3 máy ảo (Windows 11, Kali Linux, Ubuntu Server) phải cùng gắn vào một Virtual Switch ở chế độ **Host-Only** để nằm chung dải mạng nội bộ `192.168.56.0/24`.
   - Kiểm tra lệnh `ping` thông suốt 2 chiều giữa các máy trước khi thử nghiệm dịch vụ.
2. **Quản lý dịch vụ Telnet trên Ubuntu Server:**
   - Dịch vụ Telnet cài qua gói `inetutils-telnetd`.
   - Kiểm tra trạng thái cổng lắng nghe: `sudo ss -ltnp | grep ":23"`.
   - Chỉ thử nghiệm Telnet trong mạng lab cô lập, không công bố TCP/23 ra Internet.
3. **Quản lý dịch vụ SSH trên Ubuntu Server:**
   - Dịch vụ SSH cài qua gói `openssh-server`.
   - Khởi chạy dịch vụ: `sudo systemctl enable --now ssh`.
   - Kiểm tra trạng thái hoạt động: `systemctl status ssh.service` (phải báo active running trên port 22).
4. **Vị trí và bộ lọc bắt gói tin trong Wireshark:**
   - Trong mô hình mạng switch hiện đại, máy Attacker thứ ba không tự động nhìn thấy gói tin unicast giữa Client và Server (do cơ chế bảng CAM của Switch).
   - Để thu thập được toàn bộ lưu lượng unicast của phiên kết nối một cách đầy đủ và chính xác nhất, bài thực hành thực hiện bắt gói tin trực tiếp trên card mạng `Ethernet 2` của Windows Client.
   - Các bộ lọc hiển thị (Display Filter) sử dụng:
     - Lọc Telnet: `tcp.port == 23`
     - Lọc SSH: `tcp.port == 22`
     - Khôi phục luồng phiên: Nhấp chuột phải vào gói tin chọn `Analyze` -> `Follow` -> `TCP Stream`.
