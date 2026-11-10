USE [LAB1_MaHoaCoDien];
GO
SELECT TOP (20) id, created_at, algorithm, operation, input_text, result_text
FROM dbo.history ORDER BY id DESC;
GO
SELECT COUNT(*) AS so_ban_ghi FROM dbo.history;
GO
SELECT algorithm, COUNT(*) AS so_lan FROM dbo.history GROUP BY algorithm ORDER BY so_lan DESC;
GO
