# LAB 6 Tích hợp Active Directory và Captive Portal trên pfSense

- **Họ tên:** Phạm Minh Quân
- **MSSV:** 1150080154
- **Lớp:** 11THMT

## Môi trường

- **Firewall:** pfSense trên VMware, sử dụng bốn card mạng WAN, MGMT, LAN và DMZ; dựng topology riêng cho LAB6.
- **WAN (em0):** nhận địa chỉ qua DHCP.
- **MGMT (em1 / OPT2):** mạng Host-Only `192.168.56.0/24`, địa chỉ pfSense `192.168.56.254`; máy thật `192.168.56.1` truy cập WebGUI qua HTTPS cổng `443`.
- **LAN (em2):** gateway `10.6.0.1/24`.
- **DMZ (em3 / OPT1):** gateway `172.16.6.1/24`.
- **DC01:** Windows Server chạy AD DS và DNS, IP `10.6.0.10/24`, gateway `10.6.0.1`, DNS trỏ về chính nó; forest `lab6.test`.
- **Client-A:** IP `10.6.0.21/24`, tài khoản domain kiểm thử `sv_a`.
- **Client-B:** IP `10.6.0.22/24`, tài khoản domain kiểm thử `sv_b`. Hai client dùng gateway `10.6.0.1` và DNS `10.6.0.10`.
- **DMZ-WEB:** Ubuntu chạy Nginx, gateway `172.16.6.1`; các địa chỉ trên `ens33` mô phỏng những nhóm website dưới đây.

| Alias | Địa chỉ | Vai trò |
|---|---|---|
| `GENERAL_SITE` | `172.16.6.10` | Website bình thường, dùng làm đối chứng |
| `JOB_SITE` | `172.16.6.20` | Mô phỏng nhóm Job Search |
| `SOCIAL_SITE` | `172.16.6.30` | Mô phỏng nhóm Social Media |
| `RESTRICTED_SITE` | `172.16.6.40` | Mô phỏng nhóm Restricted Content |

## Cách dựng môi trường

1. Tạo các mạng ảo riêng và gắn đúng bốn adapter của pfSense. Tách MGMT khỏi LAN để giữ đường quản trị khi bật Captive Portal hoặc thay đổi chính sách LAN.
2. Cấu hình các interface và IP tĩnh theo mô hình trên. Trên DC01, cài AD DS và DNS, tạo forest `lab6.test`, cấu hình DNS Forwarders phù hợp với đường ra của môi trường lab.
3. Tạo OU `LAB6-Users` chứa `sv_a`, `sv_b`; tạo tài khoản bind LDAP riêng `svc_pfsense_ldap` trong `CN=Users`.
4. Đặt IP, gateway và DNS cho hai client; kiểm tra kết nối đến DC01 và phân giải `lab6.test`. Trên DMZ-WEB, bật Nginx và gán đủ bốn IP `.10`, `.20`, `.30`, `.40`.
5. Tại **System > User Manager > Authentication Servers**, tạo backend LDAP `LAB6-AD`: host `10.6.0.10`, TCP/389, LDAP v3, Search Scope **Entire Subtree**, Base DN `DC=lab6,DC=test`, Authentication Container `OU=LAB6-Users,DC=lab6,DC=test`. Bind DN là `CN=svc_pfsense_ldap,CN=Users,DC=lab6,DC=test`; các thuộc tính dùng `samAccountName`, `cn`, `memberOf`.
6. Tại **Diagnostics > Authentication**, xác nhận cả `sv_a` và `sv_b` xác thực thành công trước khi cấu hình Portal.
7. Tại **Services > Captive Portal**, bật zone `LAB6_USERS` trên LAN, chọn **Use an Authentication backend** với `LAB6-AD`. Từ client, mở HTTP đến `172.16.6.10` để nhận trang đăng nhập tại `10.6.0.1:8002`.
8. Tạo alias `CLIENT_A`, `CLIENT_B`, `POLICY_CLIENTS` chứa cả hai client, các site và alias cổng `WEB_PORTS` gồm riêng `80`, `443`. Tạo lịch `LUNCH_WINDOW` từ thứ Hai đến thứ Sáu, `11:30–13:00`; `WORK_HOURS` từ thứ Hai đến thứ Sáu, `08:00–17:00`. Múi giờ pfSense là `Asia/Ho_Chi_Minh`.
9. Thiết lập ruleset theo thứ tự bên dưới, **Save / Apply Changes**, xử lý state cũ khi cần rồi kiểm thử bằng kết nối mới. `TEST_NOW` chỉ dùng thử nhanh; cấu hình cuối đã trả rule Social về `LUNCH_WINDOW` và không có rule nào tham chiếu lịch tạm.

