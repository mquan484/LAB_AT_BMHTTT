# LAB 4 Nmap

- **Họ tên:** Phạm Minh Quân
- **MSSV:** 1150080154
- **Lớp:** 11THMT

## Môi trường

- **Máy thật (Host):** Windows 11, VMware Workstation Pro, card mạng ảo `VMware Network Adapter VMnet1` (Host-Only) gán địa chỉ IP `192.168.56.1/24`.
- **Máy quét chính:** Kali Linux 2026 VM (`192.168.56.128/24`), công cụ Nmap 7.99, xsltproc.
- **Máy mục tiêu có lỗ hổng:** Metasploitable 2 VM (`192.168.56.129/24`, nhân Linux kernel 2.6.9 - 2.6.33), mở sẵn 23 dịch vụ mạng TCP.
- **Máy mục tiêu đối chiếu & Hardening:** Windows 11 x64 VM (`192.168.56.130/24`), sử dụng Windows Defender Firewall with Advanced Security.

## Cách dựng môi trường

1. Thiết lập phân đoạn mạng ảo Host-Only `192.168.56.0/24` trên Virtual Network Editor của VMware.
2. Cấu hình card mạng của Kali Linux VM, Metasploitable 2 VM và Windows 11 VM đều ở chế độ Host-Only (VMnet1). Ngắt hoàn toàn kết nối Internet (NAT/Bridged) trước khi thực hành nhằm đảm bảo an toàn cô lập.
3. Kiểm tra và xác nhận địa chỉ IP thực tế của từng thiết bị bằng lệnh `ipconfig` (Windows host, Windows VM), `ip -br addr` (Kali) và `ifconfig` (Metasploitable 2).
4. Kiểm tra kết nối mạng nội bộ thông suốt bằng lệnh `ping` giữa các máy và tạo snapshot sạch trước khi thực hiện các bài quét.

## Các tình huống và kết quả

| Tình huống / Nhiệm vụ | Nội dung thực hiện |
|---|---|
| NV1 - Host Discovery | Phát hiện host toàn dải `192.168.56.0/24` bằng `sudo nmap -sn -n` |
| NV2 - TCP Connect Scan | Khảo sát cổng TCP bằng bắt tay 3 bước đầy đủ (`nmap -sT`) |
| NV3 - SYN Scan (Stealth) | Quét half-open bằng gói SYN (`sudo nmap -sS`), so sánh với `-sT` |
| NV4 - FIN / Xmas / NULL | Kiểm tra phản ứng TCP RFC 793 bằng `-sF`, `-sX`, `-sN` |
| NV5 - ACK Scan | Khảo sát chính sách lọc của tường lửa (`sudo nmap -sA`) |
| NV6 - UDP Scan | Quét 20 cổng UDP phổ biến (`sudo nmap -sU --top-ports 20`) |
| NV7 - Service Version Detection | Định danh phiên bản 23 dịch vụ mở (`sudo nmap -sV`) |
| NV8 - OS Fingerprinting | Nhận diện hệ điều hành mục tiêu bằng `-O` và `-A` |
| NV9 - NSE SMB Scripts | Thu thập thông tin SMB và rà soát lỗ hổng MS17-010 |
| NV10 - Export Evidence | Xuất kết quả ra các định dạng `-oN`, `-oX`, `-oG` và render HTML |
| NV11 - Hardening Assessment | Đánh giá trước/sau khi thiết lập rule Windows Defender Firewall |
