# LAB 5 Cấu hình firewall pfSense và phân vùng LAN DMZ

- **Họ tên:** Phạm Minh Quân
- **MSSV:** 1150080154
- **Lớp:** 11THMT

## Môi trường

- **Firewall:** pfSense 2.7.2 trên máy ảo VMware, sử dụng ba card mạng WAN, LAN và DMZ (OPT1).
- **WAN (em0):** Bridged, nhận DHCP `192.168.100.127/24` tại thời điểm thực hành.
- **LAN (em1):** mạng Custom, gateway `10.0.0.1/8`.
- **DMZ (em2):** LAN Segment, gateway `172.16.0.1/16`.
- **Domain Controller:** Windows Server 2025, IP `10.0.0.2/8`, gateway `10.0.0.1`; forest AD DS `vietnam.local`, DNS Forwarder `8.8.8.8`.
- **LAN-Test:** Ubuntu, IP `10.0.0.3/8`, gateway `10.0.0.1`.
- **DMZ-Web:** IP `172.16.0.2/16`, gateway `172.16.0.1`.

## Cách dựng môi trường

1. Gắn ba adapter của pfSense vào đúng mạng WAN, LAN và DMZ; đối chiếu console để xác nhận interface và prefix. Ba mạng IPv4 không chồng lấn.
2. Đặt IP tĩnh và gateway cho Domain Controller, LAN-Test và DMZ-Web theo mô hình trên. Cấu hình AD DS và DNS trên Windows Server.
3. Truy cập giao diện quản trị pfSense từ LAN, kiểm tra các interface đang hoạt động và giữ **Anti-Lockout Rule** để duy trì quyền quản trị.
4. Giữ **Automatic Outbound NAT**, kiểm tra các rule tự sinh dịch nguồn LAN `10.0.0.0/8` và DMZ `172.16.0.0/16` sang WAN address.
5. Tắt hai rule **Default allow LAN to any** (IPv4 và IPv6). Kiểm thử rule nền tảng **LAN to Internet** trước khi thay bằng các rule riêng cho từng tình huống.
6. Sau mỗi lần đổi rule, **Apply Changes** và xử lý state cũ bằng **Diagnostics > States > Reset States** trước khi kiểm thử lại.

## Các tình huống và kết quả

| Tình huống | Nội dung | Kết quả quan sát |
|---|---|---|
| Nền tảng | Bật và tắt rule LAN to Internet | Khi bật, DC ping `8.8.8.8` nhận 4/4 gói; khi tắt và reset state, mất 100% gói |
| TH1 | Chặn ICMP, cho phép DNS và Web từ LAN | Ping mất 100% gói; DNS trực tiếp qua `8.8.8.8` trả về bản ghi A/AAAA; HTTPS IPv4 trả về HTML Example Domain |
| TH2 | Chỉ cho DC `10.0.0.2` ra Internet | DC ping thành công với 0% loss; LAN-Test `10.0.0.3` bị chặn với 100% loss |
| TH3 | Cô lập DMZ khỏi LAN nhưng vẫn cho DMZ ra Internet | Trước khi chặn, DMZ-Web ping DC nhận đủ bốn phản hồi; sau khi thêm rule Block, ping DC bị chặn nhưng ping Internet vẫn thành công |

### Rule sử dụng trong từng tình huống

- **TH1, tab LAN:** Block ICMP từ LAN net tới Any; Pass TCP/UDP tới cổng đích `53`; Pass TCP tới cổng đích `80` và `443`. Tắt rule LAN to Internet và các rule mặc định cho phép tổng quát.
- **TH2, tab LAN:** Pass Any từ host `10.0.0.2` tới Any đặt trước Block Any từ LAN net tới Any.
- **TH3, tab DMZ:** Block Any từ DMZ net tới LAN net đặt trước Pass Any từ DMZ net tới Any.

## Lưu ý khi kiểm thử và khắc phục sự cố

- Đặt rule chặn cụ thể trước rule Pass rộng; kiểm tra đúng tab interface của máy nguồn và reset state cũ sau khi thay đổi.
- Phân biệt NAT và firewall rule: Outbound NAT dịch địa chỉ nguồn, còn quyền cho phép hoặc chặn lưu lượng nằm ở firewall rule.
- Với TH1, kiểm thử riêng bằng `ping 8.8.8.8`, `Resolve-DnsName example.com -Server 8.8.8.8` và `curl.exe -4 https://example.com`.
- Dùng alias chứa hai cổng `80`, `443` hoặc hai rule riêng; không dùng dải `80-443` vì sẽ mở thêm các cổng ngoài yêu cầu.
- Ảnh cấu hình IPv4 ban đầu của DMZ-Web chưa có DNS. Khi kiểm thử tên miền, kiểm tra DNS client riêng để tránh nhầm lỗi phân giải với lỗi firewall.
- Khi kiểm thử DMZ tới DC, đối chiếu Windows Firewall trên DC để xác định việc chặn thuộc pfSense hay hệ điều hành máy đích. Khôi phục bảo vệ máy đích sau thực hành.
- Phần logging và hardening trong báo cáo là trả lời lý thuyết, không coi là minh chứng đã thực hiện thêm tình huống kiểm thử.

## Báo cáo

Chi tiết cấu hình, ảnh minh chứng và phần trả lời câu hỏi nằm trong [LAB5_11THMT_1150080154_PhamMinhQuan.docx](LAB5_11THMT_1150080154_PhamMinhQuan.docx).
