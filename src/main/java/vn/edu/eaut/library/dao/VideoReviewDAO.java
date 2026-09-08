package vn.edu.eaut.library.dao;
import java.sql.Connection;
 import java.sql.PreparedStatement;
import java.sql.ResultSet;
 import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import vn.edu.eaut.library.model.VideoReview;
import vn.edu.eaut.library.utils.DBConnection;
public class VideoReviewDAO {
 public List<VideoReview> findByVideoId(int videoId){List<VideoReview> l=new ArrayList<>();String s="SELECT r.*,u.username,u.full_name AS user_full_name FROM video_reviews r JOIN users u ON r.user_id=u.user_id WHERE r.video_id=? ORDER BY r.created_at DESC";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,videoId);try(ResultSet rs=p.executeQuery()){while(rs.next())l.add(map(rs));}}catch(SQLException e){throw new RuntimeException("Lỗi lấy đánh giá video",e);}return l;}
 public Double average(int videoId){String s="SELECT AVG(rating) FROM video_reviews WHERE video_id=?";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,videoId);try(ResultSet r=p.executeQuery()){if(r.next()){double v=r.getDouble(1);return r.wasNull()?0.0:v;}}}catch(SQLException e){throw new RuntimeException("Lỗi tính điểm đánh giá video",e);}return 0.0;}
 public VideoReview findByUser(int userId,int videoId){String s="SELECT r.*,u.username,u.full_name AS user_full_name FROM video_reviews r JOIN users u ON r.user_id=u.user_id WHERE r.user_id=? AND r.video_id=?";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,userId);p.setInt(2,videoId);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}}catch(SQLException e){throw new RuntimeException("Lỗi tìm đánh giá video",e);}}
 public boolean upsert(VideoReview x){String s="INSERT INTO video_reviews(user_id,video_id,rating,comment) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE rating=VALUES(rating),comment=VALUES(comment),updated_at=NOW()";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,x.getUserId());p.setInt(2,x.getVideoId());p.setInt(3,x.getRating());p.setString(4,x.getComment());return p.executeUpdate()>0;}catch(SQLException e){throw new RuntimeException("Lỗi lưu đánh giá video",e);}}
 public boolean delete(int id,int userId){String s="DELETE FROM video_reviews WHERE review_id=? AND user_id=?";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,id);p.setInt(2,userId);return p.executeUpdate()>0;}catch(SQLException e){throw new RuntimeException("Lỗi xóa đánh giá video",e);}}
 private VideoReview map(ResultSet r)throws SQLException{VideoReview x=new VideoReview();x.setReviewId(r.getInt("review_id"));x.setUserId(r.getInt("user_id"));x.setVideoId(r.getInt("video_id"));x.setRating(r.getInt("rating"));x.setUsername(r.getString("username"));x.setUserFullName(r.getString("user_full_name"));x.setComment(r.getString("comment"));Timestamp t=r.getTimestamp("created_at");if(t!=null)x.setCreatedAt(t.toLocalDateTime());t=r.getTimestamp("updated_at");if(t!=null)x.setUpdatedAt(t.toLocalDateTime());return x;}
}