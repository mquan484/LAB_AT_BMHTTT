-- BAI 3 - CO SO DU LIEU SQL SERVER CHO NHAT KY THAO TAC
-- Chay bang tai khoan co quyen tao database trong SQL Server Management Studio.
USE master;
GO
IF DB_ID(N'LAB3_MaHoaKhoaCongKhai') IS NULL
BEGIN
 CREATE DATABASE LAB3_MaHoaKhoaCongKhai;
END;
GO
USE LAB3_MaHoaKhoaCongKhai;
GO
IF OBJECT_ID(N'dbo.History', N'U') IS NULL
BEGIN
 CREATE TABLE dbo.History
 (
 Id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
 Algorithm NVARCHAR(30) NOT NULL,
 Operation NVARCHAR(12) NOT NULL,
 InputLength INT NOT NULL,
 Success BIT NOT NULL,
 CreatedAt DATETIME2(0) NOT NULL
 CONSTRAINT DF_Lab3_History_CreatedAt DEFAULT SYSDATETIME(),
 CONSTRAINT CK_Lab3_InputLength CHECK (InputLength >= 0),
 CONSTRAINT CK_Lab3_Operation CHECK (Operation IN
 (N'ENCRYPT', N'DECRYPT', N'SIGN', N'VERIFY'))
 );
END;
GO
SELECT DB_NAME() AS DatabaseName;
SELECT TOP (10) * FROM dbo.History ORDER BY Id DESC;
GO