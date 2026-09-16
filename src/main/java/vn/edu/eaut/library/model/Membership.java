package vn.edu.eaut.library.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Membership implements Serializable {
    private int membershipId;
    private int userId;
    private String username;   // chỉ dùng khi JOIN để hiển thị (lịch sử đăng ký hội viên bên thủ thư)
    private String planType;   // MONTHLY, YEARLY
    private long price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;

    public int getMembershipId() { return membershipId; }
    public void setMembershipId(int membershipId) { this.membershipId = membershipId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
