package vn.edu.eaut.library.dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import vn.edu.eaut.library.model.BookReview;
import vn.edu.eaut.library.utils.DBConnection;
public class BookReviewDAO {
 public List<BookReview> findByBookId(int bookId){List<BookReview> l=new ArrayList<>();String s="SELECT r.*,u.username,u.full_name AS user_full_name FROM book_reviews r JOIN users u ON r.user_id=u.user_id WHERE r.book_id=? ORDER BY r.created_at DESC";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,bookId);try(ResultSet rs=p.executeQuery()){while(rs.next())l.add(map(rs));}}catch(SQLException e){throw new RuntimeException("Lỗi lấy đánh giá sách",e);}return l;}
 public Double average(int bookId){String s="SELECT AVG(rating) FROM book_reviews WHERE book_id=?";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,bookId);try(ResultSet r=p.executeQuery()){if(r.next()){double v=r.getDouble(1);return r.wasNull()?0.0:v;}}}catch(SQLException e){throw new RuntimeException("Lỗi tính điểm đánh giá sách",e);}return 0.0;}
 public BookReview findByUser(int userId,int bookId){String s="SELECT r.*,u.username,u.full_name AS user_full_name FROM book_reviews r JOIN users u ON r.user_id=u.user_id WHERE r.user_id=? AND r.book_id=?";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,userId);p.setInt(2,bookId);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}}catch(SQLException e){throw new RuntimeException("Lỗi tìm đánh giá sách",e);}}
 public boolean upsert(BookReview x){String s="INSERT INTO book_reviews(user_id,book_id,rating,comment) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE rating=VALUES(rating),comment=VALUES(comment),updated_at=NOW()";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,x.getUserId());p.setInt(2,x.getBookId());p.setInt(3,x.getRating());p.setString(4,x.getComment());return p.executeUpdate()>0;}catch(SQLException e){throw new RuntimeException("Lỗi lưu đánh giá sách",e);}}
 public boolean delete(int id,int userId){String s="DELETE FROM book_reviews WHERE review_id=? AND user_id=?";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,id);p.setInt(2,userId);return p.executeUpdate()>0;}catch(SQLException e){throw new RuntimeException("Lỗi xóa đánh giá sách",e);}}
 private BookReview map(ResultSet r)throws SQLException{BookReview x=new BookReview();x.setReviewId(r.getInt("review_id"));x.setUserId(r.getInt("user_id"));x.setBookId(r.getInt("book_id"));x.setRating(r.getInt("rating"));x.setUsername(r.getString("username"));x.setUserFullName(r.getString("user_full_name"));x.setComment(r.getString("comment"));Timestamp t=r.getTimestamp("created_at");if(t!=null)x.setCreatedAt(t.toLocalDateTime());t=r.getTimestamp("updated_at");if(t!=null)x.setUpdatedAt(t.toLocalDateTime());return x;}
}