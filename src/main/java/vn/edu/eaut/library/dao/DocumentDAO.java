package vn.edu.eaut.library.dao;

import vn.edu.eaut.library.model.Document;
import vn.edu.eaut.library.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {

    private static final String BASE_SELECT =
            "SELECT d.*, c.category_name, u.full_name AS uploader_name " +
            "FROM documents d " +
            "LEFT JOIN categories c ON d.category_id = c.category_id " +
            "LEFT JOIN users u ON d.uploaded_by = u.user_id ";

    public List<Document> findAll() {
        List<Document> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY d.upload_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách tài liệu", e);
        }
        return list;
    }

    public Document findById(int documentId) {
        String sql = BASE_SELECT + "WHERE d.document_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm tài liệu theo id", e);
        }
        return null;
    }

    public List<Document> search(String keyword, Integer categoryId, String accessLevel, String status,
                                 String author, int page, int pageSize) {
        List<Document> list = new ArrayList<>();
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(Math.min(pageSize, 100), 1);
        int offset = (safePage - 1) * safeSize;
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        if (keyword != null && !keyword.trim().isEmpty()) sql.append("AND (d.title LIKE ? OR d.author LIKE ? OR d.description LIKE ?) ");
        if (categoryId != null) sql.append("AND d.category_id = ? ");
        if (accessLevel != null && !accessLevel.trim().isEmpty()) sql.append("AND d.access_level = ? ");
        if (status != null && !status.trim().isEmpty()) sql.append("AND d.status = ? ");
        if (author != null && !author.trim().isEmpty()) sql.append("AND d.author LIKE ? ");
        sql.append("ORDER BY d.upload_date DESC LIMIT ? OFFSET ?");
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx=1;
            if (keyword != null && !keyword.trim().isEmpty()) { String kw="%"+keyword.trim()+"%"; ps.setString(idx++,kw); ps.setString(idx++,kw); ps.setString(idx++,kw); }
            if (categoryId != null) ps.setInt(idx++,categoryId);
            if (accessLevel != null && !accessLevel.trim().isEmpty()) ps.setString(idx++,accessLevel);
            if (status != null && !status.trim().isEmpty()) ps.setString(idx++,status);
            if (author != null && !author.trim().isEmpty()) ps.setString(idx++,"%"+author.trim()+"%");
            ps.setInt(idx++, safeSize); ps.setInt(idx, offset);
            try(ResultSet rs=ps.executeQuery()){ while(rs.next()) list.add(mapRow(rs)); }
        } catch(SQLException e){ throw new RuntimeException("Lỗi khi tìm kiếm tài liệu",e); }
        return list;
    }

    public int countSearch(String keyword, Integer categoryId, String accessLevel, String status, String author) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM documents d WHERE 1=1 ");
        if (keyword != null && !keyword.trim().isEmpty()) sql.append("AND (d.title LIKE ? OR d.author LIKE ? OR d.description LIKE ?) ");
        if (categoryId != null) sql.append("AND d.category_id = ? ");
        if (accessLevel != null && !accessLevel.trim().isEmpty()) sql.append("AND d.access_level = ? ");
        if (status != null && !status.trim().isEmpty()) sql.append("AND d.status = ? ");
        if (author != null && !author.trim().isEmpty()) sql.append("AND d.author LIKE ? ");
        try(Connection conn=DBConnection.getConnection(); PreparedStatement ps=conn.prepareStatement(sql.toString())){
            int idx=1;
            if(keyword!=null&&!keyword.trim().isEmpty()){String kw="%"+keyword.trim()+"%";ps.setString(idx++,kw);ps.setString(idx++,kw);ps.setString(idx++,kw);}
            if(categoryId!=null)ps.setInt(idx++,categoryId);
            if(accessLevel!=null&&!accessLevel.trim().isEmpty())ps.setString(idx++,accessLevel);
            if(status!=null&&!status.trim().isEmpty())ps.setString(idx++,status);
            if(author!=null&&!author.trim().isEmpty())ps.setString(idx++,"%"+author.trim()+"%");
            try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getInt(1):0;}
        }catch(SQLException e){throw new RuntimeException("Lỗi đếm kết quả tìm kiếm tài liệu",e);}
    }

    // Giữ tương thích với code cũ.
    public List<Document> search(String keyword, Integer categoryId) {
        return search(keyword, categoryId, null, null, null, 1, 1000);
    }

    public boolean insert(Document doc) {
        String sql = "INSERT INTO documents (title, author, category_id, file_path, description, uploaded_by, access_level, status) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doc.getTitle());
            ps.setString(2, doc.getAuthor());
            ps.setInt(3, doc.getCategoryId());
            ps.setString(4, doc.getFilePath());
            ps.setString(5, doc.getDescription());
            ps.setInt(6, doc.getUploadedBy());
            ps.setString(7, doc.getAccessLevel() != null ? doc.getAccessLevel() : "PUBLIC");
            ps.setString(8, doc.getStatus() != null ? doc.getStatus() : "AVAILABLE");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm tài liệu", e);
        }
    }

    public boolean update(Document doc) {
        String sql = "UPDATE documents SET title=?, author=?, category_id=?, description=?, access_level=?, status=? WHERE document_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doc.getTitle());
            ps.setString(2, doc.getAuthor());
            ps.setInt(3, doc.getCategoryId());
            ps.setString(4, doc.getDescription());
            ps.setString(5, doc.getAccessLevel());
            ps.setString(6, doc.getStatus());
            ps.setInt(7, doc.getDocumentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi cập nhật tài liệu", e);
        }
    }

    public boolean updateWithFile(Document doc) {
        String sql = "UPDATE documents SET title=?, author=?, category_id=?, file_path=?, description=?, access_level=?, status=? WHERE document_id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doc.getTitle()); ps.setString(2, doc.getAuthor()); ps.setInt(3, doc.getCategoryId());
            ps.setString(4, doc.getFilePath()); ps.setString(5, doc.getDescription()); ps.setString(6, doc.getAccessLevel());
            ps.setString(7, doc.getStatus()); ps.setInt(8, doc.getDocumentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException("Lỗi khi cập nhật tài liệu và file", e); }
    }

    public boolean delete(int documentId) {
        String sql = "DELETE FROM documents WHERE document_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, documentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa tài liệu", e);
        }
    }

    private Document mapRow(ResultSet rs) throws SQLException {
        Document d = new Document();
        d.setDocumentId(rs.getInt("document_id"));
        d.setTitle(rs.getString("title"));
        d.setAuthor(rs.getString("author"));
        d.setCategoryId(rs.getInt("category_id"));
        d.setCategoryName(rs.getString("category_name"));
        d.setFilePath(rs.getString("file_path"));
        d.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("upload_date");
        if (ts != null) {
            d.setUploadDate(ts.toLocalDateTime());
        }
        d.setUploadedBy(rs.getInt("uploaded_by"));
        d.setUploaderName(rs.getString("uploader_name"));
        d.setAccessLevel(rs.getString("access_level"));
        d.setStatus(rs.getString("status"));
        return d;
    }
}