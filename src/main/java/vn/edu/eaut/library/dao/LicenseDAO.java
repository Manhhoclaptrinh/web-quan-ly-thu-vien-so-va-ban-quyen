package vn.edu.eaut.library.dao;

import vn.edu.eaut.library.model.License;
import vn.edu.eaut.library.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LicenseDAO {

    private static final String BASE_SELECT =
            "SELECT l.*, d.title AS document_title " +
            "FROM licenses l " +
            "LEFT JOIN documents d ON l.document_id = d.document_id ";

    public List<License> findAll() {
        List<License> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY l.license_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách license", e);
        }
        return list;
    }

    public boolean hasValidLicense(int documentId) {
        String sql = "SELECT COUNT(*) FROM licenses WHERE document_id=? AND status='VALID' AND (expiry_date IS NULL OR expiry_date >= CURDATE())";
        try (Connection conn=DBConnection.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)) {
            ps.setInt(1, documentId);
            try (ResultSet rs=ps.executeQuery()) { return rs.next() && rs.getInt(1)>0; }
        } catch(SQLException e) { throw new RuntimeException("Lỗi kiểm tra hiệu lực bản quyền",e); }
    }

    public int markExpiredLicenses() {
        String sql = "UPDATE licenses SET status='EXPIRED' WHERE status='VALID' AND expiry_date IS NOT NULL AND expiry_date < CURDATE()";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Lỗi cập nhật bản quyền hết hạn", e); }
    }

    public License findById(int licenseId) {
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
            throw new RuntimeException("Lỗi khi tìm license theo id", e);
        }
        return null;
    }

    public List<License> findByDocumentId(int documentId) {
        List<License> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE l.document_id = ? ORDER BY l.issued_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm license theo tài liệu", e);
        }
        return list;
    }

    public boolean insert(License license) {
        String sql = "INSERT INTO licenses (document_id, license_type, license_code, issued_date, expiry_date, terms, status) " +
                "VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, license.getDocumentId());
            ps.setString(2, license.getLicenseType());
            ps.setString(3, license.getLicenseCode());
            ps.setDate(4, license.getIssuedDate() != null ? Date.valueOf(license.getIssuedDate()) : null);
            ps.setDate(5, license.getExpiryDate() != null ? Date.valueOf(license.getExpiryDate()) : null);
            ps.setString(6, license.getTerms());
            ps.setString(7, license.getStatus() != null ? license.getStatus() : "VALID");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm license", e);
        }
    }

    public boolean update(License license) {
        String sql = "UPDATE licenses SET license_type=?, license_code=?, issued_date=?, expiry_date=?, terms=?, status=? WHERE license_id=?";
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
            throw new RuntimeException("Lỗi khi cập nhật license", e);
        }
    }

    public boolean delete(int licenseId) {
        String sql = "DELETE FROM licenses WHERE license_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, licenseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa license", e);
        }
    }

    private License mapRow(ResultSet rs) throws SQLException {
        License l = new License();
        l.setLicenseId(rs.getInt("license_id"));
        l.setDocumentId(rs.getInt("document_id"));
        l.setDocumentTitle(rs.getString("document_title"));
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
