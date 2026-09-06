package vn.edu.eaut.library.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Document implements Serializable {

    private int documentId;
    private String title;
    private String author;
    private int categoryId;
    private String categoryName;   // dùng khi JOIN để hiển thị
    private String filePath;
    private String description;
    private LocalDateTime uploadDate;
    private int uploadedBy;
    private String uploaderName;   // dùng khi JOIN để hiển thị
    private String accessLevel;    // PUBLIC, RESTRICTED, PRIVATE
    private String status;         // AVAILABLE, DISABLED

    public Document() {
    }

    public Document(int documentId, String title, String author, int categoryId,
                     String filePath, String description, LocalDateTime uploadDate,
                     int uploadedBy, String accessLevel, String status) {
        this.documentId = documentId;
        this.title = title;
        this.author = author;
        this.categoryId = categoryId;
        this.filePath = filePath;
        this.description = description;
        this.uploadDate = uploadDate;
        this.uploadedBy = uploadedBy;
        this.accessLevel = accessLevel;
        this.status = status;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public int getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(int uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Document{" +
                "documentId=" + documentId +
                ", title='" + title + '\'' +
                ", accessLevel='" + accessLevel + '\'' +
                '}';
    }
}
