package vn.edu.eaut.library.dao;

import vn.edu.eaut.library.model.AccessHistory;
import vn.edu.eaut.library.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryDAO {

    private static final String BASE_SELECT =
            "SELECT h.*, u.username, d.title AS document_title " +
            "FROM access_history h " +
            "LEFT JOIN users u ON h.user_id = u.user_id " +
            "LEFT JOIN documents d ON h.document_id = d.document_id ";

    public List<AccessHistory> findAll() {
        List<AccessHistory> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY h.access_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy lịch sử truy cập", e);
        }
        return list;
    }

    public List<AccessHistory> findByUserId(int userId) {
        List<AccessHistory> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE h.user_id = ? ORDER BY h.access_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy lịch sử truy cập theo user", e);
        }
        return list;
    }

    public List<AccessHistory> findByDocumentId(int documentId) {
        List<AccessHistory> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE h.document_id = ? ORDER BY h.access_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy lịch sử truy cập theo tài liệu", e);
        }
        return list;
    }

    public List<AccessHistory> search(Integer userId, Integer documentId, String actionType, String fromDate, String toDate) {
        List<AccessHistory> list=new ArrayList<>();
        StringBuilder sql=new StringBuilder(BASE_SELECT+"WHERE 1=1 ");
        if(userId!=null)sql.append("AND h.user_id=? "); if(documentId!=null)sql.append("AND h.document_id=? ");
        if(actionType!=null&&!actionType.trim().isEmpty())sql.append("AND h.action_type=? ");
        if(fromDate!=null&&!fromDate.isEmpty())sql.append("AND DATE(h.access_time)>=? "); if(toDate!=null&&!toDate.isEmpty())sql.append("AND DATE(h.access_time)<=? ");
        sql.append("ORDER BY h.access_time DESC");
        try(Connection conn=DBConnection.getConnection();PreparedStatement ps=conn.prepareStatement(sql.toString())){
            int i=1; if(userId!=null)ps.setInt(i++,userId); if(documentId!=null)ps.setInt(i++,documentId); if(actionType!=null&&!actionType.trim().isEmpty())ps.setString(i++,actionType);
            if(fromDate!=null&&!fromDate.isEmpty())ps.setDate(i++,Date.valueOf(fromDate)); if(toDate!=null&&!toDate.isEmpty())ps.setDate(i++,Date.valueOf(toDate));
            try(ResultSet rs=ps.executeQuery()){while(rs.next())list.add(mapRow(rs));}
        }catch(Exception e){throw new RuntimeException("Lỗi lọc lịch sử truy cập",e);} return list;
    }

    public boolean insert(AccessHistory history) {
        String sql = "INSERT INTO access_history (user_id, document_id, action_type, ip_address) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, history.getUserId());
            if (history.getDocumentId() != null) {
                ps.setInt(2, history.getDocumentId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, history.getActionType());
            ps.setString(4, history.getIpAddress());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi ghi lịch sử truy cập", e);
        }
    }

    private AccessHistory mapRow(ResultSet rs) throws SQLException {
        AccessHistory h = new AccessHistory();
        h.setHistoryId(rs.getInt("history_id"));
        h.setUserId(rs.getInt("user_id"));
        h.setUsername(rs.getString("username"));
        int docId = rs.getInt("document_id");
        h.setDocumentId(rs.wasNull() ? null : docId);
        h.setDocumentTitle(rs.getString("document_title"));
        h.setActionType(rs.getString("action_type"));
        Timestamp ts = rs.getTimestamp("access_time");
        if (ts != null) {
            h.setAccessTime(ts.toLocalDateTime());
        }
        h.setIpAddress(rs.getString("ip_address"));
        return h;
    }
}
