# LAB 7 Web Protection với pfSense E2Guardian Squid và ClamAV

- **Họ tên:** Phạm Minh Quân
- **MSSV:** 1150080154
- **Lớp:** 11THMT

## Môi trường

- **Firewall:** pfSense trên VMware, tiếp tục mô hình MGMT, LAN và DMZ từ LAB6; đảm nhiệm gateway và kiểm soát đường truy cập web trực tiếp.
- **WAN (em0):** nhận địa chỉ qua DHCP.
- **MGMT (em1):** mạng `192.168.56.0/24`, địa chỉ pfSense `192.168.56.254`.
- **LAN (em2):** gateway `10.6.0.1/24`; tab interface tương ứng trong cấu hình là `opt1`.
- **DMZ (em3):** gateway `172.16.6.1/24`; tab interface tương ứng là `opt2`.
- **DC01:** DNS `10.6.0.10`, quản lý zone `lab7.test`.
- **Client-A / Client-B:** lần lượt `10.6.0.21` và `10.6.0.22`; dùng DNS `10.6.0.10`, gateway `10.6.0.1` và Manual Proxy `10.6.0.30:8080`.
- **WEB-PROXY:** Ubuntu, IP chuẩn `10.6.0.30/24`, gateway `10.6.0.1`, DNS `10.6.0.10`; chạy E2Guardian, Squid và ClamAV.
- **DMZ-WEB:** Nginx tại `172.16.6.10`, phục vụ 13 virtual host mô phỏng và các file kiểm thử tải xuống.
- **Phiên bản quan sát trong video:** Squid `7.2`, E2Guardian `5.5.9`, ClamAV `1.5.4`; chuỗi phiên bản ClamAV có hậu tố `ubuntu0.26.04.1`, khác môi trường Ubuntu 24.04 cố định trong đề.

Luồng xử lý HTTP dự kiến: **Client → E2Guardian `10.6.0.30:8080` → Squid `127.0.0.1:3128` → website**. E2Guardian gọi scanner ClamAV qua socket local; không mở cổng Squid `3128` trực tiếp cho client.

## Cách dựng môi trường

1. Kiểm tra card mạng, IP, gateway và DNS của từng máy. Trên WEB-PROXY, dùng `ip -br addr`, `ip route`, `resolvectl status`, ping gateway/DC01 và phân giải `normal.lab7.test` trước khi kiểm thử policy.
2. Trên DMZ-WEB, chuẩn bị Nginx với các virtual host `normal`, `china`, `mideast`, `jobs`, `weapons`, `politics`, `religion`, `education`, `entertainment`, `games`, `streaming`, `blockedgame` và `download` thuộc domain `lab7.test`. Kiểm tra bằng `nginx -t` và `curl -H 'Host: normal.lab7.test' http://127.0.0.1/`.
3. Trên DC01, tạo zone `lab7.test`; cả 13 bản ghi A cùng trỏ về `172.16.6.10`. Kiểm tra bằng `Resolve-DnsName normal.lab7.test`, `Resolve-DnsName jobs.lab7.test` và `Resolve-DnsName download.lab7.test`.
4. Cài các gói `squid`, `e2guardian`, `clamav-daemon`, `curl`, `openssl` trên WEB-PROXY. Kiểm tra listener bằng `ss -lntp` và trạng thái dịch vụ bằng `systemctl`; việc cài gói thành công chưa xác nhận chuỗi proxy đã hoạt động.
5. Cấu hình E2Guardian nhận request tại `10.6.0.30:8080` và chuyển tiếp đến Squid tại `127.0.0.1:3128`. Giai đoạn core giữ HTTPS inspection tắt; chọn đường dẫn và cú pháp scanner theo package thực tế.
6. Trên hai client, lưu Manual Proxy `10.6.0.30`, port `8080`; không đưa `.lab7.test` vào danh sách bỏ qua proxy. Thử đúng URL `http://normal.lab7.test/` bằng một kết nối mới.
7. Trên pfSense, tạo alias `LAB7_CLIENTS` gồm đủ `10.6.0.21`, `10.6.0.22`; `LAB7_PROXY` là `10.6.0.30`; `WEB_PORTS` gồm riêng `80`, `443`; `MAIL_PORTS` gồm `25`, `465`, `587`, `110`, `995`, `143`, `993`.
8. Thiết lập ruleset bên dưới trên interface LAN có IP `10.6.0.1`. Rà soát các rule LAB6 còn hoạt động, **Save / Apply Changes**, xử lý state liên quan khi cần rồi tạo request mới để kiểm thử.
9. Cấu hình các nhóm hard block, warning/bypass, allow, lịch giải trí, scanner và giới hạn tải xuống. Đối chiếu từng kịch bản với log và kết quả thực tế trước khi kết luận đạt.

