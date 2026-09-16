package vn.edu.eaut.library.dao;

import vn.edu.eaut.library.model.Transaction;
import vn.edu.eaut.library.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public boolean insert(Transaction t) {
        String sql = "INSERT INTO transactions (user_id, type, amount, description) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getUserId());
            ps.setString(2, t.getType());
            ps.setLong(3, t.getAmount());
            ps.setString(4, t.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi ghi giao dịch", e);
        }
    }

    /** Lịch sử giao dịch của riêng 1 user (dùng cho trang Ví của reader) */
    public List<Transaction> findByUser(int userId) {
        String sql = "SELECT * FROM transactions WHERE user_id=? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Transaction> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs, false));
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy lịch sử giao dịch", e);
        }
    }

    /** Toàn bộ giao dịch, có thể lọc theo username (dùng cho thủ thư). username null/rỗng = lấy hết */
    public List<Transaction> search(String username) {
        boolean hasFilter = username != null && !username.trim().isEmpty();
        String sql = "SELECT t.*, u.username AS username FROM transactions t " +
                "JOIN users u ON u.user_id = t.user_id " +
                (hasFilter ? "WHERE u.username LIKE ? " : "") +
                "ORDER BY t.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasFilter) ps.setString(1, "%" + username.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Transaction> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs, true));
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm giao dịch", e);
        }
    }

    private Transaction mapRow(ResultSet rs, boolean withUsername) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setUserId(rs.getInt("user_id"));
        t.setType(rs.getString("type"));
        t.setAmount(rs.getLong("amount"));
        t.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) t.setCreatedAt(ts.toLocalDateTime());
        if (withUsername) t.setUsername(rs.getString("username"));
        return t;
    }
}
