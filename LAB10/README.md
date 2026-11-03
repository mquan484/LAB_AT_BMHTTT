# LAB 10 Remote DNS Cache Poisoning trong mạng cô lập

- **Họ tên:** Phạm Minh Quân
- **MSSV:** 1150080154
- **Lớp:** 11_ĐH_THMT
- **Ngày thực hành ghi trong báo cáo:** 03/11/2026

Bài thực hành mô phỏng đầu độc cache DNS từ một phân đoạn mạng khác, đối chiếu trạng thái trước thử nghiệm, quá trình đổi hướng resolver và bước khôi phục BIND9. Tên miền sử dụng là `example.lab10.test`; trang web trên Kali chỉ hiển thị nội dung mô phỏng, không có biểu mẫu đăng nhập hoặc thu thập dữ liệu.

Pha đầu độc dùng `vuln_resolver_lab10.py` thay cho BIND9, kết hợp race relay có độ trễ nhân tạo trên AUTH-DNS. Kết quả này minh họa cơ chế đoán Transaction ID và thay đổi thông tin ủy quyền trong cache; không phải bằng chứng khai thác thành công BIND9 tiêu chuẩn hay máy chủ DNS ngoài Internet.

## Môi trường

- **Nền tảng:** các máy ảo được phân đoạn qua pfSense, tách ATTACKNET khỏi DNSLAB.
- **DNSLAB:** `10.9.0.0/24`, gateway `10.9.0.1`.
- **ATTACKNET:** `10.10.0.0/24`, gateway `10.10.0.1`.
- **DMZ:** `172.16.6.0/24`, gateway `172.16.6.1`.
- **Các mạng được bảo vệ trong ảnh firewall:** LAN `10.6.0.0/24`, MGMT `192.168.56.0/24`.
- **pfSense:** có các địa chỉ gateway trên; bảng môi trường trong báo cáo ghi gateway WAN là `192.168.250.2`.

| Máy | Địa chỉ | Vai trò |
|---|---|---|
| DNS-SERVER | `10.9.0.10/24` | BIND9 ở baseline và khôi phục; resolver mô phỏng ở pha đầu độc, dùng cổng nguồn UDP cố định `33333` |
| VICTIM | `10.9.0.100/24` | Máy trạm kiểm tra phân giải qua DNS-SERVER và truy cập web |
| ATTACKER | `10.10.0.200/24`, card `eth0` | Kali chạy script thử nghiệm, evil DNS và trang web mô phỏng |
| AUTH-DNS / APP-TEST | `172.16.6.20/24` | DNS authoritative mô phỏng tại UDP `53`; race relay tại UDP `5300` |
| DMZ-WEB | `172.16.6.10/24` | Website hợp lệ của `www.example.lab10.test` |

Baseline trả `172.16.6.10` cho `www.example.lab10.test`. Sau khi resolver mô phỏng chấp nhận NS `ns.evil.example.lab10.test` và glue A `10.10.0.200`, truy vấn tiếp theo được chuyển đến evil DNS trên Kali và trả về `10.10.0.200`.

## Cách dựng môi trường

1. Gắn các máy vào đúng DNSLAB, ATTACKNET và DMZ; kiểm tra IP, route và DNS. Kali dùng `10.10.0.200/24`, default route qua `10.10.0.1`; không nối thêm adapter vào DNSLAB.
2. Thiết lập các rule pfSense theo hai bảng bên dưới, lưu và áp dụng thay đổi. Giữ thử nghiệm trong các subnet lab, kiểm tra đường quản trị trước khi áp dụng rule chặn cuối.
3. Chuẩn bị AUTH-DNS tại `172.16.6.20:53`, web hợp lệ tại `172.16.6.10` và web mô phỏng tại `10.10.0.200:80`. DNS-SERVER chạy BIND9 với vùng chuyển tiếp `example.lab10.test` đến AUTH-DNS.
4. Ghi nhận baseline bằng `dig @10.9.0.10 www.example.lab10.test`; kết quả hợp lệ là `172.16.6.10`. Đối chiếu cấu hình cho phép recursion từ DNSLAB và kiểm tra riêng việc từ chối Kali.
5. Kiểm tra `ip -4 addr show eth0`, `ip link show eth0` và `ip route show` trên Kali. Khi kiểm tra khả năng nghe lén, lưu cả kết quả bắt gói đồng thời với truy vấn từ Victim; ảnh địa chỉ và route riêng chưa thay thế phép thử này.
6. Trong pha mô phỏng, dừng BIND9 rồi chạy `vuln_resolver_lab10.py`. Báo cáo sử dụng `kaminsky_remote_lab10.py` và `auth_dns_relay_lab10.py`; relay tạo cửa sổ chờ `3.0` giây, nhận batch đoán TXID và gửi phản hồi mô phỏng khi khớp.
7. Đối chiếu log `[PENDING]`, `[HIT]`, `[CACHE]` với capture tại DNS-SERVER. Sau đó kiểm tra lại DNS và HTTP từ máy trạm, tránh chỉ dựa vào thông báo thành công của script.
8. Kết thúc thử nghiệm, dừng resolver mô phỏng, relay, evil DNS và các dịch vụ mô phỏng không còn dùng; khởi động lại BIND9. Làm sạch cache liên quan, kiểm tra lại IP hợp lệ, quyền recursion của Kali và cổng nguồn của các truy vấn mới.

