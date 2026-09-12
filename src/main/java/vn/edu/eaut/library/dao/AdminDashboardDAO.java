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

public class AdminDashboardDAO {
    private int count(String sql) throws Exception {
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql);ResultSet rs=ps.executeQuery()){
            return rs.next()?rs.getInt(1):0;
        }
    }
    public Map<String,Integer> getOverview() throws Exception {
        Map<String,Integer> data=new LinkedHashMap<>();
        data.put("accessHistory",count("SELECT COUNT(*) FROM access_history"));
        data.put("auditLogs",safeCount("SELECT COUNT(*) FROM audit_logs"));
        data.put("failedLogins",safeCount("SELECT COUNT(*) FROM audit_logs WHERE action_type IN ('LOGIN_FAILED','LOGIN_BLOCKED') AND created_at>=DATE_SUB(NOW(),INTERVAL 24 HOUR)"));
        return data;
    }
    public List<Map<String, Object>> accessLast7Days() {

        Map<LocalDate, Integer> totals = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            totals.put(today.minusDays(i), 0);
        }

        String sql =
                "SELECT DATE(access_time) AS day, COUNT(*) AS total " +
                "FROM access_history " +
                "WHERE access_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                "GROUP BY DATE(access_time) " +
                "ORDER BY day";

        try (
                Connection c = DBConnection.getConnection();
                PreparedStatement p = c.prepareStatement(sql);
                ResultSet r = p.executeQuery()
        ) {

            while (r.next()) {

                java.sql.Date d = r.getDate("day");

                if (d != null) {
                    totals.put(
                            d.toLocalDate(),
                            r.getInt("total")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải biểu đồ truy cập", e);
        }

        List<Map<String, Object>> list = new ArrayList<>();

        for (Map.Entry<LocalDate, Integer> e : totals.entrySet()) {

            Map<String, Object> m = new LinkedHashMap<>();

            m.put("day", e.getKey());
            m.put("total", e.getValue());

            list.add(m);
        }

        return list;
    }
    public List<Map<String,Object>> recentSecurityEvents(){
        List<Map<String,Object>> list=new ArrayList<>();
        String sql="SELECT a.*,u.username FROM audit_logs a JOIN users u ON a.user_id=u.user_id WHERE a.action_type IN ('LOGIN_FAILED','LOGIN_BLOCKED') ORDER BY a.created_at DESC LIMIT 10";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            while(r.next()){Map<String,Object> m=new LinkedHashMap<>();m.put("username",r.getString("username"));m.put("action",r.getString("action_type"));m.put("description",r.getString("description"));m.put("ip",r.getString("ip_address"));m.put("createdAt",r.getTimestamp("created_at").toLocalDateTime());list.add(m);}
        }catch(SQLException e){throw new RuntimeException("Lỗi tải sự kiện bảo mật",e);}
        return list;
    }
    private int safeCount(String sql){try{return count(sql);}catch(Exception e){return 0;}}
}
