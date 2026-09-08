package vn.edu.eaut.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import vn.edu.eaut.library.model.VideoPermissionRequest;
import vn.edu.eaut.library.utils.DBConnection;

public class VideoPermissionRequestDAO {
    private static final String BASE_SELECT =
            "SELECT r.*, u.username, u.full_name AS user_full_name, " +
            "v.title AS video_title, p.full_name AS processed_by_name " +
            "FROM video_permission_requests r " +
            "JOIN users u ON r.user_id=u.user_id " +
            "JOIN videos v ON r.video_id=v.video_id " +
            "LEFT JOIN users p ON r.processed_by=p.user_id ";

    public List<VideoPermissionRequest> findAll() {
        return find(BASE_SELECT + "ORDER BY r.requested_date DESC", null);
    }

    public List<VideoPermissionRequest> findByUserId(int userId) {
        return find(BASE_SELECT + "WHERE r.user_id=? ORDER BY r.requested_date DESC", userId);
    }

    public boolean hasPending(int userId, int videoId, String permissionType) {
        String sql = "SELECT COUNT(*) FROM video_permission_requests WHERE user_id=? AND video_id=? AND permission_type=? AND status='PENDING'";
        try (Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,userId); ps.setInt(2,videoId); ps.setString(3,permissionType);
            try(ResultSet rs=ps.executeQuery()){ return rs.next() && rs.getInt(1)>0; }
        } catch(SQLException e){ throw new RuntimeException("Lỗi kiểm tra yêu cầu cấp quyền video",e); }
    }

    public boolean insert(VideoPermissionRequest r) {
        String sql="INSERT INTO video_permission_requests (user_id,video_id,permission_type,reason,status) VALUES (?,?,?,?, 'PENDING')";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,r.getUserId()); ps.setInt(2,r.getVideoId()); ps.setString(3,r.getPermissionType()); ps.setString(4,r.getReason());
            return ps.executeUpdate()>0;
        }catch(SQLException e){ throw new RuntimeException("Lỗi tạo yêu cầu cấp quyền video",e); }
    }

    public VideoPermissionRequest findById(int id) {
        List<VideoPermissionRequest> list=find(BASE_SELECT+"WHERE r.request_id=?",id);
        return list.isEmpty()?null:list.get(0);
    }

    public boolean approve(int requestId, int processedBy, Timestamp expiryDate) {
        String update="UPDATE video_permission_requests SET status='APPROVED', processed_date=NOW(), processed_by=? WHERE request_id=? AND status='PENDING'";
        String upsert="INSERT INTO video_permissions (user_id,video_id,permission_type,granted_by,granted_date,expiry_date) VALUES (?,?,?,?,NOW(),?) " +
                "ON DUPLICATE KEY UPDATE granted_by=VALUES(granted_by), granted_date=NOW(), expiry_date=VALUES(expiry_date)";
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement ps=c.prepareStatement("SELECT user_id,video_id,permission_type FROM video_permission_requests WHERE request_id=? AND status='PENDING' FOR UPDATE")){
                ps.setInt(1,requestId);
                try(ResultSet rs=ps.executeQuery()){
                    if(!rs.next()){c.rollback();return false;}
                    int userId=rs.getInt("user_id"), videoId=rs.getInt("video_id"); String type=rs.getString("permission_type");
                    try(PreparedStatement up=c.prepareStatement(upsert); PreparedStatement st=c.prepareStatement(update)){
                        up.setInt(1,userId); up.setInt(2,videoId); up.setString(3,type); up.setInt(4,processedBy);
                        if(expiryDate==null) up.setNull(5,Types.TIMESTAMP); else up.setTimestamp(5,expiryDate);
                        up.executeUpdate();
                        st.setInt(1,processedBy); st.setInt(2,requestId); st.executeUpdate();
                    }
                }
            }
            c.commit(); return true;
        }catch(SQLException e){ throw new RuntimeException("Lỗi duyệt yêu cầu cấp quyền video",e); }
    }

    public boolean reject(int requestId, int processedBy) {
        String sql="UPDATE video_permission_requests SET status='REJECTED', processed_date=NOW(), processed_by=? WHERE request_id=? AND status='PENDING'";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,processedBy); ps.setInt(2,requestId); return ps.executeUpdate()>0;
        }catch(SQLException e){ throw new RuntimeException("Lỗi từ chối yêu cầu cấp quyền video",e); }
    }

    private List<VideoPermissionRequest> find(String sql, Integer param){
        List<VideoPermissionRequest> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            if(param!=null) ps.setInt(1,param);
            try(ResultSet rs=ps.executeQuery()){while(rs.next()) list.add(mapRow(rs));}
        }catch(SQLException e){throw new RuntimeException("Lỗi truy vấn yêu cầu cấp quyền video",e);}
        return list;
    }

    private VideoPermissionRequest mapRow(ResultSet rs)throws SQLException{
        VideoPermissionRequest r=new VideoPermissionRequest();
        r.setRequestId(rs.getInt("request_id")); r.setUserId(rs.getInt("user_id")); r.setUsername(rs.getString("username"));
        r.setUserFullName(rs.getString("user_full_name")); r.setVideoId(rs.getInt("video_id")); r.setVideoTitle(rs.getString("video_title"));
        r.setPermissionType(rs.getString("permission_type")); r.setReason(rs.getString("reason")); r.setStatus(rs.getString("status"));
        Timestamp t=rs.getTimestamp("requested_date"); if(t!=null) r.setRequestedDate(t.toLocalDateTime());
        t=rs.getTimestamp("processed_date"); if(t!=null) r.setProcessedDate(t.toLocalDateTime());
        int p=rs.getInt("processed_by"); if(!rs.wasNull()) r.setProcessedBy(p); r.setProcessedByName(rs.getString("processed_by_name"));
        t=rs.getTimestamp("expiry_date"); if(t!=null) r.setExpiryDate(t.toLocalDateTime());
        return r;
    }
}