package vn.edu.eaut.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import vn.edu.eaut.library.model.BookPermission;
import vn.edu.eaut.library.utils.DBConnection;

public class BookPermissionDAO {

    private static final String BASE_SELECT =
            "SELECT p.*, u.username, b.title AS book_title, g.full_name AS granted_by_name " +
            "FROM book_permissions p " +
            "LEFT JOIN users u ON p.user_id = u.user_id " +
            "LEFT JOIN books b ON p.book_id = b.book_id " +
            "LEFT JOIN users g ON p.granted_by = g.user_id ";

    public List<BookPermission> findAll() {
        List<BookPermission> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY p.granted_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách phân quyền sách", e);
        }
        return list;
    }

    public List<BookPermission> findByUserId(int userId) {
        List<BookPermission> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.user_id = ? ORDER BY p.granted_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm phân quyền sách theo user", e);
        }
        return list;
    }

    public boolean hasPermission(int userId, int bookId, String permissionType) {
        String sql = "SELECT COUNT(*) FROM book_permissions " +
                "WHERE user_id = ? AND book_id = ? AND permission_type = ? " +
                "AND (expiry_date IS NULL OR expiry_date > NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            ps.setString(3, permissionType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi kiểm tra quyền sách", e);
        }
        return false;
    }

    public boolean insert(BookPermission permission) {
        String sql = "INSERT INTO book_permissions (user_id, book_id, permission_type, granted_by, expiry_date) " +
                "VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, permission.getUserId());
            ps.setInt(2, permission.getBookId());
            ps.setString(3, permission.getPermissionType());
            if (permission.getGrantedBy() != null) {
                ps.setInt(4, permission.getGrantedBy());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (permission.getExpiryDate() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(permission.getExpiryDate()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm phân quyền sách", e);
        }
    }

    public boolean delete(int permissionId) {
        String sql = "DELETE FROM book_permissions WHERE permission_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, permissionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa phân quyền sách", e);
        }
    }

    private BookPermission mapRow(ResultSet rs) throws SQLException {
        BookPermission p = new BookPermission();
        p.setPermissionId(rs.getInt("permission_id"));
        p.setUserId(rs.getInt("user_id"));
        p.setUsername(rs.getString("username"));
        p.setBookId(rs.getInt("book_id"));
        p.setBookTitle(rs.getString("book_title"));
        p.setPermissionType(rs.getString("permission_type"));
        int grantedBy = rs.getInt("granted_by");
        if (!rs.wasNull()) {
            p.setGrantedBy(grantedBy);
        }
        p.setGrantedByName(rs.getString("granted_by_name"));
        Timestamp granted = rs.getTimestamp("granted_date");
        if (granted != null) {
            p.setGrantedDate(granted.toLocalDateTime());
        }
        Timestamp expiry = rs.getTimestamp("expiry_date");
        if (expiry != null) {
            p.setExpiryDate(expiry.toLocalDateTime());
        }
        return p;
    }
}