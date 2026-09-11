package vn.edu.eaut.library.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BookPermission implements Serializable {

    private int permissionId;
    private int userId;
    private String username;
    private int bookId;
    private String bookTitle;
    private String permissionType; // VIEW, DOWNLOAD, EDIT
    private Integer grantedBy;
    private String grantedByName;
    private LocalDateTime grantedDate;
    private LocalDateTime expiryDate;

    public BookPermission() {
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

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
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