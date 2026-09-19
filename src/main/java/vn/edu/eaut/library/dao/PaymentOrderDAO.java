package vn.edu.eaut.library.dao;

import vn.edu.eaut.library.model.PaymentOrder;
import vn.edu.eaut.library.utils.DBConnection;

import java.sql.*;

public class PaymentOrderDAO {

    public boolean insert(PaymentOrder o) {
        String sql = "INSERT INTO payment_orders (txn_ref, user_id, order_type, amount, status) VALUES (?,?,?,?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, o.getTxnRef());
            ps.setInt(2, o.getUserId());
            ps.setString(3, o.getOrderType());
            ps.setLong(4, o.getAmount());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tạo đơn thanh toán", e);
        }
    }

    public PaymentOrder findByTxnRef(String txnRef) {
        String sql = "SELECT * FROM payment_orders WHERE txn_ref = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txnRef);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm đơn thanh toán", e);
        }
        return null;
    }

    /**
     * Đánh dấu đơn hàng SUCCESS/FAILED, CHỈ khi đơn đang ở trạng thái PENDING
     * (điều kiện AND status='PENDING' đảm bảo không xử lý/cộng tiền 2 lần
     * nếu người dùng bấm F5 hoặc quay lại trang Return URL nhiều lần).
     * Trả về true nếu đây là lần cập nhật ĐẦU TIÊN (tức là lần cần cộng tiền/kích hoạt hội viên).
     */
    public boolean markResult(String txnRef, String status, String vnpTransactionNo) {
        String sql = "UPDATE payment_orders SET status=?, vnp_transaction_no=?, paid_at=NOW() " +
                "WHERE txn_ref=? AND status='PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, vnpTransactionNo);
            ps.setString(3, txnRef);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi cập nhật kết quả thanh toán", e);
        }
    }

    private PaymentOrder mapRow(ResultSet rs) throws SQLException {
        PaymentOrder o = new PaymentOrder();
        o.setOrderId(rs.getInt("order_id"));
        o.setTxnRef(rs.getString("txn_ref"));
        o.setUserId(rs.getInt("user_id"));
        o.setOrderType(rs.getString("order_type"));
        o.setAmount(rs.getLong("amount"));
        o.setStatus(rs.getString("status"));
        o.setVnpTransactionNo(rs.getString("vnp_transaction_no"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) o.setCreatedAt(created.toLocalDateTime());
        Timestamp paid = rs.getTimestamp("paid_at");
        if (paid != null) o.setPaidAt(paid.toLocalDateTime());
        return o;
    }
}
