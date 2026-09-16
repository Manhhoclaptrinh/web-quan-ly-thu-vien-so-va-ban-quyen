package vn.edu.eaut.library.dao;

import vn.edu.eaut.library.model.Membership;
import vn.edu.eaut.library.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MembershipDAO {

    public boolean insert(Membership m) {
        String sql = "INSERT INTO memberships (user_id, plan_type, price, start_date, end_date) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.getUserId());
            ps.setString(2, m.getPlanType());
            ps.setLong(3, m.getPrice());
            ps.setTimestamp(4, Timestamp.valueOf(m.getStartDate()));
            ps.setTimestamp(5, Timestamp.valueOf(m.getEndDate()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi đăng ký hội viên", e);
        }
    }

    /** Bản ghi hội viên còn hiệu lực gần nhất của user, null nếu không phải hội viên / đã hết hạn */
    public Membership findActiveByUser(int userId) {
        String sql = "SELECT * FROM memberships WHERE user_id=? AND end_date >= NOW() ORDER BY end_date DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs, false);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi kiểm tra hội viên", e);
        }
        return null;
    }

    public boolean isActiveMember(int userId) {
        return findActiveByUser(userId) != null;
    }

    /** Toàn bộ lịch sử đăng ký hội viên, lọc theo username nếu có (dùng cho thủ thư) */
    public List<Membership> search(String username) {
        boolean hasFilter = username != null && !username.trim().isEmpty();
        String sql = "SELECT m.*, u.username AS username FROM memberships m " +
                "JOIN users u ON u.user_id = m.user_id " +
                (hasFilter ? "WHERE u.username LIKE ? " : "") +
                "ORDER BY m.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasFilter) ps.setString(1, "%" + username.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Membership> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs, true));
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm lịch sử hội viên", e);
        }
    }

    private Membership mapRow(ResultSet rs, boolean withUsername) throws SQLException {
        Membership m = new Membership();
        m.setMembershipId(rs.getInt("membership_id"));
        m.setUserId(rs.getInt("user_id"));
        m.setPlanType(rs.getString("plan_type"));
        m.setPrice(rs.getLong("price"));
        Timestamp start = rs.getTimestamp("start_date");
        Timestamp end = rs.getTimestamp("end_date");
        Timestamp created = rs.getTimestamp("created_at");
        if (start != null) m.setStartDate(start.toLocalDateTime());
        if (end != null) m.setEndDate(end.toLocalDateTime());
        if (created != null) m.setCreatedAt(created.toLocalDateTime());
        if (withUsername) m.setUsername(rs.getString("username"));
        return m;
    }
}
