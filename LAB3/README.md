# LAB 3 Nhận diện và ứng phó các mối đe dọa đến an toàn thông tin

- **Họ tên:** Phạm Minh Quân
- **MSSV:** 1150080154
- **Lớp:** 11THMT

## Môi trường

Windows 11 25H2 build 26200.9445 trên VMware Workstation Pro 26H1, mạng Host-only; PowerShell 5.1, Microsoft Defender, Python 3.14.7, Wireshark 4.6.8, Sysmon 15.22, Autoruns 14.3 và Process Explorer 17.14.

## Cách dựng môi trường

Tạo VM Windows 11 và snapshot sạch, đặt card mạng ở Host-only, tạo thư mục `C:\LAB3`, kiểm tra SHA-256 rồi giải nén gói dữ liệu lab. Cài đúng phiên bản công cụ, giữ Defender và Tamper Protection bật, sau đó thu baseline trước khi thực hành.

## Các tình huống và kết quả

| Tình huống | Nội dung | Kết quả |
|---|---|---|
| TH1 | Risk register và phân loại nguồn đe dọa | PASS |
| TH2 | Kiểm chứng Defender bằng EICAR | PASS |
| TH3 | Sự kiện xác thực 4624, 4625, 4648 và đổi mật khẩu | PASS |
| TH4 | Persistence, Sysmon, Autoruns và listener 8080 | PASS |
| TH5 | So sánh HTTP và HTTPS/TLS bằng Wireshark | PASS |
| TH6 | Phân tích DoS, DDoS và mail bombing offline | PASS |
| TH7 | Phishing và Social Engineering offline | PASS |
| Cleanup | Gỡ artefact, dừng listener, xóa tài khoản lab và kiểm tra Defender | PASS |

## Lỗi gặp phải và cách khắc phục

- EICAR bị Defender chặn ngay khi ghi: kiểm tra Protection history và `Get-MpThreatDetection` để lưu bằng chứng.
- Security log có nhiều sự kiện: lọc theo Event ID 4624, 4625, 4648, tài khoản `lab3user` và khoảng thời gian thực hành.
- Sau bài lab còn artefact thử nghiệm: gỡ Run value, Scheduled Task, tài khoản `lab3user`, dừng đúng PID đang nghe cổng 8080 và kiểm tra lại trạng thái Defender.
