package vn.edu.eaut.library.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import vn.edu.eaut.library.model.BookLicense;
import vn.edu.eaut.library.utils.DBConnection;

public class BookLicenseDAO {

    private static final String BASE_SELECT =
            "SELECT l.*, b.title AS book_title " +
            "FROM book_licenses l " +
            "LEFT JOIN books b ON l.book_id = b.book_id ";

    public List<BookLicense> findAll() {
        List<BookLicense> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY l.license_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách bản quyền sách", e);
        }
        return list;
    }

    public boolean hasValidLicense(int bookId) {
        String sql = "SELECT COUNT(*) FROM book_licenses WHERE book_id=? AND status='VALID' AND (expiry_date IS NULL OR expiry_date >= CURDATE())";
        try (Connection conn=DBConnection.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs=ps.executeQuery()) { return rs.next() && rs.getInt(1)>0; }
        } catch(SQLException e) { throw new RuntimeException("Lỗi kiểm tra hiệu lực bản quyền sách",e); }
    }

    public int markExpiredLicenses() {
        String sql = "UPDATE book_licenses SET status='EXPIRED' WHERE status='VALID' AND expiry_date IS NOT NULL AND expiry_date < CURDATE()";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Lỗi cập nhật bản quyền sách hết hạn", e); }
    }

    public BookLicense findById(int licenseId) {
        String sql = BASE_SELECT + "WHERE l.license_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, licenseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm bản quyền sách theo id", e);
        }
        return null;
    }

    public List<BookLicense> findByBookId(int bookId) {
        List<BookLicense> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE l.book_id = ? ORDER BY l.issued_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm bản quyền theo sách", e);
        }
        return list;
    }

    public boolean insert(BookLicense license) {
        String sql = "INSERT INTO book_licenses (book_id, license_type, license_code, issued_date, expiry_date, terms, status) " +
                "VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, license.getBookId());
            ps.setString(2, license.getLicenseType());
            ps.setString(3, license.getLicenseCode());
            ps.setDate(4, license.getIssuedDate() != null ? Date.valueOf(license.getIssuedDate()) : null);
            ps.setDate(5, license.getExpiryDate() != null ? Date.valueOf(license.getExpiryDate()) : null);
            ps.setString(6, license.getTerms());
            ps.setString(7, license.getStatus() != null ? license.getStatus() : "VALID");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm bản quyền sách", e);
        }
    }

    public boolean update(BookLicense license) {
        String sql = "UPDATE book_licenses SET license_type=?, license_code=?, issued_date=?, expiry_date=?, terms=?, status=? WHERE license_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, license.getLicenseType());
            ps.setString(2, license.getLicenseCode());
            ps.setDate(3, license.getIssuedDate() != null ? Date.valueOf(license.getIssuedDate()) : null);
            ps.setDate(4, license.getExpiryDate() != null ? Date.valueOf(license.getExpiryDate()) : null);
            ps.setString(5, license.getTerms());
            ps.setString(6, license.getStatus());
            ps.setInt(7, license.getLicenseId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi cập nhật bản quyền sách", e);
        }
    }

    public boolean delete(int licenseId) {
        String sql = "DELETE FROM book_licenses WHERE license_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, licenseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa bản quyền sách", e);
        }
    }

    private BookLicense mapRow(ResultSet rs) throws SQLException {
        BookLicense l = new BookLicense();
        l.setLicenseId(rs.getInt("license_id"));
        l.setBookId(rs.getInt("book_id"));
        l.setBookTitle(rs.getString("book_title"));
        l.setLicenseType(rs.getString("license_type"));
        l.setLicenseCode(rs.getString("license_code"));
        Date issued = rs.getDate("issued_date");
        if (issued != null) {
            l.setIssuedDate(issued.toLocalDate());
        }
        Date expiry = rs.getDate("expiry_date");
        if (expiry != null) {
            l.setExpiryDate(expiry.toLocalDate());
        }
        l.setTerms(rs.getString("terms"));
        l.setStatus(rs.getString("status"));
        return l;
    }
}