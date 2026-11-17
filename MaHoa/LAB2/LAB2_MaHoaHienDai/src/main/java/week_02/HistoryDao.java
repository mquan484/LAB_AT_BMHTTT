package week_02;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

/** Ghi và đọc bảng dbo.crypto_history (chỉ lưu độ dài dữ liệu, KHÔNG lưu bản rõ hay khóa). */
public final class HistoryDao {

    private HistoryDao() {
    }

    public static void insert(String algorithm, String operation, String username, int inputLength, int outputLength)
            throws SQLException {
        String sql = "INSERT INTO dbo.crypto_history(algorithm, operation, app_username, input_length, output_length) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DbConfig.open(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, algorithm);
            ps.setString(2, operation);
            ps.setString(3, username);
            ps.setInt(4, inputLength);
            ps.setInt(5, outputLength);
            ps.executeUpdate();
        }
    }

    /** Thử ghi lịch sử, không làm dừng chương trình; trả về câu thông báo cho lbl_status. */
    public static String tryInsert(String algorithm, String operation, String username, int inputLength, int outputLength) {
        if (!DbConfig.isReady()) {
            return "(Chưa kết nối SQL Server - chưa lưu lịch sử)";
        }
        try {
            insert(algorithm, operation, username, inputLength, outputLength);
            return "(Đã lưu lịch sử SQL Server)";
        } catch (SQLException ex) {
            return "(Lỗi lưu lịch sử: " + ex.getMessage() + ")";
        }
    }

    public static DefaultTableModel emptyModel() {
        return new DefaultTableModel(new Object[] {"ID", "Thời gian", "Thuật toán", "Thao tác", "User", "Độ dài vào", "Độ dài ra"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static DefaultTableModel loadLatest(int top) throws SQLException {
        DefaultTableModel model = emptyModel();
        String sql = "SELECT TOP (?) history_id, created_at, algorithm, operation, app_username, input_length, output_length "
                + "FROM dbo.crypto_history ORDER BY history_id DESC";
        try (Connection con = DbConfig.open(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, top);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[] {rs.getLong(1), rs.getTimestamp(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getInt(6), rs.getInt(7)});
                }
            }
        }
        return model;
    }
}