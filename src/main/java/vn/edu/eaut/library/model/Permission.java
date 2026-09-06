package vn.edu.eaut.library.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Permission implements Serializable {

    private int permissionId;
    private int userId;
    private String username;       // dùng khi JOIN để hiển thị
    private int documentId;
    private String documentTitle;  // dùng khi JOIN để hiển thị
    private String permissionType; // VIEW, DOWNLOAD, EDIT
    private Integer grantedBy;
    private String grantedByName;  // dùng khi JOIN để hiển thị
    private LocalDateTime grantedDate;
    private LocalDateTime expiryDate;

    public Permission() {
    }

    public Permission(int permissionId, int userId, int documentId, String permissionType,
                       Integer grantedBy, LocalDateTime grantedDate, LocalDateTime expiryDate) {
        this.permissionId = permissionId;
        this.userId = userId;
        this.documentId = documentId;
        this.permissionType = permissionType;
        this.grantedBy = grantedBy;
        this.grantedDate = grantedDate;
        this.expiryDate = expiryDate;
    }

    public int getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(int permissionId) {
        this.permissionId = permissionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }

    public Integer getGrantedBy() {
        return grantedBy;
    }

    public void setGrantedBy(Integer grantedBy) {
        this.grantedBy = grantedBy;
    }

    public String getGrantedByName() {
        return grantedByName;
    }

    public void setGrantedByName(String grantedByName) {
        this.grantedByName = grantedByName;
    }

    public LocalDateTime getGrantedDate() {
        return grantedDate;
    }

    public void setGrantedDate(LocalDateTime grantedDate) {
        this.grantedDate = grantedDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }
}
