package vn.edu.eaut.library.dao;

import vn.edu.eaut.library.utils.DBConnection;

import java.sql.*;

/** Cung cấp các số liệu tổng hợp cho Dashboard. */
public class DashboardDAO {

    public int countDocuments() {
        return count("SELECT COUNT(*) FROM documents");
    }

    public int countAvailableDocuments() {
        return count("SELECT COUNT(*) FROM documents WHERE status = 'AVAILABLE'");
    }

    public int countVideos() {
        return count("SELECT COUNT(*) FROM videos");
    }

    public int countAvailableVideos() {
        return count("SELECT COUNT(*) FROM videos WHERE status = 'AVAILABLE'");
    }

    public int countValidVideoLicenses() {
        return count("SELECT COUNT(*) FROM video_licenses WHERE status = 'VALID' AND (expiry_date IS NULL OR expiry_date >= CURDATE())");
    }

    public int countCategories() {
        return count("SELECT COUNT(*) FROM categories");
    }

    public int countUsers() {
        return count("SELECT COUNT(*) FROM users");
    }

    public int countActiveUsers() {
        return count("SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'");
    }

    public int countPermissions() {
        return count("SELECT COUNT(*) FROM permissions");
    }

    public int countValidLicenses() {
        return count("SELECT COUNT(*) FROM licenses WHERE status = 'VALID' AND (expiry_date IS NULL OR expiry_date >= CURDATE())");
    }

    public int countDownloads() {
        return count("SELECT COUNT(*) FROM access_history WHERE action_type = 'DOWNLOAD'");
    }

    public int countViews() {
        return count("SELECT COUNT(*) FROM access_history WHERE action_type = 'VIEW'");
    }

    public java.util.Map<String,Integer> documentsByCategory() {
        java.util.Map<String,Integer> map=new java.util.LinkedHashMap<>();
        String sql="SELECT COALESCE(c.category_name,'Chưa phân loại'), COUNT(*) FROM documents d LEFT JOIN categories c ON d.category_id=c.category_id GROUP BY c.category_id,c.category_name ORDER BY COUNT(*) DESC";
        try(Connection conn=DBConnection.getConnection();PreparedStatement ps=conn.prepareStatement(sql);ResultSet rs=ps.executeQuery()){while(rs.next())map.put(rs.getString(1),rs.getInt(2));}
        catch(SQLException e){throw new RuntimeException("Lỗi thống kê tài liệu theo danh mục",e);} return map;
    }
    public java.util.Map<String,Integer> activitySummary() {
        java.util.Map<String,Integer> map=new java.util.LinkedHashMap<>(); String sql="SELECT action_type,COUNT(*) FROM access_history GROUP BY action_type ORDER BY COUNT(*) DESC";
        try(Connection conn=DBConnection.getConnection();PreparedStatement ps=conn.prepareStatement(sql);ResultSet rs=ps.executeQuery()){while(rs.next())map.put(rs.getString(1),rs.getInt(2));}
        catch(SQLException e){throw new RuntimeException("Lỗi thống kê hoạt động",e);} return map;
    }
    public java.util.List<java.util.Map<String,Object>> topDocuments(int limit) {
        java.util.List<java.util.Map<String,Object>> list=new java.util.ArrayList<>();
        // Gộp cả tài liệu và video để bảng "Được truy cập nhiều nhất" phản ánh đúng toàn hệ thống.
        String sql="SELECT title,SUM(total) AS total FROM (" +
                "SELECT d.title AS title, COUNT(h.history_id) AS total FROM access_history h JOIN documents d ON h.document_id=d.document_id " +
                "WHERE h.action_type IN ('VIEW','DOWNLOAD') AND (h.target_type IS NULL OR h.target_type='DOCUMENT') GROUP BY d.document_id,d.title " +
                "UNION ALL " +
                "SELECT CONCAT(v.title,' (Video)') AS title, COUNT(h.history_id) AS total FROM access_history h JOIN videos v ON h.video_id=v.video_id " +
                "WHERE h.action_type IN ('VIEW','DOWNLOAD') AND h.target_type='VIDEO' GROUP BY v.video_id,v.title" +
                ") combined GROUP BY title ORDER BY total DESC LIMIT ?";
        try(Connection conn=DBConnection.getConnection();PreparedStatement ps=conn.prepareStatement(sql)){ps.setInt(1,Math.max(1,Math.min(limit,20)));try(ResultSet rs=ps.executeQuery()){while(rs.next()){java.util.Map<String,Object> m=new java.util.HashMap<>();m.put("title",rs.getString(1));m.put("total",rs.getInt(2));list.add(m);}}}
        catch(SQLException e){throw new RuntimeException("Lỗi thống kê tài liệu phổ biến",e);} return list;
    }

    private int count(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy dữ liệu Dashboard", e);
        }
        return 0;
    }
}
