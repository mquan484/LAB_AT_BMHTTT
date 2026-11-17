USE LAB2_MaHoaHienDai;
GO
SELECT DB_NAME() AS database_name, @@SERVERNAME AS sql_server_name;
SELECT TOP (20) user_id, username, created_at FROM dbo.app_users ORDER BY user_id DESC;
SELECT TOP (50) history_id, created_at, algorithm, operation,
       app_username, input_length, output_length
FROM dbo.crypto_history
ORDER BY history_id DESC;
SELECT COUNT(*) AS so_dong_lich_su FROM dbo.crypto_history;
GO