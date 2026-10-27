# LAB 8 DNS Spoofing và DNS Cache Poisoning trong mạng cô lập

- **Họ tên:** Phạm Minh Quân
- **MSSV:** 1150080154
- **Lớp:** 11THMT

Bài thực hành đối chiếu ba cách làm sai lệch kết quả phân giải tên miền: sửa tệp hosts trên máy trạm, giả mạo phản hồi trực tiếp cho Victim và đầu độc cache của BIND9. Các phép thử sử dụng tên miền nội bộ và trang web mô phỏng, không thu thập tài khoản hay dữ liệu người dùng.

Báo cáo được lưu trong LAB8 nhưng vẫn sử dụng tên miền `lab9.test`, script có hậu tố `lab9` và một số nhãn LAB9. README giữ nguyên các định danh này để đối chiếu với ảnh minh chứng.

## Môi trường

- **Nền tảng:** các máy ảo trên VMware, pfSense kiểm soát lưu lượng giữa DNSLAB và các phân đoạn mạng khác.
- **DNSLAB:** mạng `10.9.0.0/24`, gateway `10.9.0.1`; dành riêng cho thử nghiệm DNS, tách khỏi LAN-DOMAIN.
- **LAN-DOMAIN:** mạng `10.6.0.0/24`, không phải mục tiêu thử nghiệm; không đầu độc DNS của DC01.
- **MGMT:** mạng `192.168.56.0/24`, được chặn từ DNSLAB trong ruleset của bài.
- **DMZ:** mạng `172.16.6.0/24`, chứa máy chủ web hợp lệ và máy chủ DNS authoritative mô phỏng.

| Máy | Địa chỉ trong kịch bản | Vai trò |
|---|---|---|
| DNS-SERVER | `10.9.0.10/24`, card `ens33` | Ubuntu chạy BIND9, nhận truy vấn của Victim và lưu cache |
| VICTIM | `10.9.0.100/24`, card `ens33` | Máy trạm Ubuntu, dùng DNS `10.9.0.10` |
| ATTACKER | `10.9.0.200/24`, card `eth0` | Kali Linux, bắt gói DNS và phục vụ trang web mô phỏng tại cổng `80` |
| APP-TEST / AUTH-DNS | `172.16.6.20` | Chạy `auth_dns_delayed.py`, trả lời DNS với độ trễ `1.5` giây |
| DMZ-WEB | `172.16.6.10` | Website hợp lệ dùng làm đối chứng |

Luồng phân giải bình thường: **Victim → DNS-SERVER → AUTH-DNS**, trả về `172.16.6.10` cho `www.external.lab9.test`. Khi kết quả bị làm sai lệch, Victim nhận `10.9.0.200` và truy cập trang mô phỏng trên Kali.

## Cách dựng môi trường

1. Gắn các máy vào đúng phân đoạn DNSLAB và DMZ; kiểm tra địa chỉ, prefix, route và DNS bằng `ip -br addr`, `ip route`, `resolvectl status`. Victim dùng gateway `10.9.0.1` và DNS `10.9.0.10`.
2. Trên pfSense, cấu hình năm rule DNSLAB theo bảng bên dưới; lưu và áp dụng thay đổi trước khi kiểm thử. Kiểm tra các adapter phụ để tránh tạo đường đi ngoài pfSense.
3. Trên DNS-SERVER, kiểm tra BIND9 bằng `named-checkconf`, `named-checkzone` cho zone thuận `lab9.test` và zone nghịch `9.10.in-addr.arpa`. Khởi động lại dịch vụ, xác nhận listener TCP/UDP `53` tại `10.9.0.10`.
4. Trên APP-TEST, chuẩn bị authoritative DNS mô phỏng tại `172.16.6.20:53`, có độ trễ `1.5` giây. Chuẩn bị web hợp lệ tại `172.16.6.10` và trang mô phỏng tại `10.9.0.200:80`.
5. Ghi nhận baseline của `www.external.lab9.test` qua DNS-SERVER trước mỗi thử nghiệm; địa chỉ hợp lệ là `172.16.6.10`. Phân biệt kết quả DNS với kết quả phân giải qua tệp hosts.
6. Thực hiện riêng Task 1, Task 2 và Task 3 trong phạm vi lab. Báo cáo dùng `dns_spoof_lab9.py` với hai chế độ `victim` và `resolver`; đối chiếu kết quả trên Victim với cache BIND9 và gói tin thay vì chỉ dựa vào thông báo của script.
7. Dùng Wireshark trên `eth0` để đối chiếu truy vấn, phản hồi giả và phản hồi hợp lệ. Sau thử nghiệm, dừng script, khôi phục hosts, làm sạch cache và kiểm tra lại IP hợp lệ.