## Các tình huống và kết quả

| Tình huống | Nội dung | Kết quả quan sát |
|---|---|---|
| LDAP | Xác thực hai tài khoản qua `LAB6-AD` | `sv_a` và `sv_b` đều được xác thực thành công |
| Captive Portal | Yêu cầu người dùng LAN đăng nhập bằng tài khoản AD | Bảng Users Logged In ghi nhận `sv_a` tại `10.6.0.21` và `sv_b` tại `10.6.0.22` |
| KB1 | Chặn Client-A đến `JOB_SITE`, cho phép `GENERAL_SITE` qua TCP/80 | `.10` mở được Nginx; `.20` timeout và có log khớp rule `LAB6_KB1_BLOCK_A_JOB` |
| KB2 | Chặn cả A/B đến `RESTRICTED_SITE`, dùng `GENERAL_SITE` làm đối chứng | Cả hai client truy cập `.40` thất bại; `.10` trả HTML Nginx; có log chặn tương ứng |
| KB3 | Chỉ cho Client-B đến `SOCIAL_SITE` trong giờ nghỉ | Có lượt mở được `.30` và lượt timeout khi thay đổi trạng thái lịch thử; sau thử nghiệm trả về `LUNCH_WINDOW` |
| KB4 | Cho HTTP/HTTPS theo `WORK_HOURS`, chặn các dịch vụ còn lại | Đã quan sát HTTP nội bộ đến `.10` thành công và ping mất 100%; chưa đủ kiểm thử HTTP/HTTPS Internet trong và ngoài lịch |
| KB5 | Chặn kết nối do DMZ khởi tạo đến LAN, vẫn cho LAN truy cập web cần thiết | DMZ-WEB ping DC01 mất 100%; log ICMP khớp `LAB6_KB5_ISOLATE_DMZ`; chiều LAN đến web DMZ có kết quả thành công |

Các kết quả là những lượt kiểm thử đã ghi nhận trong báo cáo. Chưa thể coi mọi tổ hợp thời gian đã được kiểm thử lại trên ruleset tổng hợp cuối cùng; riêng KB4 cần bổ sung phép thử Internet trong và ngoài `WORK_HOURS`.

## Ruleset tổng hợp

Thứ tự bảy rule trên tab **LAN** trong `config.xml`:

| Ưu tiên | Action | Source | Destination | Dịch vụ | Schedule |
|---|---|---|---|---|---|
| 1 | Block + Log | `CLIENT_A` | `JOB_SITE` | TCP/80 | Luôn áp dụng |
| 2 | Block + Log | `POLICY_CLIENTS` | `RESTRICTED_SITE` | TCP/80 | Luôn áp dụng |
| 3 | Pass | `CLIENT_B` | `SOCIAL_SITE` | TCP/80 | `LUNCH_WINDOW` |
| 4 | Block | `CLIENT_B` | `SOCIAL_SITE` | TCP/80 | Luôn áp dụng |
| 5 | Pass | `POLICY_CLIENTS` | `GENERAL_SITE` | TCP/80 | Luôn áp dụng |
| 6 | Pass | `POLICY_CLIENTS` | Any | TCP/`WEB_PORTS` | `WORK_HOURS` |
| 7 | Block + Log | `POLICY_CLIENTS` | Any | IPv4 Any | Luôn áp dụng |

