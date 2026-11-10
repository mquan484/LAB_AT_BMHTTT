-- CHỈ DÀNH CHO LAB TRÊN MÁY CÁ NHÂN. Chạy với quyền quản trị SQL Server.
-- Mật khẩu lab: LabUser@2026 - chỉ dùng trên máy cá nhân; nếu đổi thì gõ đúng mật khẩu mới trên form kết nối.
USE [master];
GO
IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = N'lab1_user')
BEGIN
 CREATE LOGIN [lab1_user]
 WITH PASSWORD = 'LabUser@2026',
 CHECK_POLICY = ON,
 CHECK_EXPIRATION = OFF;
END;
GO
USE [LAB1_MaHoaCoDien];
GO
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'lab1_user')
 CREATE USER [lab1_user] FOR LOGIN [lab1_user];
GO
-- Cấp quyền vừa đủ: SELECT, INSERT, DELETE lịch sử. Không cấp db_owner.
GRANT SELECT, INSERT, DELETE ON OBJECT::dbo.history TO [lab1_user];
GO