Các script và zone file được nhắc trong báo cáo nhưng không có tệp nguồn riêng trong thư mục LAB8; README mô tả quy trình và kết quả, không thay thế bộ cấu hình để tái lập đầy đủ môi trường.

## Ruleset cô lập DNSLAB

Thứ tự năm rule trên tab **DNSLAB** trong ảnh cấu hình pfSense:

| Ưu tiên | Action | Source | Destination | Protocol / port |
|---|---|---|---|---|
| 1 | Pass | DNS-SERVER `10.9.0.10` | AUTH-DNS `172.16.6.20` | IPv4 TCP/UDP `53` |
| 2 | Pass | DNSLAB subnets | DMZ-WEB `172.16.6.10` | IPv4 TCP `80` |
| 3 | Block | DNSLAB subnets | LAN-DOMAIN `10.6.0.0/24` | IPv4 Any |
| 4 | Block | DNSLAB subnets | MGMT `192.168.56.0/24` | IPv4 Any |
| 5 | Block | DNSLAB subnets | Any | IPv4 Any |

Rule web trong ảnh áp dụng cho cả DNSLAB subnets, không chỉ riêng Victim. Các kết nối giữa Victim, DNS-SERVER và Kali trên cùng subnet không tự đi qua pfSense; ruleset này kiểm soát lưu lượng được định tuyến qua firewall, không thay thế kiểm soát lớp 2.

## Các tình huống và kết quả

| Tình huống | Nội dung | Kết quả quan sát |
|---|---|---|
| Chuẩn bị BIND9 | Kiểm tra cấu hình, zone và listener | Sau lệnh gõ sai ban đầu, `named-checkconf` chạy không báo lỗi; hai zone trả `OK`; có listener TCP/UDP `53` tại `10.9.0.10` |
| Baseline | Hỏi DNS-SERVER về `www.external.lab9.test` | Có câu trả lời A `172.16.6.10`; ảnh đồng thời có thông báo timeout trước câu trả lời thành công |
| Task 1 Hosts file | Đổi phân giải cục bộ của `www.lab9.test` | `getent hosts` trả `10.9.0.200`; `curl` nhận HTML trang mô phỏng. Lệnh `dig` trong ảnh hỏi `@10.9.0.200` và bị từ chối, chưa phải phép đối chứng hỏi trực tiếp DNS-SERVER |
| Task 2 Direct DNS Spoofing | Giả mạo phản hồi gửi tới Victim | Script ghi nhận phản hồi giả; `resolvectl query` trên Victim nhận `10.9.0.200`, `curl` trả trang mô phỏng |
| Task 3 DNS Cache Poisoning | Giả mạo phản hồi gửi tới resolver | Victim nhận `10.9.0.200`; sau `rndc dumpdb -cache`, `named_dump.db` chứa bản ghi A của `www.external.lab9.test` trỏ về `10.9.0.200`, TTL còn lại trong ảnh là `38` giây |
| Khôi phục DNS | Làm sạch cache và kiểm tra lại phân giải | Ảnh kiểm tra cuối trả lại A `172.16.6.10`, vẫn có một thông báo timeout trước kết quả |
| Wireshark | Đối chiếu trình tự gói tin | Có phản hồi chứa A `10.9.0.200` và phản hồi hợp lệ chứa A `172.16.6.10`; ảnh tổng quan còn có `Port unreachable` và `Server failure` |
| Kết thúc thử nghiệm | Kiểm tra trạng thái card Kali | `ip link show dev eth0` không còn cờ `PROMISC` |

