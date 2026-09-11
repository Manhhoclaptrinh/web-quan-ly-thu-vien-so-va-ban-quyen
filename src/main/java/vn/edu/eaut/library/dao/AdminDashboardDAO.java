package vn.edu.eaut.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import vn.edu.eaut.library.utils.DBConnection;

public class AdminDashboardDAO {

    private int count(String sql) throws Exception {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public Map<String, Integer> getOverview() throws Exception {
        Map<String, Integer> data = new LinkedHashMap<>();
        data.put("users", count("SELECT COUNT(*) FROM users"));
        data.put("activeUsers", count("SELECT COUNT(*) FROM users WHERE status='ACTIVE'"));
        data.put("documents", count("SELECT COUNT(*) FROM documents"));
        data.put("videos", count("SELECT COUNT(*) FROM videos"));
        data.put("books", count("SELECT COUNT(*) FROM books"));
        data.put("accessHistory", count("SELECT COUNT(*) FROM access_history"));
        data.put("auditLogs", safeCount("SELECT COUNT(*) FROM audit_logs"));
        data.put("permissions", safeCount("SELECT COUNT(*) FROM permissions")
                + safeCount("SELECT COUNT(*) FROM video_permissions")
                + safeCount("SELECT COUNT(*) FROM book_permissions"));
        data.put("permissionRequests", safeCount("SELECT COUNT(*) FROM permission_requests")
                + safeCount("SELECT COUNT(*) FROM video_permission_requests")
                + safeCount("SELECT COUNT(*) FROM book_permission_requests"));
        data.put("licenses", safeCount("SELECT COUNT(*) FROM licenses")
                + safeCount("SELECT COUNT(*) FROM video_licenses")
                + safeCount("SELECT COUNT(*) FROM book_licenses"));
        return data;
    }

    private int safeCount(String sql) {
        try { return count(sql); } catch (Exception e) { return 0; }
    }
}
