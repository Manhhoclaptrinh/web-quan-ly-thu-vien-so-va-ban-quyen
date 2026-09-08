package vn.edu.eaut.library.dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
 import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import vn.edu.eaut.library.model.Favorite;
import vn.edu.eaut.library.utils.DBConnection;
public class FavoriteDAO {
 public boolean exists(int userId,int documentId){String s="SELECT COUNT(*) FROM favorites WHERE user_id=? AND document_id=? AND item_type='DOCUMENT'";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,userId);p.setInt(2,documentId);try(ResultSet r=p.executeQuery()){return r.next()&&r.getInt(1)>0;}}catch(SQLException e){throw new RuntimeException("Lỗi kiểm tra yêu thích",e);}}
 public boolean add(int userId,int documentId){String s="INSERT IGNORE INTO favorites(user_id,item_type,document_id) VALUES(?, 'DOCUMENT', ?)";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,userId);p.setInt(2,documentId);return p.executeUpdate()>0;}catch(SQLException e){throw new RuntimeException("Lỗi thêm yêu thích",e);}}
 public boolean remove(int userId,int documentId){String s="DELETE FROM favorites WHERE user_id=? AND document_id=? AND item_type='DOCUMENT'";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,userId);p.setInt(2,documentId);return p.executeUpdate()>0;}catch(SQLException e){throw new RuntimeException("Lỗi xóa yêu thích",e);}}

 public boolean existsVideo(int userId,int videoId){String s="SELECT COUNT(*) FROM favorites WHERE user_id=? AND video_id=? AND item_type='VIDEO'";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,userId);p.setInt(2,videoId);try(ResultSet r=p.executeQuery()){return r.next()&&r.getInt(1)>0;}}catch(SQLException e){throw new RuntimeException("Lỗi kiểm tra yêu thích video",e);}}
 public boolean addVideo(int userId,int videoId){String s="INSERT IGNORE INTO favorites(user_id,item_type,video_id) VALUES(?, 'VIDEO', ?)";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,userId);p.setInt(2,videoId);return p.executeUpdate()>0;}catch(SQLException e){throw new RuntimeException("Lỗi thêm yêu thích video",e);}}
 public boolean removeVideo(int userId,int videoId){String s="DELETE FROM favorites WHERE user_id=? AND video_id=? AND item_type='VIDEO'";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,userId);p.setInt(2,videoId);return p.executeUpdate()>0;}catch(SQLException e){throw new RuntimeException("Lỗi xóa yêu thích video",e);}}

 public List<Favorite> findByUserId(int userId){
    List<Favorite> l=new ArrayList<>();
    String s="SELECT f.*, d.title AS document_title, d.author AS author FROM favorites f JOIN documents d ON f.document_id=d.document_id WHERE f.user_id=? AND f.item_type='DOCUMENT' " +
             "UNION ALL " +
             "SELECT f.*, vv.title AS document_title, vv.author AS author FROM favorites f JOIN videos vv ON f.video_id=vv.video_id WHERE f.user_id=? AND f.item_type='VIDEO' " +
             "ORDER BY created_at DESC";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){
        p.setInt(1,userId); p.setInt(2,userId);
        try(ResultSet r=p.executeQuery()){while(r.next())l.add(map(r));}
    }catch(SQLException e){throw new RuntimeException("Lỗi lấy danh sách yêu thích",e);}
    return l;
 }
 private Favorite map(ResultSet r)throws SQLException{
    Favorite f=new Favorite();
    f.setFavoriteId(r.getInt("favorite_id"));
    f.setUserId(r.getInt("user_id"));
    f.setItemType(r.getString("item_type"));
    int docId=r.getInt("document_id"); if(!r.wasNull()) f.setDocumentId(docId);
    int vidId=r.getInt("video_id"); if(!r.wasNull()) f.setVideoId(vidId);
    f.setDocumentTitle(r.getString("document_title"));
    f.setAuthor(r.getString("author"));
    Timestamp t=r.getTimestamp("created_at");if(t!=null)f.setCreatedAt(t.toLocalDateTime());
    return f;
 }
}