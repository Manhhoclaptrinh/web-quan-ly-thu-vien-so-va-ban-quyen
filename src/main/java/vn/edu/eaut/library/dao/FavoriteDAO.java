package vn.edu.eaut.library.dao;
import vn.edu.eaut.library.model.Favorite;
import vn.edu.eaut.library.utils.DBConnection;
import java.sql.*; import java.util.*;
public class FavoriteDAO {
 public boolean exists(int userId,int documentId){String s="SELECT COUNT(*) FROM favorites WHERE user_id=? AND document_id=?";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,userId);p.setInt(2,documentId);try(ResultSet r=p.executeQuery()){return r.next()&&r.getInt(1)>0;}}catch(SQLException e){throw new RuntimeException("Lỗi kiểm tra yêu thích",e);}}
 public boolean add(int userId,int documentId){String s="INSERT IGNORE INTO favorites(user_id,document_id) VALUES(?,?)";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,userId);p.setInt(2,documentId);return p.executeUpdate()>0;}catch(SQLException e){throw new RuntimeException("Lỗi thêm yêu thích",e);}}
 public boolean remove(int userId,int documentId){String s="DELETE FROM favorites WHERE user_id=? AND document_id=?";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,userId);p.setInt(2,documentId);return p.executeUpdate()>0;}catch(SQLException e){throw new RuntimeException("Lỗi xóa yêu thích",e);}}
 public List<Favorite> findByUserId(int userId){List<Favorite> l=new ArrayList<>();String s="SELECT f.*,d.title document_title,d.author FROM favorites f JOIN documents d ON f.document_id=d.document_id WHERE f.user_id=? ORDER BY f.created_at DESC";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(s)){p.setInt(1,userId);try(ResultSet r=p.executeQuery()){while(r.next())l.add(map(r));}}catch(SQLException e){throw new RuntimeException("Lỗi lấy danh sách yêu thích",e);}return l;}
 private Favorite map(ResultSet r)throws SQLException{Favorite f=new Favorite();f.setFavoriteId(r.getInt("favorite_id"));f.setUserId(r.getInt("user_id"));f.setDocumentId(r.getInt("document_id"));f.setDocumentTitle(r.getString("document_title"));f.setAuthor(r.getString("author"));Timestamp t=r.getTimestamp("created_at");if(t!=null)f.setCreatedAt(t.toLocalDateTime());return f;}
}
