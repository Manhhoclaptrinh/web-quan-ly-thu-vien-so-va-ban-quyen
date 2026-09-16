package vn.edu.eaut.library.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Transaction implements Serializable {
    private int transactionId;
    private int userId;
    private String username;   // chỉ dùng khi JOIN để hiển thị (lịch sử giao dịch bên thủ thư)
    private String type;       // TOPUP, MEMBERSHIP_MONTHLY, MEMBERSHIP_YEARLY, VIEW_PDF, DOWNLOAD
    private long amount;       // dương = cộng tiền, âm = trừ tiền
    private String description;
    private LocalDateTime createdAt;

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