Các script, tệp cấu hình BIND9, bản export pfSense và `race_attack.pcap` được nhắc trong báo cáo nhưng không có tệp riêng trong thư mục LAB10. README tóm tắt môi trường và minh chứng, không phải bộ triển khai đầy đủ để tái lập bài thực hành.

## Ruleset cô lập mạng

Ảnh cấu hình có **12 rule**, gồm 5 rule trên ATTACKNET và 7 rule trên DNSLAB. Các bảng giữ nguyên source, destination và protocol/port hiển thị trong ảnh.

### Interface ATTACKNET

| Ưu tiên | Action | Source | Destination | Protocol / port |
|---|---|---|---|---|
| 1 | Pass | `10.10.0.200` | DNS-SERVER `10.9.0.10` | IPv4 TCP/UDP `53` |
| 2 | Pass | `10.10.0.200` | AUTH-DNS `172.16.6.20` | IPv4 UDP `5300` |
| 3 | Block + Log | `10.10.0.0/24` | LAN `10.6.0.0/24` | IPv4 Any |
| 4 | Block + Log | `10.10.0.0/24` | MGMT `192.168.56.0/24` | IPv4 Any |
| 5 | Block + Log | `10.10.0.0/24` | Any | IPv4 Any |

### Interface DNSLAB

| Ưu tiên | Action | Source | Destination | Protocol / port |
|---|---|---|---|---|
| 1 | Pass | VICTIM `10.9.0.100` | DNS-SERVER `10.9.0.10` | IPv4 TCP/UDP `53` |
| 2 | Pass | DNS-SERVER `10.9.0.10` | AUTH-DNS `172.16.6.20` | IPv4 UDP `53` |
| 3 | Pass | DNS-SERVER `10.9.0.10` | ATTACKER `10.10.0.200` | IPv4 UDP `53` |
| 4 | Pass | `10.9.0.0/24` | ATTACKER `10.10.0.200` | IPv4 TCP `80` |
| 5 | Block + Log | `10.9.0.0/24` | LAN `10.6.0.0/24` | IPv4 Any |
| 6 | Block + Log | `10.9.0.0/24` | MGMT `192.168.56.0/24` | IPv4 Any |
| 7 | Block + Log | `10.9.0.0/24` | Any | IPv4 Any |

- Rule HTTP trong ảnh áp dụng cho cả DNSLAB, không chỉ riêng Victim. Bảng DNSLAB không có Pass HTTP đến DMZ-WEB `172.16.6.10`; cần đối chiếu cấu hình bổ sung nếu kiểm tra web hợp lệ ở baseline hoặc sau khôi phục.
- Victim và DNS-SERVER cùng subnet nên lưu lượng trực tiếp giữa hai máy không tự đi qua pfSense. Rule số 1 trên DNSLAB không chứng minh firewall đã xử lý những truy vấn nội bộ này.
- pfSense cho phép đường mạng đến cổng `53` không đồng nghĩa BIND9 cho phép recursion. ACL của dịch vụ DNS cần được kiểm tra riêng ở từng pha.
- Các rule chặn trong ảnh là IPv4; cần kiểm tra adapter phụ và IPv6 khi nghiệm thu cô lập, không suy rộng từ hai ảnh ruleset sang mọi đường truyền.

## Các tình huống và kết quả

| Tình huống | Kết quả quan sát trong báo cáo |
|---|---|
| Baseline | `dig @10.9.0.10 www.example.lab10.test` trả `NOERROR`, A `172.16.6.10`, TTL `60` và có cờ `ra` |
| Mạng Kali | `eth0` mang `10.10.0.200/24`, không hiện cờ `PROMISC`; route hiển thị default qua `10.10.0.1` và mạng trực tiếp `10.10.0.0/24` |
| Race relay | Log AUTH-DNS có `[PENDING]`, `delay=3.0s` và `[HIT]`; ảnh ghi nhận TXID `40724` và `43552` ở hai lượt |
| Cache resolver mô phỏng | Terminal dừng BIND9, chạy resolver tại `10.9.0.10:53` với upstream source port `33333`; log `[CACHE] delegation poisoned` ghi NS giả trỏ về `10.10.0.200` |
| Packet capture | Bản đọc `race_attack.pcap` có truy vấn từ `10.9.0.10:33333` đến `172.16.6.20:53`, phản hồi khớp TXID `40724`, rồi các truy vấn đến `10.10.0.200:53` nhận A `10.10.0.200` |
| DNS sau đầu độc | `dig` hỏi `10.9.0.10` trả A `10.10.0.200`; đồng thời có cảnh báo `recursion requested but not available` |
| HTTP sau đầu độc | `curl -i http://www.example.lab10.test/` trả `HTTP/1.0 200 OK`, HTML `LAB 10 - Fake Site` với nội dung mô phỏng trên ATTACKER |
| Khôi phục BIND9 | `systemctl is-active bind9` trả `active`; capture ba truy vấn đến AUTH-DNS có cổng nguồn lần lượt `59334`, `34355`, `58273` |

