package vn.edu.eaut.library.dao;

import vn.edu.eaut.library.model.PermissionRequest;
import vn.edu.eaut.library.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionRequestDAO {
    private static final String BASE_SELECT =
            "SELECT r.*, u.username, u.full_name AS user_full_name, " +
            "d.title AS document_title, p.full_name AS processed_by_name " +
            "FROM permission_requests r " +
            "JOIN users u ON r.user_id=u.user_id " +
            "JOIN documents d ON r.document_id=d.document_id " +
            "LEFT JOIN users p ON r.processed_by=p.user_id ";

    public List<PermissionRequest> findAll() {
        return find(BASE_SELECT + "ORDER BY r.requested_date DESC", null);
    }

    public List<PermissionRequest> findByUserId(int userId) {
        return find(BASE_SELECT + "WHERE r.user_id=? ORDER BY r.requested_date DESC", userId);
    }

    public boolean hasPending(int userId, int documentId, String permissionType) {
        String sql = "SELECT COUNT(*) FROM permission_requests WHERE user_id=? AND document_id=? AND permission_type=? AND status='PENDING'";
        try (Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,userId); ps.setInt(2,documentId); ps.setString(3,permissionType);
            try(ResultSet rs=ps.executeQuery()){ return rs.next() && rs.getInt(1)>0; }
        } catch(SQLException e){ throw new RuntimeException("Lỗi kiểm tra yêu cầu cấp quyền",e); }
    }

    public boolean insert(PermissionRequest r) {
        String sql="INSERT INTO permission_requests (user_id,document_id,permission_type,reason,status) VALUES (?,?,?,?, 'PENDING')";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,r.getUserId()); ps.setInt(2,r.getDocumentId()); ps.setString(3,r.getPermissionType()); ps.setString(4,r.getReason());
            return ps.executeUpdate()>0;
        }catch(SQLException e){ throw new RuntimeException("Lỗi tạo yêu cầu cấp quyền",e); }
    }

    public PermissionRequest findById(int id) {
        List<PermissionRequest> list=find(BASE_SELECT+"WHERE r.request_id=?",id);
        return list.isEmpty()?null:list.get(0);
    }

    public boolean approve(int requestId, int processedBy, Timestamp expiryDate) {
        String update="UPDATE permission_requests SET status='APPROVED', processed_date=NOW(), processed_by=? WHERE request_id=? AND status='PENDING'";
        String upsert="INSERT INTO permissions (user_id,document_id,permission_type,granted_by,granted_date,expiry_date) VALUES (?,?,?,?,NOW(),?) " +
                "ON DUPLICATE KEY UPDATE granted_by=VALUES(granted_by), granted_date=NOW(), expiry_date=VALUES(expiry_date)";
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement ps=c.prepareStatement("SELECT user_id,document_id,permission_type FROM permission_requests WHERE request_id=? AND status='PENDING' FOR UPDATE")){
                ps.setInt(1,requestId);
                try(ResultSet rs=ps.executeQuery()){
                    if(!rs.next()){c.rollback();return false;}
                    int userId=rs.getInt("user_id"), documentId=rs.getInt("document_id"); String type=rs.getString("permission_type");
                    try(PreparedStatement up=c.prepareStatement(upsert); PreparedStatement st=c.prepareStatement(update)){
                        up.setInt(1,userId); up.setInt(2,documentId); up.setString(3,type); up.setInt(4,processedBy);
                        if(expiryDate==null) up.setNull(5,Types.TIMESTAMP); else up.setTimestamp(5,expiryDate);
                        up.executeUpdate();
                        st.setInt(1,processedBy); st.setInt(2,requestId); st.executeUpdate();
                    }
                }
            }
            c.commit(); return true;
        }catch(SQLException e){ throw new RuntimeException("Lỗi duyệt yêu cầu cấp quyền",e); }
    }

    public boolean reject(int requestId, int processedBy) {
        String sql="UPDATE permission_requests SET status='REJECTED', processed_date=NOW(), processed_by=? WHERE request_id=? AND status='PENDING'";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,processedBy); ps.setInt(2,requestId); return ps.executeUpdate()>0;
        }catch(SQLException e){ throw new RuntimeException("Lỗi từ chối yêu cầu cấp quyền",e); }
    }

    private List<PermissionRequest> find(String sql, Integer param){
        List<PermissionRequest> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            if(param!=null) ps.setInt(1,param);
            try(ResultSet rs=ps.executeQuery()){while(rs.next()) list.add(mapRow(rs));}
        }catch(SQLException e){throw new RuntimeException("Lỗi truy vấn yêu cầu cấp quyền",e);}
        return list;
    }

    private PermissionRequest mapRow(ResultSet rs)throws SQLException{
        PermissionRequest r=new PermissionRequest();
        r.setRequestId(rs.getInt("request_id")); r.setUserId(rs.getInt("user_id")); r.setUsername(rs.getString("username"));
        r.setUserFullName(rs.getString("user_full_name")); r.setDocumentId(rs.getInt("document_id")); r.setDocumentTitle(rs.getString("document_title"));
        r.setPermissionType(rs.getString("permission_type")); r.setReason(rs.getString("reason")); r.setStatus(rs.getString("status"));
        Timestamp t=rs.getTimestamp("requested_date"); if(t!=null) r.setRequestedDate(t.toLocalDateTime());
        t=rs.getTimestamp("processed_date"); if(t!=null) r.setProcessedDate(t.toLocalDateTime());
        int p=rs.getInt("processed_by"); if(!rs.wasNull()) r.setProcessedBy(p); r.setProcessedByName(rs.getString("processed_by_name"));
        t=rs.getTimestamp("expiry_date"); if(t!=null) r.setExpiryDate(t.toLocalDateTime());
        return r;
    }
}
