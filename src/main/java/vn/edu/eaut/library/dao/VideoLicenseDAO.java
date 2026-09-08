package vn.edu.eaut.library.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import vn.edu.eaut.library.model.VideoLicense;
import vn.edu.eaut.library.utils.DBConnection;

public class VideoLicenseDAO {

    private static final String BASE_SELECT =
            "SELECT l.*, v.title AS video_title " +
            "FROM video_licenses l " +
            "LEFT JOIN videos v ON l.video_id = v.video_id ";

    public List<VideoLicense> findAll() {
        List<VideoLicense> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY l.license_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách bản quyền video", e);
        }
        return list;
    }

    public boolean hasValidLicense(int videoId) {
        String sql = "SELECT COUNT(*) FROM video_licenses WHERE video_id=? AND status='VALID' AND (expiry_date IS NULL OR expiry_date >= CURDATE())";
        try (Connection conn=DBConnection.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)) {
            ps.setInt(1, videoId);
            try (ResultSet rs=ps.executeQuery()) { return rs.next() && rs.getInt(1)>0; }
        } catch(SQLException e) { throw new RuntimeException("Lỗi kiểm tra hiệu lực bản quyền video",e); }
    }

    public int markExpiredLicenses() {
        String sql = "UPDATE video_licenses SET status='EXPIRED' WHERE status='VALID' AND expiry_date IS NOT NULL AND expiry_date < CURDATE()";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Lỗi cập nhật bản quyền video hết hạn", e); }
    }

    public VideoLicense findById(int licenseId) {
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
            throw new RuntimeException("Lỗi khi tìm bản quyền video theo id", e);
        }
        return null;
    }

    public List<VideoLicense> findByVideoId(int videoId) {
        List<VideoLicense> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE l.video_id = ? ORDER BY l.issued_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, videoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm bản quyền theo video", e);
        }
        return list;
    }

    public boolean insert(VideoLicense license) {
        String sql = "INSERT INTO video_licenses (video_id, license_type, license_code, issued_date, expiry_date, terms, status) " +
                "VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, license.getVideoId());
            ps.setString(2, license.getLicenseType());
            ps.setString(3, license.getLicenseCode());
            ps.setDate(4, license.getIssuedDate() != null ? Date.valueOf(license.getIssuedDate()) : null);
            ps.setDate(5, license.getExpiryDate() != null ? Date.valueOf(license.getExpiryDate()) : null);
            ps.setString(6, license.getTerms());
            ps.setString(7, license.getStatus() != null ? license.getStatus() : "VALID");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm bản quyền video", e);
        }
    }

    public boolean update(VideoLicense license) {
        String sql = "UPDATE video_licenses SET license_type=?, license_code=?, issued_date=?, expiry_date=?, terms=?, status=? WHERE license_id=?";
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
            throw new RuntimeException("Lỗi khi cập nhật bản quyền video", e);
        }
    }

    public boolean delete(int licenseId) {
        String sql = "DELETE FROM video_licenses WHERE license_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, licenseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa bản quyền video", e);
        }
    }

    private VideoLicense mapRow(ResultSet rs) throws SQLException {
        VideoLicense l = new VideoLicense();
        l.setLicenseId(rs.getInt("license_id"));
        l.setVideoId(rs.getInt("video_id"));
        l.setVideoTitle(rs.getString("video_title"));
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