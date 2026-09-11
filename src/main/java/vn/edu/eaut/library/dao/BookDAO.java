package vn.edu.eaut.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import vn.edu.eaut.library.model.Book;
import vn.edu.eaut.library.utils.DBConnection;

public class BookDAO {

    private static final String BASE_SELECT =
            "SELECT b.*, c.category_name, u.full_name AS uploader_name " +
            "FROM books b " +
            "LEFT JOIN categories c ON b.category_id = c.category_id " +
            "LEFT JOIN users u ON b.uploaded_by = u.user_id ";

    public List<Book> findAll() {
        List<Book> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY b.upload_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách sách", e);
        }
        return list;
    }

    public Book findById(int bookId) {
        String sql = BASE_SELECT + "WHERE b.book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm sách theo id", e);
        }
        return null;
    }

    public List<Book> search(String keyword, Integer categoryId, String accessLevel, String status,
                              String author, int page, int pageSize) {
        List<Book> list = new ArrayList<>();
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(Math.min(pageSize, 100), 1);
        int offset = (safePage - 1) * safeSize;
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        if (keyword != null && !keyword.trim().isEmpty()) sql.append("AND (b.title LIKE ? OR b.author LIKE ? OR b.description LIKE ? OR b.isbn LIKE ?) ");
        if (categoryId != null) sql.append("AND b.category_id = ? ");
        if (accessLevel != null && !accessLevel.trim().isEmpty()) sql.append("AND b.access_level = ? ");
        if (status != null && !status.trim().isEmpty()) sql.append("AND b.status = ? ");
        if (author != null && !author.trim().isEmpty()) sql.append("AND b.author LIKE ? ");
        sql.append("ORDER BY b.upload_date DESC LIMIT ? OFFSET ?");
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx=1;
            if (keyword != null && !keyword.trim().isEmpty()) { String kw="%"+keyword.trim()+"%"; ps.setString(idx++,kw); ps.setString(idx++,kw); ps.setString(idx++,kw); ps.setString(idx++,kw); }
            if (categoryId != null) ps.setInt(idx++,categoryId);
            if (accessLevel != null && !accessLevel.trim().isEmpty()) ps.setString(idx++,accessLevel);
            if (status != null && !status.trim().isEmpty()) ps.setString(idx++,status);
            if (author != null && !author.trim().isEmpty()) ps.setString(idx++,"%"+author.trim()+"%");
            ps.setInt(idx++, safeSize); ps.setInt(idx, offset);
            try(ResultSet rs=ps.executeQuery()){ while(rs.next()) list.add(mapRow(rs)); }
        } catch(SQLException e){ throw new RuntimeException("Lỗi khi tìm kiếm sách",e); }
        return list;
    }

    public int countSearch(String keyword, Integer categoryId, String accessLevel, String status, String author) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM books b WHERE 1=1 ");
        if (keyword != null && !keyword.trim().isEmpty()) sql.append("AND (b.title LIKE ? OR b.author LIKE ? OR b.description LIKE ? OR b.isbn LIKE ?) ");
        if (categoryId != null) sql.append("AND b.category_id = ? ");
        if (accessLevel != null && !accessLevel.trim().isEmpty()) sql.append("AND b.access_level = ? ");
        if (status != null && !status.trim().isEmpty()) sql.append("AND b.status = ? ");
        if (author != null && !author.trim().isEmpty()) sql.append("AND b.author LIKE ? ");
        try(Connection conn=DBConnection.getConnection(); PreparedStatement ps=conn.prepareStatement(sql.toString())){
            int idx=1;
            if(keyword!=null&&!keyword.trim().isEmpty()){String kw="%"+keyword.trim()+"%";ps.setString(idx++,kw);ps.setString(idx++,kw);ps.setString(idx++,kw);ps.setString(idx++,kw);}
            if(categoryId!=null)ps.setInt(idx++,categoryId);
            if(accessLevel!=null&&!accessLevel.trim().isEmpty())ps.setString(idx++,accessLevel);
            if(status!=null&&!status.trim().isEmpty())ps.setString(idx++,status);
            if(author!=null&&!author.trim().isEmpty())ps.setString(idx++,"%"+author.trim()+"%");
            try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getInt(1):0;}
        }catch(SQLException e){throw new RuntimeException("Lỗi đếm kết quả tìm kiếm sách",e);}
    }

    public boolean insert(Book b) {
        String sql = "INSERT INTO books (title, author, category_id, file_path, isbn, publisher, publish_year, total_copies, available_copies, description, uploaded_by, access_level, status) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getTitle());
            ps.setString(2, b.getAuthor());
            ps.setInt(3, b.getCategoryId());
            ps.setString(4, b.getFilePath());
            ps.setString(5, b.getIsbn());
            ps.setString(6, b.getPublisher());
            if (b.getPublishYear() != null) ps.setInt(7, b.getPublishYear()); else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, b.getTotalCopies() > 0 ? b.getTotalCopies() : 1);
            ps.setInt(9, b.getAvailableCopies() >= 0 ? b.getAvailableCopies() : (b.getTotalCopies() > 0 ? b.getTotalCopies() : 1));
            ps.setString(10, b.getDescription());
            ps.setInt(11, b.getUploadedBy());
            ps.setString(12, b.getAccessLevel() != null ? b.getAccessLevel() : "PUBLIC");
            ps.setString(13, b.getStatus() != null ? b.getStatus() : "AVAILABLE");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm sách", e);
        }
    }

    public boolean update(Book b) {
        String sql = "UPDATE books SET title=?, author=?, category_id=?, isbn=?, publisher=?, publish_year=?, total_copies=?, available_copies=?, description=?, access_level=?, status=? WHERE book_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getTitle());
            ps.setString(2, b.getAuthor());
            ps.setInt(3, b.getCategoryId());
            ps.setString(4, b.getIsbn());
            ps.setString(5, b.getPublisher());
            if (b.getPublishYear() != null) ps.setInt(6, b.getPublishYear()); else ps.setNull(6, Types.INTEGER);
            ps.setInt(7, b.getTotalCopies());
            ps.setInt(8, b.getAvailableCopies());
            ps.setString(9, b.getDescription());
            ps.setString(10, b.getAccessLevel());
            ps.setString(11, b.getStatus());
            ps.setInt(12, b.getBookId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi cập nhật sách", e);
        }
    }

    public boolean updateWithFile(Book b) {
        String sql = "UPDATE books SET title=?, author=?, category_id=?, file_path=?, isbn=?, publisher=?, publish_year=?, total_copies=?, available_copies=?, description=?, access_level=?, status=? WHERE book_id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getTitle()); ps.setString(2, b.getAuthor()); ps.setInt(3, b.getCategoryId());
            ps.setString(4, b.getFilePath()); ps.setString(5, b.getIsbn()); ps.setString(6, b.getPublisher());
            if (b.getPublishYear() != null) ps.setInt(7, b.getPublishYear()); else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, b.getTotalCopies()); ps.setInt(9, b.getAvailableCopies());
            ps.setString(10, b.getDescription()); ps.setString(11, b.getAccessLevel());
            ps.setString(12, b.getStatus()); ps.setInt(13, b.getBookId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException("Lỗi khi cập nhật sách và file", e); }
    }

    public boolean delete(int bookId) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa sách", e);
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setBookId(rs.getInt("book_id"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setCategoryId(rs.getInt("category_id"));
        b.setCategoryName(rs.getString("category_name"));
        b.setFilePath(rs.getString("file_path"));
        b.setIsbn(rs.getString("isbn"));
        b.setPublisher(rs.getString("publisher"));
        int year = rs.getInt("publish_year");
        if (!rs.wasNull()) b.setPublishYear(year);
        b.setTotalCopies(rs.getInt("total_copies"));
        b.setAvailableCopies(rs.getInt("available_copies"));
        b.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("upload_date");
        if (ts != null) {
            b.setUploadDate(ts.toLocalDateTime());
        }
        b.setUploadedBy(rs.getInt("uploaded_by"));
        b.setUploaderName(rs.getString("uploader_name"));
        b.setAccessLevel(rs.getString("access_level"));
        b.setStatus(rs.getString("status"));
        return b;
    }
}