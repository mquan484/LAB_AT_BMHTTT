USE LAB3_MaHoaKhoaCongKhai;
GO
SELECT TOP (30) Id, Algorithm, Operation, InputLength, Success, CreatedAt
FROM dbo.History ORDER BY Id DESC;
GO
SELECT Algorithm, Operation, COUNT(*) AS SoLan
FROM dbo.History GROUP BY Algorithm, Operation ORDER BY Algorithm, Operation;
GO
