package vn.edu.eaut.library.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AccessHistory implements Serializable {

    private int historyId;
    private int userId;
    private String username;       // dùng khi JOIN để hiển thị
    private Integer documentId;    // null nếu hành động không gắn tài liệu (LOGIN/LOGOUT)
    private String documentTitle;  // dùng khi JOIN để hiển thị
    private String actionType;     // VIEW, DOWNLOAD, LOGIN, LOGOUT
    private LocalDateTime accessTime;
    private String ipAddress;

    public AccessHistory() {
    }

    public AccessHistory(int historyId, int userId, Integer documentId, String actionType,
                          LocalDateTime accessTime, String ipAddress) {
        this.historyId = historyId;
        this.userId = userId;
        this.documentId = documentId;
        this.actionType = actionType;
        this.accessTime = accessTime;
        this.ipAddress = ipAddress;
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
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

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public LocalDateTime getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
