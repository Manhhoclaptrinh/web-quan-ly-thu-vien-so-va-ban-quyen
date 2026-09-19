package vn.edu.eaut.library.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PaymentOrder implements Serializable {
    private int orderId;
    private String txnRef;
    private int userId;
    private String orderType;   // WALLET_TOPUP, MEMBERSHIP_MONTHLY, MEMBERSHIP_YEARLY
    private long amount;
    private String status;      // PENDING, SUCCESS, FAILED
    private String vnpTransactionNo;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getTxnRef() { return txnRef; }
    public void setTxnRef(String txnRef) { this.txnRef = txnRef; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVnpTransactionNo() { return vnpTransactionNo; }
    public void setVnpTransactionNo(String vnpTransactionNo) { this.vnpTransactionNo = vnpTransactionNo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