Ảnh terminal được đặt trong phần kiểm thử Victim nhưng không hiển thị IP nguồn của máy chạy `dig` và `curl`; cần đối chiếu video hoặc capture để xác nhận đúng máy `10.9.0.100`. Mẫu capture đầu độc và log resolver dùng các lượt TXID khác nhau, không ghép chúng thành cùng một giao dịch.

Bảng tổng hợp trong báo cáo nêu sau khôi phục Victim trở lại IP hợp lệ và Kali nhận `REFUSED`, nhưng chưa có ảnh kiểm tra riêng hai kết quả này. Tương tự, nội dung mô tả Kali bắt được `0` gói không kèm ảnh kết quả tcpdump trong 10 hình; ảnh `eth0` không có `PROMISC` cũng chưa xác nhận thiết lập Promiscuous Mode của hypervisor.

## Lưu ý khi kiểm thử và khắc phục sự cố

- **Giới hạn mô phỏng:** relay biết truy vấn đang chờ, cố ý trì hoãn phản hồi và tạo phản hồi khi batch đoán trúng. Không dùng kết quả này để kết luận một attacker từ xa có thể đầu độc BIND9 tiêu chuẩn với cùng điều kiện trên Internet.
- **Đối chiếu giao dịch:** kiểm tra IP, cổng, TXID, tên truy vấn và thời điểm trong cùng một lượt. Phần tcpdump tóm tắt hiển thị `0/1/1` nhưng không in đầy đủ NS/glue; cần capture chi tiết để xác nhận nội dung từng section.
- **Recursion:** cảnh báo thiếu cờ `ra` ở resolver mô phỏng không làm mất bản ghi A đã nhận, nhưng cho thấy phản hồi không thể được đánh giá như hành vi đầy đủ của BIND9. Khi khôi phục, kiểm tra riêng client được phép và Kali bị từ chối.
- **Khôi phục:** xác nhận tiến trình mô phỏng không còn chiếm UDP `53` hoặc `33333`, BIND9 đang chạy và cache đã được xử lý. Kiểm tra lại DNS và HTTP; trạng thái `active` một mình chưa chứng minh mọi chính sách hardening đã đúng.
- **Cổng nguồn:** ba cổng khác nhau là minh chứng quan sát được sau khôi phục, chưa đo được phân bố ngẫu nhiên hoặc entropy thực tế. Không suy ra bảo đảm chống mọi cache poisoning chỉ từ mẫu ba gói hoặc ước lượng lý thuyết gần `2^32`.
- **DNSSEC:** `dnssec-validation auto` bật xác thực với trust anchor mặc định, không bắt buộc mọi zone đều phải ký và không tự ký zone `.test`. Bảo vệ dữ liệu của zone lab cần cấu hình ký và chuỗi tin cậy hoặc trust anchor phù hợp; báo cáo chưa có phép thử xác thực DNSSEC riêng. Đối chiếu [DNSSEC Guide của ISC BIND9](https://bind9.readthedocs.io/en/stable/dnssec-guide.html).
- **Firewall và state:** kiểm tra đúng interface nhận gói, thứ tự first-match và rule đã Apply. Dùng kết nối mới sau khi đổi chính sách; ưu tiên xử lý state đúng luồng để tránh ngắt đường quản trị.
- **Mốc thời gian:** bìa ghi ngày `03/11/2026`, trong khi một số ảnh DNS/HTTP hiển thị ngày `04/11/2026` theo UTC/GMT. Cần đồng bộ đồng hồ máy ảo khi đối chiếu log; không dùng riêng ngày trên bìa để xác định thời điểm của mọi phép thử.

## Báo cáo và tệp nộp

- [LAB10_THMT_1150080154_PhamMinhQuan.docx](LAB10_THMT_1150080154_PhamMinhQuan.docx): báo cáo môi trường, baseline, mô phỏng đầu độc cache, packet capture, kết quả DNS/HTTP, khôi phục BIND9, 10 ảnh minh chứng và 12 câu hỏi phân tích.
- [Video toàn bộ quá trình thực hiện](https://youtu.be/PbOnMvLZfFM).