## Chính sách web

Các policy dưới đây là mục tiêu cấu hình và kiểm thử trong báo cáo, không phải danh sách các tính năng đã được xác nhận chạy thành công.

| Nhóm | Website / tài nguyên | Hành vi yêu cầu |
|---|---|---|
| Allow | `normal.lab7.test`, `education.lab7.test` | Truy cập qua proxy; education không hiện cảnh báo |
| Hard block | `china.lab7.test`, `mideast.lab7.test`, `jobs.lab7.test`, `blockedgame.lab7.test` | Từ chối, không cho temporary bypass |
| Warning | `weapons.lab7.test`, `politics.lab7.test`, `religion.lab7.test`, `streaming.lab7.test` | Cảnh báo; cho xác nhận mở tạm `300` giây |
| Theo thời gian | `entertainment.lab7.test`, `games.lab7.test` | Allow trong cửa sổ `LAB7_ENT_60M`, Block ngoài cửa sổ |
| Download | `download.lab7.test` | Cho vào site nhưng vẫn áp dụng quét nội dung và chính sách kích thước |
| Antivirus | `/files/eicar.com.txt` trên site download | Scanner phát hiện và chặn nội dung kiểm thử EICAR qua HTTP |
| Kích thước | `100MB.bin`, `110MB.bin` | Kiểm tra ngưỡng `104857600` byte; từ chối file vượt ngưỡng khi build hỗ trợ strict size |

Khung `20:00–21:00` trong báo cáo chỉ là ví dụ cho cửa sổ 60 phút, không phải thời gian đã đo trên máy ảo. Schedule cố định không tương đương quota thời lượng theo category; Captive Portal Hard Timeout cũng không thay thế quota này.

## Ruleset yêu cầu và cấu hình cần đối chiếu

Thứ tự ruleset tối thiểu theo báo cáo trên **LAN `10.6.0.1` / `opt1`**:

| Ưu tiên | Action | Source | Destination | Protocol / port |
|---|---|---|---|---|
| 1 | Pass | LAN net | DC01 `10.6.0.10` | TCP/UDP `53` |
| 2 | Pass | `LAB7_CLIENTS` | `LAB7_PROXY` | TCP `8080` |
| 3 | Pass | LAN net | Any | Các cổng `MAIL_PORTS` theo yêu cầu LAB |
| 4 | Block | `LAB7_CLIENTS` | Any | TCP `WEB_PORTS` |
| 5 | Pass | `LAB7_PROXY` | Any | TCP `WEB_PORTS` |
| 6 | Pass | `LAB7_PROXY` | DMZ-WEB `172.16.6.10` | TCP `80` |
| 7 | Block | LAN net | Any | Any |

- Bản cấu hình local đã có đủ hai địa chỉ trong `LAB7_CLIENTS`, cùng alias proxy, web và mail.
- Rule DNS đang dùng TCP, chưa khớp yêu cầu TCP/UDP; rule deny cuối nhóm LAB7 cũng đang giới hạn TCP, chưa phải Any. Phía dưới còn các rule kế thừa LAB6, do đó không coi đây là ruleset đã nghiệm thu chống bypass cho mọi giao thức.
- Client, DC01 và WEB-PROXY cùng subnet; lưu lượng trực tiếp giữa các máy này không đi qua pfSense nếu không có cơ chế phân tách bổ sung. Rule client → proxy và DNS trên pfSense không tự chứng minh đã kiểm soát các kết nối nội bộ đó.
- Cả 13 hostname cùng một IP, nên rule IP/schedule trên pfSense không đủ để phân biệt riêng entertainment/game với các nhóm khác. Cần đối chiếu cơ chế lọc theo hostname tại proxy.

## Các tình huống và kết quả