Báo cáo nêu TTL giả mạo ban đầu là `120` giây và mô tả việc dừng script để kiểm tra cache còn hiệu lực. Ảnh cache xác nhận đã lưu bản ghi giả, nhưng chưa đủ chuỗi minh chứng riêng để kết luận nhiều client đều bị ảnh hưởng sau khi dừng script trong toàn bộ khoảng TTL.

## Lưu ý khi kiểm thử và khắc phục sự cố

- **Phân biệt hosts và DNS:** `getent` và ứng dụng dùng bộ phân giải hệ điều hành có thể chịu ảnh hưởng của `/etc/hosts` theo cấu hình NSS. `dig @10.9.0.10` hỏi trực tiếp DNS-SERVER; không dùng kết quả `dig @10.9.0.200` bị từ chối để kết luận DNS-SERVER vẫn trả IP gốc.
- **Phân biệt cache máy trạm và cache resolver:** `resolvectl flush-caches` xử lý cache phía máy trạm; `rndc flush` xử lý cache BIND9. Chỉ dừng script không tự xóa bản ghi đã được resolver lưu.
- **Đối chiếu gói tin:** kiểm tra cùng giao dịch theo IP nguồn/đích, cổng UDP, Transaction ID và câu hỏi DNS. Trong ảnh Wireshark có truy vấn AAAA nhưng phần answer hiển thị A; cần packet capture đầy đủ để giải thích từng giao dịch, không suy rộng rằng mọi truy vấn đều thành công.
- **Giới hạn cô lập:** DNS-SERVER còn adapter `ens37` với route/DNS khác; Kali còn `eth1`, `eth2`, các địa chỉ thuộc MGMT/DMZ và hai interface cùng mang `10.9.0.200`. Cần đối chiếu hoặc ngắt các đường phụ khi nghiệm thu cô lập; ảnh ruleset riêng chưa chứng minh mọi lưu lượng đều qua pfSense.
- **Promiscuous Mode:** cờ `PROMISC` trên máy khách và chính sách chuyển tiếp của switch ảo là hai lớp cấu hình khác nhau. Ảnh cuối chỉ xác nhận cờ trên `eth0` đã tắt, chưa xác nhận toàn bộ cấu hình hypervisor hay các adapter còn lại.
- **Timeout và lỗi DNS:** kiểm tra dịch vụ, route, firewall, cache và log theo đúng thời điểm. Một câu trả lời đúng sau timeout không chứng minh dịch vụ hoạt động ổn định trong mọi lượt thử.
- **Khôi phục:** trả lại `/etc/hosts`, dừng dịch vụ mô phỏng không còn dùng, làm sạch cache máy trạm và resolver, xác nhận IP hợp lệ và rà soát các thiết lập nới lỏng phục vụ lab.
- **Hardening:** phần câu hỏi đề cập DNSSEC validation, DNS Cookies, phân đoạn mạng và kiểm soát DNS egress. Đây là nội dung phân tích/đề xuất, chưa có minh chứng triển khai đầy đủ trong báo cáo; không coi DNS Cookies hoặc DNSSEC validation đơn lẻ là bảo đảm chống mọi giả mạo, nhất là với zone nội bộ chưa ký.

## Báo cáo và tệp nộp

- [LAB8_THMT_1150080154_PhamMinhQuan.docx](LAB8_THMT_1150080154_PhamMinhQuan.docx): báo cáo ba task, 18 ảnh minh chứng, phần khôi phục và 10 câu hỏi phân tích.
- [Video quá trình thực hiện](https://youtu.be/si049eX7XiY).