- **Tab DMZ:** Block + Log IPv4 Any từ DMZ net đến LAN net.
- **Tab MGMT:** Pass TCP từ `192.168.56.1` đến địa chỉ MGMT của pfSense, cổng `443`.
- Hai Block đặc hiệu phải đứng trước Pass web rộng. Cặp rule Social phải giữ Pass theo giờ ở trên Block dự phòng; ngoài lịch, Pass bị bỏ qua chứ không tự đổi thành Block.
- `GENERAL_SITE` là ngoại lệ được phép qua TCP/80 cả ngoài `WORK_HOURS`. Các ảnh kiểm thử riêng có rule bật/tắt trung gian nên không đại diện cho toàn bộ ruleset cuối.

## Lưu ý khi kiểm thử và khắc phục sự cố

- **LDAP và danh tính:** LDAP/Captive Portal xác thực người dùng; firewall rule chuẩn vẫn xét IP, subnet hoặc alias, không trực tiếp dùng username AD làm Source. Đăng nhập Portal không bỏ qua các rule chặn.
- **LDAP lỗi:** kiểm tra đường đến DC01, TCP/389, Base DN, OU, bind DN, mật khẩu và trạng thái tài khoản. Dùng service account riêng; TCP/389 chỉ dành cho mạng lab cô lập, triển khai thực tế cần bảo vệ LDAP bằng TLS/SSL.
- **Portal không hiện:** kiểm tra zone trên LAN, DNS, dùng URL HTTP, phiên đăng nhập cũ và đường đi IPv6. WebGUI quản trị dùng MGMT tại `https://192.168.56.254`.
- **Rule hoặc schedule không đúng:** kiểm tra Apply Changes, thứ tự first-match, interface nhận gói, alias, protocol/port, múi giờ và trạng thái Active của lịch. Tạo kết nối mới; ưu tiên xóa state đúng luồng, vì Reset States toàn bộ có thể ngắt cả phiên quản trị.
- **DNS:** hai client và DC01 cùng subnet nên truy vấn DNS trực tiếp đến `10.6.0.10` không đi qua pfSense. DC01 truy vấn DNS upstream ngoài subnet vẫn cần đường ra và rule cho phép phù hợp.
- **Website mô phỏng:** bốn IP dùng cùng trang Nginx, chỉ phân biệt bằng destination IP; đây không phải Web Category Filtering thật. IP phụ thêm bằng `ip addr add` không tự tồn tại sau khởi động lại nếu chưa lưu cấu hình mạng.
- **Đối chiếu minh chứng:** khi truy cập bị chặn, kiểm tra Firewall Log theo đúng rule, source, destination, port và thời gian. Với KB5, log trên interface DMZ giúp phân biệt pfSense chặn với Windows Firewall trên DC01 chặn.
- **Firewall stateful:** phản hồi từ DMZ cho phiên do LAN khởi tạo khác với kết nối mới do DMZ khởi tạo; không dùng kết quả LAN mở được web để kết luận DMZ được chủ động truy cập LAN.
- **Tệp cấu hình:** `config.xml` là bản sao cấu hình lab có thông tin xác thực và khóa riêng chứng chỉ. Chỉ dùng cho môi trường thực hành; không tái sử dụng các bí mật này trên hệ thống thật và cần thay credential/chứng chỉ khi dựng môi trường khác.

## Báo cáo và tệp nộp

- [11THMT_LAB6_1150080154_PhamMinhQuan.docx](11THMT_LAB6_1150080154_PhamMinhQuan.docx): báo cáo chi tiết, 44 hình minh chứng, bảng kết quả và phần trả lời câu hỏi.
- [config.xml](config.xml): cấu hình pfSense cuối cùng để đối chiếu interface, LDAP, Captive Portal, alias, schedule và firewall rule.
- [Video toàn bộ quá trình thực hiện](https://youtu.be/kVgAEOxqs6E).
