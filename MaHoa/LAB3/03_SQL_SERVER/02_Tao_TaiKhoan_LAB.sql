-- BAI 3 - TAO SQL LOGIN CHI DUOC INSERT/SELECT LICH SU
-- Mật khẩu lab: LabUser@2026 - chỉ dùng trên máy cá nhân; nếu đổi thì gõ đúng mật khẩu mới trên form kết nối.
-- SQL Server phai bat SQL Server and Windows Authentication mode (Mixed mode).
USE master;
GO
IF SUSER_ID(N'lab3_user') IS NULL
BEGIN
 CREATE LOGIN lab3_user WITH PASSWORD = 'LabUser@2026', CHECK_POLICY = ON;
END;
GO
USE LAB3_MaHoaKhoaCongKhai;
GO
IF DATABASE_PRINCIPAL_ID(N'lab3_user') IS NULL
BEGIN
 CREATE USER lab3_user FOR LOGIN lab3_user;
END;
GO
GRANT SELECT, INSERT ON OBJECT::dbo.History TO lab3_user;