| Tình huống | Kết quả quan sát / giới hạn minh chứng |
|---|---|
| Chuẩn bị DMZ-WEB | `nginx -t` báo thành công; script báo `LAB7 DMZ-WEB ready`; curl loopback với Host header trả HTML Normal Site |
| DNS | DC01 phân giải `normal`, `jobs`, `download` về `172.16.6.10`; chưa xác nhận đủ 13 website truy cập được từ browser |
| Cài proxy và AV | Terminal có phiên bản Squid, E2Guardian, ClamAV; chưa chứng minh dịch vụ chạy đúng chuỗi |
| Manual Proxy | Client-A hiển thị On; Client-B đang nhập `10.6.0.30`, trường port còn chỉnh sửa trong khung hình được trích |
| Firewall | Có ảnh alias và nhập rule; DNS trong ảnh là TCP. Checkbox Log chưa bật tại thời điểm ghi hình |
| Baseline / chống bypass | Client-A có `ERR_CONNECTION_TIMED_OUT` và thao tác tắt proxy; chưa đủ cặp đối chứng bật proxy truy cập thành công / tắt proxy bị chặn kèm log |
| Hard block / warning / allow | Giao diện đích và policy trong báo cáo có phần minh họa; chưa đủ bằng chứng runtime để xác nhận từng nhóm |
| Entertainment / game | Chưa xác nhận được kết quả trong và ngoài lịch với cơ chế phân biệt hostname |
| EICAR / file lớn | Có nội dung yêu cầu và minh họa; chưa đủ kết quả scanner, byte/status và log để xác nhận chặn EICAR hoặc file 110 MB |

Ma trận phần F của báo cáo giữ 10 kịch bản theo đề. Các hình minh họa và kỳ vọng không được xem là kết quả đo thực tế; cần chạy lại từng URL, lưu request/log và bổ sung phép thử đối chứng.

## Lưu ý khi kiểm thử và khắc phục sự cố

- **Không có log proxy:** kiểm tra URL, Manual Proxy, port `8080`, danh sách exception, ứng dụng có dùng proxy, listener và dịch vụ. Timeout trên browser không tự chứng minh proxy đã chặn đúng policy.
- **Chống bypass:** xác nhận đủ hai client trong alias, đúng interface LAN, thứ tự first-match và các rule Pass rộng kế thừa. Bật log đúng rule chặn, đối chiếu source/destination/port/thời điểm và dùng kết nối mới sau Apply Changes.
- **State:** ưu tiên xóa state đúng luồng kiểm thử; Reset States toàn bộ có thể ngắt các phiên đang dùng, kể cả quản trị.
- **Hard block và warning:** giữ nhóm non-bypass riêng; thử trước xác nhận, sau xác nhận và sau `300` giây. Không để template hard block có nút mở tạm.
- **Whitelist:** allow site download không có nghĩa bỏ qua scanner. Kiểm tra exception không vô hiệu hóa quét nội dung, đối chiếu `contentscanexceptions = off` theo hướng dẫn trong báo cáo.
- **Giới hạn tải:** `maxcontentfilecachescansize` là giới hạn cache/scan, không mặc nhiên là hard download limit. Với build hỗ trợ `reply_body_max_size`, kiểm tra `squid -k parse` và thử riêng file ở ngưỡng 100 MiB với file 110 MiB.
- **HTTPS:** khi chỉ tunnel CONNECT và không MITM, scanner không nhận được payload bản rõ. Không suy rộng kết quả HTTP thành kết quả quét HTTPS; chỉ triển khai CA inspection trong phạm vi lab được phép và không công bố private key.
- **Trang block:** tùy biến giao diện không chứng minh policy hoạt động; giữ đúng token của package và kiểm tra riêng quyền bypass.
- **Tệp cấu hình local:** `config.xml` chứa thông tin xác thực LDAP và khóa riêng chứng chỉ; không nằm trong các tệp công bố của lần cập nhật này. Không dùng lại các bí mật lab trên hệ thống thật.

## Báo cáo và tệp nộp

- [11THMT_LAB7_1150080154_PhamMinhQuan.docx](11THMT_LAB7_1150080154_PhamMinhQuan.docx): báo cáo cấu hình, ảnh trích video, phần minh họa chính sách, ma trận 10 kịch bản và câu trả lời lý thuyết.
- [Video toàn bộ quá trình thực hiện](https://youtu.be/NJfV-AZMix8).
