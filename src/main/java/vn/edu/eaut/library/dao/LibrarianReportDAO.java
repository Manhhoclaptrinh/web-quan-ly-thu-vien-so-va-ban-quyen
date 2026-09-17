package vn.edu.eaut.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vn.edu.eaut.library.utils.DBConnection;

/**
 * Báo cáo nghiệp vụ dành cho LIBRARIAN.
 * Chỉ đọc dữ liệu và tổng hợp trực tiếp từ các bảng nghiệp vụ.
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

    private long sum(String sql, Object... params) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) p.setObject(i + 1, params[i]);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tổng hợp số liệu", e);
        }
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
        m.put("reviews", count("SELECT COUNT(*) FROM document_reviews") + count("SELECT COUNT(*) FROM video_reviews"));
        m.put("pendingRequests", count(
                "SELECT (SELECT COUNT(*) FROM permission_requests WHERE status='PENDING') " +
                "+ (SELECT COUNT(*) FROM video_permission_requests WHERE status='PENDING')"));
        return m;
    }

    /** Hoạt động theo ngày, tự lấp ngày không có dữ liệu bằng 0. */
    public List<Map<String, Object>> accessByDate(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Map<String, Object>> data = new LinkedHashMap<>();

        LocalDate d = startDate;
        while (!d.isAfter(endDate)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("day", d.toString());
            row.put("views", 0);
            row.put("downloads", 0);
            row.put("total", 0);
            data.put(d.toString(), row);
            d = d.plusDays(1);
        }

        String sql =
                "SELECT DATE(access_time) day, " +
                "SUM(CASE WHEN action_type='VIEW' THEN 1 ELSE 0 END) views, " +
                "SUM(CASE WHEN action_type='DOWNLOAD' THEN 1 ELSE 0 END) downloads, " +
                "COUNT(*) total " +
                "FROM access_history " +
                "WHERE access_time >= ? AND access_time < DATE_ADD(?, INTERVAL 1 DAY) " +
                "AND action_type IN ('VIEW','DOWNLOAD') " +
                "GROUP BY DATE(access_time) ORDER BY day";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setObject(1, startDate);
            p.setObject(2, endDate);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    String day = r.getDate("day").toLocalDate().toString();
                    Map<String, Object> row = data.get(day);
                    if (row != null) {
                        row.put("views", r.getInt("views"));
                        row.put("downloads", r.getInt("downloads"));
                        row.put("total", r.getInt("total"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thống kê hoạt động theo ngày", e);
        }
        return new ArrayList<>(data.values());
    }

    /** Giữ tương thích với code cũ. */
    public List<Map<String, Object>> accessLast7Days() {
        return accessByDate(LocalDate.now().minusDays(6), LocalDate.now());
    }

    /** Báo cáo 5 năm theo năm. */
    public List<Map<String, Object>> yearlyReport(LocalDate endDate) {
        LocalDate firstYear = LocalDate.of(endDate.getYear() - 4, 1, 1);
        LocalDate nextDay = endDate.plusDays(1);
        List<Map<String, Object>> list = new ArrayList<>();

        String sql =
                "SELECT YEAR(access_time) report_year, " +
                "SUM(CASE WHEN action_type='VIEW' THEN 1 ELSE 0 END) views, " +
                "SUM(CASE WHEN action_type='DOWNLOAD' THEN 1 ELSE 0 END) downloads, " +
                "COUNT(*) total " +
                "FROM access_history " +
                "WHERE access_time >= ? AND access_time < ? " +
                "AND action_type IN ('VIEW','DOWNLOAD') " +
                "GROUP BY YEAR(access_time) ORDER BY report_year";

        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setObject(1, firstYear);
            p.setObject(2, nextDay);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("year", r.getInt("report_year"));
                    row.put("views", r.getInt("views"));
                    row.put("downloads", r.getInt("downloads"));
                    row.put("total", r.getInt("total"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi báo cáo hoạt động 5 năm", e);
        }
        return list;
    }

    /** Báo cáo 12 tháng của một năm. */
    public List<Map<String, Object>> monthlyReport(int year) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", month);
            row.put("views", 0);
            row.put("downloads", 0);
            row.put("total", 0);
            list.add(row);
        }

        String sql =
                "SELECT MONTH(access_time) report_month, " +
                "SUM(CASE WHEN action_type='VIEW' THEN 1 ELSE 0 END) views, " +
                "SUM(CASE WHEN action_type='DOWNLOAD' THEN 1 ELSE 0 END) downloads, " +
                "COUNT(*) total " +
                "FROM access_history WHERE YEAR(access_time)=? " +
                "AND action_type IN ('VIEW','DOWNLOAD') " +
                "GROUP BY MONTH(access_time) ORDER BY report_month";

        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, year);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    int month = r.getInt("report_month");
                    Map<String, Object> row = list.get(month - 1);
                    row.put("views", r.getInt("views"));
                    row.put("downloads", r.getInt("downloads"));
                    row.put("total", r.getInt("total"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi báo cáo hoạt động theo tháng", e);
        }
        return list;
    }

    public List<Map<String, Object>> contentByType() {
        List<Map<String, Object>> list = new ArrayList<>();
        addType(list, "Tài liệu", count("SELECT COUNT(*) FROM documents"), count("SELECT COUNT(*) FROM documents WHERE status='AVAILABLE'"));
        addType(list, "Sách", count("SELECT COUNT(*) FROM books"), count("SELECT COUNT(*) FROM books WHERE status='AVAILABLE'"));
        addType(list, "Video", count("SELECT COUNT(*) FROM videos"), count("SELECT COUNT(*) FROM videos WHERE status='AVAILABLE'"));
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

    public List<Map<String, Object>> topContent(int limit, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql =
                "SELECT title, content_type, total FROM (" +
                " SELECT d.title, 'Tài liệu' content_type, COUNT(h.history_id) total " +
                " FROM documents d JOIN access_history h ON h.document_id=d.document_id " +
                " WHERE h.action_type IN ('VIEW','DOWNLOAD') AND h.access_time >= ? AND h.access_time < DATE_ADD(?, INTERVAL 1 DAY) " +
                " GROUP BY d.document_id,d.title " +
                " UNION ALL " +
                " SELECT b.title, 'Sách' content_type, COUNT(h.history_id) total " +
                " FROM books b JOIN access_history h ON h.book_id=b.book_id " +
                " WHERE h.action_type IN ('VIEW','DOWNLOAD') AND h.access_time >= ? AND h.access_time < DATE_ADD(?, INTERVAL 1 DAY) " +
                " GROUP BY b.book_id,b.title " +
                " UNION ALL " +
                " SELECT v.title, 'Video' content_type, COUNT(h.history_id) total " +
                " FROM videos v JOIN access_history h ON h.video_id=v.video_id " +
                " WHERE h.action_type IN ('VIEW','DOWNLOAD') AND h.access_time >= ? AND h.access_time < DATE_ADD(?, INTERVAL 1 DAY) " +
                " GROUP BY v.video_id,v.title " +
                ") x ORDER BY total DESC LIMIT ?";

        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setObject(1, startDate); p.setObject(2, endDate);
            p.setObject(3, startDate); p.setObject(4, endDate);
            p.setObject(5, startDate); p.setObject(6, endDate);
            p.setInt(7, Math.max(1, Math.min(limit, 20)));
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

    /** Tương thích với code cũ. */
    public List<Map<String, Object>> topContent(int limit) {
        return topContent(limit, LocalDate.of(2000, 1, 1), LocalDate.now());
    }

    public List<Map<String, Object>> categoryStats() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql =
                "SELECT c.category_name, " +
                "(SELECT COUNT(*) FROM documents d WHERE d.category_id=c.category_id) documents, " +
                "(SELECT COUNT(*) FROM books b WHERE b.category_id=c.category_id) books, " +
                "(SELECT COUNT(*) FROM videos v WHERE v.category_id=c.category_id) videos " +
                "FROM categories c ORDER BY c.category_name";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
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

    public Map<String, Object> transactionStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> m = new LinkedHashMap<>();
        String from = " created_at >= ? AND created_at < DATE_ADD(?, INTERVAL 1 DAY)";
        m.put("transactions", countWithDates("SELECT COUNT(*) FROM transactions WHERE" + from, startDate, endDate));
        m.put("topups", countWithDates("SELECT COUNT(*) FROM transactions WHERE type='TOPUP' AND" + from, startDate, endDate));
        m.put("downloads", countWithDates("SELECT COUNT(*) FROM transactions WHERE type='DOWNLOAD' AND" + from, startDate, endDate));
        m.put("views", countWithDates("SELECT COUNT(*) FROM transactions WHERE type='VIEW_PDF' AND" + from, startDate, endDate));
        m.put("memberships", countWithDates("SELECT COUNT(*) FROM memberships WHERE created_at >= ? AND created_at < DATE_ADD(?, INTERVAL 1 DAY)", startDate, endDate));
        m.put("revenue", sum("SELECT COALESCE(SUM(CASE WHEN amount < 0 THEN -amount ELSE 0 END),0) FROM transactions WHERE" + from, startDate, endDate));
        return m;
    }

    private int countWithDates(String sql, LocalDate startDate, LocalDate endDate) {
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setObject(1, startDate); p.setObject(2, endDate);
            try (ResultSet r = p.executeQuery()) { return r.next() ? r.getInt(1) : 0; }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thống kê theo khoảng thời gian", e);
        }
    }

    /** Tương thích với code cũ. */
    public Map<String, Object> transactionStats() {
        return transactionStats(LocalDate.of(2000, 1, 1), LocalDate.now());
    }

    public Map<String, Integer> licenseStats() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("documents", count("SELECT COUNT(*) FROM licenses WHERE status='VALID' AND (expiry_date IS NULL OR expiry_date >= CURDATE())"));
        m.put("books", count("SELECT COUNT(*) FROM book_licenses WHERE status='VALID' AND (expiry_date IS NULL OR expiry_date >= CURDATE())"));
        m.put("videos", count("SELECT COUNT(*) FROM video_licenses WHERE status='VALID' AND (expiry_date IS NULL OR expiry_date >= CURDATE())"));
        m.put("expired", count(
                "SELECT (SELECT COUNT(*) FROM licenses WHERE expiry_date < CURDATE()) + " +
                "(SELECT COUNT(*) FROM book_licenses WHERE expiry_date < CURDATE()) + " +
                "(SELECT COUNT(*) FROM video_licenses WHERE expiry_date < CURDATE())"));
        m.put("expiring30", count(
                "SELECT (SELECT COUNT(*) FROM licenses WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(),INTERVAL 30 DAY)) +" +
                "(SELECT COUNT(*) FROM book_licenses WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(),INTERVAL 30 DAY)) +" +
                "(SELECT COUNT(*) FROM video_licenses WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(),INTERVAL 30 DAY))"));
        return m;
    }
}
