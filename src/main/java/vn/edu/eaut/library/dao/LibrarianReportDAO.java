package vn.edu.eaut.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vn.edu.eaut.library.utils.DBConnection;

/**
 * Thống kê nghiệp vụ dành cho LIBRARIAN.
 * Không thực hiện thêm/sửa/xóa dữ liệu; chỉ đọc và tổng hợp từ các bảng hiện có.
 */
public class LibrarianReportDAO {

    private int count(String sql) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            return r.next() ? r.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy số liệu báo cáo", e);
        }
    }

    public Map<String, Integer> getOverview() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("documents", count("SELECT COUNT(*) FROM documents"));
        m.put("availableDocuments", count("SELECT COUNT(*) FROM documents WHERE status='AVAILABLE'"));
        m.put("books", count("SELECT COUNT(*) FROM books"));
        m.put("availableBooks", count("SELECT COUNT(*) FROM books WHERE status='AVAILABLE'"));
        m.put("videos", count("SELECT COUNT(*) FROM videos"));
        m.put("availableVideos", count("SELECT COUNT(*) FROM videos WHERE status='AVAILABLE'"));
        m.put("categories", count("SELECT COUNT(*) FROM categories"));
        m.put("activeUsers", count("SELECT COUNT(*) FROM users WHERE status='ACTIVE'"));
        m.put("views", count("SELECT COUNT(*) FROM access_history WHERE action_type='VIEW'"));
        m.put("downloads", count("SELECT COUNT(*) FROM access_history WHERE action_type='DOWNLOAD'"));
        m.put("favorites", count("SELECT COUNT(*) FROM favorites"));
        m.put("reviews", count("SELECT COUNT(*) FROM document_reviews")
                + count("SELECT COUNT(*) FROM video_reviews"));
        m.put("pendingRequests", count(
                "SELECT (SELECT COUNT(*) FROM permission_requests WHERE status='PENDING') " +
                "+ (SELECT COUNT(*) FROM video_permission_requests WHERE status='PENDING')"));
        return m;
    }

    public List<Map<String, Object>> accessLast7Days() {
        String sql =
            "SELECT DATE(access_time) day, " +
            "SUM(CASE WHEN action_type='VIEW' THEN 1 ELSE 0 END) views, " +
            "SUM(CASE WHEN action_type='DOWNLOAD' THEN 1 ELSE 0 END) downloads, " +
            "COUNT(*) total " +
            "FROM access_history " +
            "WHERE access_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE(access_time) ORDER BY day";

        Map<String, Map<String, Object>> data = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            String day = java.time.LocalDate.now().minusDays(i).toString();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("day", day);
            row.put("views", 0);
            row.put("downloads", 0);
            row.put("total", 0);
            data.put(day, row);
        }

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                String day = r.getDate("day").toLocalDate().toString();
                Map<String, Object> row = data.get(day);
                if (row != null) {
                    row.put("views", r.getInt("views"));
                    row.put("downloads", r.getInt("downloads"));
                    row.put("total", r.getInt("total"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thống kê truy cập 7 ngày", e);
        }
        return new ArrayList<>(data.values());
    }

    public List<Map<String, Object>> contentByType() {
        List<Map<String, Object>> list = new ArrayList<>();
        addType(list, "Tài liệu", count("SELECT COUNT(*) FROM documents"),
                count("SELECT COUNT(*) FROM documents WHERE status='AVAILABLE'"));
        addType(list, "Sách", count("SELECT COUNT(*) FROM books"),
                count("SELECT COUNT(*) FROM books WHERE status='AVAILABLE'"));
        addType(list, "Video", count("SELECT COUNT(*) FROM videos"),
                count("SELECT COUNT(*) FROM videos WHERE status='AVAILABLE'"));
        return list;
    }

    private void addType(List<Map<String, Object>> list, String type, int total, int available) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", type);
        m.put("total", total);
        m.put("available", available);
        m.put("disabled", total - available);
        list.add(m);
    }

    public List<Map<String, Object>> topContent(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql =
            "SELECT title, content_type, total FROM (" +
            " SELECT d.title, 'Tài liệu' content_type, COUNT(h.history_id) total " +
            " FROM documents d JOIN access_history h ON h.document_id=d.document_id " +
            " WHERE h.action_type IN ('VIEW','DOWNLOAD') GROUP BY d.document_id,d.title " +
            " UNION ALL " +
            " SELECT b.title, 'Sách' content_type, COUNT(h.history_id) total " +
            " FROM books b JOIN access_history h ON h.book_id=b.book_id " +
            " WHERE h.action_type IN ('VIEW','DOWNLOAD') GROUP BY b.book_id,b.title " +
            " UNION ALL " +
            " SELECT v.title, 'Video' content_type, COUNT(h.history_id) total " +
            " FROM videos v JOIN access_history h ON h.video_id=v.video_id " +
            " WHERE h.action_type IN ('VIEW','DOWNLOAD') GROUP BY v.video_id,v.title " +
            ") x ORDER BY total DESC LIMIT ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, Math.max(1, Math.min(limit, 20)));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("title", r.getString("title"));
                    m.put("type", r.getString("content_type"));
                    m.put("total", r.getInt("total"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thống kê nội dung được truy cập nhiều", e);
        }
        return list;
    }

    public List<Map<String, Object>> categoryStats() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql =
            "SELECT c.category_name, " +
            " (SELECT COUNT(*) FROM documents d WHERE d.category_id=c.category_id) documents, " +
            " (SELECT COUNT(*) FROM books b WHERE b.category_id=c.category_id) books, " +
            " (SELECT COUNT(*) FROM videos v WHERE v.category_id=c.category_id) videos " +
            "FROM categories c ORDER BY c.category_name";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", r.getString("category_name"));
                m.put("documents", r.getInt("documents"));
                m.put("books", r.getInt("books"));
                m.put("videos", r.getInt("videos"));
                m.put("total", r.getInt("documents") + r.getInt("books") + r.getInt("videos"));
                list.add(m);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thống kê theo danh mục", e);
        }
        return list;
    }

    public Map<String, Integer> requestStats() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("pending", count("SELECT (SELECT COUNT(*) FROM permission_requests WHERE status='PENDING') + (SELECT COUNT(*) FROM video_permission_requests WHERE status='PENDING')"));
        m.put("approved", count("SELECT (SELECT COUNT(*) FROM permission_requests WHERE status='APPROVED') + (SELECT COUNT(*) FROM video_permission_requests WHERE status='APPROVED')"));
        m.put("rejected", count("SELECT (SELECT COUNT(*) FROM permission_requests WHERE status='REJECTED') + (SELECT COUNT(*) FROM video_permission_requests WHERE status='REJECTED')"));
        return m;
    }

    public Map<String, Object> reviewStats() {
        Map<String, Object> m = new LinkedHashMap<>();
        int reviews = count("SELECT COUNT(*) FROM document_reviews") + count("SELECT COUNT(*) FROM video_reviews");
        double avg = average("SELECT AVG(rating) FROM (SELECT rating FROM document_reviews UNION ALL SELECT rating FROM video_reviews) x");
        m.put("total", reviews);
        m.put("average", avg);
        return m;
    }

    private double average(String sql) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            return r.next() ? r.getDouble(1) : 0.0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thống kê đánh giá", e);
        }
    }

    public Map<String, Object> transactionStats() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("transactions", count("SELECT COUNT(*) FROM transactions"));
        m.put("topups", count("SELECT COUNT(*) FROM transactions WHERE type='TOPUP'"));
        m.put("downloads", count("SELECT COUNT(*) FROM transactions WHERE type='DOWNLOAD'"));
        m.put("views", count("SELECT COUNT(*) FROM transactions WHERE type='VIEW_PDF'"));
        m.put("memberships", count("SELECT COUNT(*) FROM memberships"));
        m.put("revenue", sum("SELECT COALESCE(SUM(CASE WHEN amount < 0 THEN -amount ELSE 0 END),0) FROM transactions"));
        return m;
    }

    private long sum(String sql) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            return r.next() ? r.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thống kê giao dịch", e);
        }
    }

    public Map<String, Integer> licenseStats() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("documents", count("SELECT COUNT(*) FROM licenses WHERE status='VALID' AND (expiry_date IS NULL OR expiry_date >= CURDATE())"));
        m.put("books", count("SELECT COUNT(*) FROM book_licenses WHERE status='VALID' AND (expiry_date IS NULL OR expiry_date >= CURDATE())"));
        m.put("videos", count("SELECT COUNT(*) FROM video_licenses WHERE status='VALID' AND (expiry_date IS NULL OR expiry_date >= CURDATE())"));
        m.put("expiring30", count(
            "SELECT " +
            "(SELECT COUNT(*) FROM licenses WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(),INTERVAL 30 DAY)) +" +
            "(SELECT COUNT(*) FROM book_licenses WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(),INTERVAL 30 DAY)) +" +
            "(SELECT COUNT(*) FROM video_licenses WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(),INTERVAL 30 DAY))"));
        return m;
    }
}
