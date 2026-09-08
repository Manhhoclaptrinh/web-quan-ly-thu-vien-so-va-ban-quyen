package vn.edu.eaut.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import vn.edu.eaut.library.model.Video;
import vn.edu.eaut.library.utils.DBConnection;

public class VideoDAO {

    private static final String BASE_SELECT =
            "SELECT v.*, c.category_name, u.full_name AS uploader_name " +
            "FROM videos v " +
            "LEFT JOIN categories c ON v.category_id = c.category_id " +
            "LEFT JOIN users u ON v.uploaded_by = u.user_id ";

    public List<Video> findAll() {
        List<Video> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY v.upload_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách video", e);
        }
        return list;
    }

    public Video findById(int videoId) {
        String sql = BASE_SELECT + "WHERE v.video_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, videoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm video theo id", e);
        }
        return null;
    }

    public List<Video> search(String keyword, Integer categoryId, String accessLevel, String status,
                               String author, int page, int pageSize) {
        List<Video> list = new ArrayList<>();
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(Math.min(pageSize, 100), 1);
        int offset = (safePage - 1) * safeSize;
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        if (keyword != null && !keyword.trim().isEmpty()) sql.append("AND (v.title LIKE ? OR v.author LIKE ? OR v.description LIKE ?) ");
        if (categoryId != null) sql.append("AND v.category_id = ? ");
        if (accessLevel != null && !accessLevel.trim().isEmpty()) sql.append("AND v.access_level = ? ");
        if (status != null && !status.trim().isEmpty()) sql.append("AND v.status = ? ");
        if (author != null && !author.trim().isEmpty()) sql.append("AND v.author LIKE ? ");
        sql.append("ORDER BY v.upload_date DESC LIMIT ? OFFSET ?");
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx=1;
            if (keyword != null && !keyword.trim().isEmpty()) { String kw="%"+keyword.trim()+"%"; ps.setString(idx++,kw); ps.setString(idx++,kw); ps.setString(idx++,kw); }
            if (categoryId != null) ps.setInt(idx++,categoryId);
            if (accessLevel != null && !accessLevel.trim().isEmpty()) ps.setString(idx++,accessLevel);
            if (status != null && !status.trim().isEmpty()) ps.setString(idx++,status);
            if (author != null && !author.trim().isEmpty()) ps.setString(idx++,"%"+author.trim()+"%");
            ps.setInt(idx++, safeSize); ps.setInt(idx, offset);
            try(ResultSet rs=ps.executeQuery()){ while(rs.next()) list.add(mapRow(rs)); }
        } catch(SQLException e){ throw new RuntimeException("Lỗi khi tìm kiếm video",e); }
        return list;
    }

    public int countSearch(String keyword, Integer categoryId, String accessLevel, String status, String author) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM videos v WHERE 1=1 ");
        if (keyword != null && !keyword.trim().isEmpty()) sql.append("AND (v.title LIKE ? OR v.author LIKE ? OR v.description LIKE ?) ");
        if (categoryId != null) sql.append("AND v.category_id = ? ");
        if (accessLevel != null && !accessLevel.trim().isEmpty()) sql.append("AND v.access_level = ? ");
        if (status != null && !status.trim().isEmpty()) sql.append("AND v.status = ? ");
        if (author != null && !author.trim().isEmpty()) sql.append("AND v.author LIKE ? ");
        try(Connection conn=DBConnection.getConnection(); PreparedStatement ps=conn.prepareStatement(sql.toString())){
            int idx=1;
            if(keyword!=null&&!keyword.trim().isEmpty()){String kw="%"+keyword.trim()+"%";ps.setString(idx++,kw);ps.setString(idx++,kw);ps.setString(idx++,kw);}
            if(categoryId!=null)ps.setInt(idx++,categoryId);
            if(accessLevel!=null&&!accessLevel.trim().isEmpty())ps.setString(idx++,accessLevel);
            if(status!=null&&!status.trim().isEmpty())ps.setString(idx++,status);
            if(author!=null&&!author.trim().isEmpty())ps.setString(idx++,"%"+author.trim()+"%");
            try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getInt(1):0;}
        }catch(SQLException e){throw new RuntimeException("Lỗi đếm kết quả tìm kiếm video",e);}
    }

    public boolean insert(Video v) {
        String sql = "INSERT INTO videos (title, author, category_id, file_path, duration_seconds, thumbnail_path, description, uploaded_by, access_level, status) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getTitle());
            ps.setString(2, v.getAuthor());
            ps.setInt(3, v.getCategoryId());
            ps.setString(4, v.getFilePath());
            if (v.getDurationSeconds() != null) ps.setInt(5, v.getDurationSeconds()); else ps.setNull(5, Types.INTEGER);
            ps.setString(6, v.getThumbnailPath());
            ps.setString(7, v.getDescription());
            ps.setInt(8, v.getUploadedBy());
            ps.setString(9, v.getAccessLevel() != null ? v.getAccessLevel() : "PUBLIC");
            ps.setString(10, v.getStatus() != null ? v.getStatus() : "AVAILABLE");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm video", e);
        }
    }

    public boolean update(Video v) {
        String sql = "UPDATE videos SET title=?, author=?, category_id=?, description=?, access_level=?, status=? WHERE video_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getTitle());
            ps.setString(2, v.getAuthor());
            ps.setInt(3, v.getCategoryId());
            ps.setString(4, v.getDescription());
            ps.setString(5, v.getAccessLevel());
            ps.setString(6, v.getStatus());
            ps.setInt(7, v.getVideoId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi cập nhật video", e);
        }
    }

    public boolean updateWithFile(Video v) {
        String sql = "UPDATE videos SET title=?, author=?, category_id=?, file_path=?, description=?, access_level=?, status=? WHERE video_id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getTitle()); ps.setString(2, v.getAuthor()); ps.setInt(3, v.getCategoryId());
            ps.setString(4, v.getFilePath()); ps.setString(5, v.getDescription()); ps.setString(6, v.getAccessLevel());
            ps.setString(7, v.getStatus()); ps.setInt(8, v.getVideoId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException("Lỗi khi cập nhật video và file", e); }
    }

    public boolean delete(int videoId) {
        String sql = "DELETE FROM videos WHERE video_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, videoId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa video", e);
        }
    }

    private Video mapRow(ResultSet rs) throws SQLException {
        Video v = new Video();
        v.setVideoId(rs.getInt("video_id"));
        v.setTitle(rs.getString("title"));
        v.setAuthor(rs.getString("author"));
        v.setCategoryId(rs.getInt("category_id"));
        v.setCategoryName(rs.getString("category_name"));
        v.setFilePath(rs.getString("file_path"));
        int duration = rs.getInt("duration_seconds");
        if (!rs.wasNull()) v.setDurationSeconds(duration);
        v.setThumbnailPath(rs.getString("thumbnail_path"));
        v.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("upload_date");
        if (ts != null) {
            v.setUploadDate(ts.toLocalDateTime());
        }
        v.setUploadedBy(rs.getInt("uploaded_by"));
        v.setUploaderName(rs.getString("uploader_name"));
        v.setAccessLevel(rs.getString("access_level"));
        v.setStatus(rs.getString("status"));
        return v;
    }
}