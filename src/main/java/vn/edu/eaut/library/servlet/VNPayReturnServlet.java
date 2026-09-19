package vn.edu.eaut.library.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.library.dao.MembershipDAO;
import vn.edu.eaut.library.dao.PaymentOrderDAO;
import vn.edu.eaut.library.dao.TransactionDAO;
import vn.edu.eaut.library.dao.UserDAO;
import vn.edu.eaut.library.model.*;
import vn.edu.eaut.library.payment.VNPayConfig;
import vn.edu.eaut.library.payment.VNPayUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * /payment/vnpay/return - VNPay chuyển hướng trình duyệt người dùng về đây
 * sau khi thanh toán xong (thành công hoặc thất bại).
 *
 * QUAN TRỌNG:
 *  - Chữ ký HMAC-SHA512 (vnp_SecureHash) do VNPay ký bằng HashSecret bí mật
 *    chỉ VNPay và merchant biết -> nếu khớp chữ ký thì chắc chắn dữ liệu
 *    đến từ VNPay thật, không thể giả mạo. Đây là cơ sở để tin tưởng xử lý.
 *  - Chỉ cộng tiền/kích hoạt hội viên MỘT LẦN DUY NHẤT cho mỗi đơn hàng
 *    (PaymentOrderDAO.markResult chỉ update khi đơn đang PENDING) để tránh
 *    trường hợp người dùng bấm F5 / quay lại trang này nhiều lần.
 *
 * GHI CHÚ VỀ IPN: VNPay còn 1 kênh xác nhận độc lập gọi là IPN (server gọi
 * server), đáng tin cậy hơn Return URL vì không phụ thuộc trình duyệt người
 * dùng có quay lại được hay không. Do IPN yêu cầu 1 địa chỉ public (VNPay ở
 * ngoài Internet gọi vào), còn ở đây đang chạy Tomcat trên localhost nên
 * CHƯA cài IPN. Khi lên môi trường thật (có domain/HTTPS public), nên bổ
 * sung thêm /payment/vnpay/ipn và khai báo với VNPay để chắc chắn hơn.
 */
@WebServlet("/payment/vnpay/return")
public class VNPayReturnServlet extends HttpServlet {

    private final PaymentOrderDAO paymentOrderDAO = new PaymentOrderDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final MembershipDAO membershipDAO = new MembershipDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map<String, String> fields = new HashMap<>();
        Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            String value = req.getParameter(name);
            if (name.startsWith("vnp_") && value != null && !value.isEmpty()) {
                fields.put(name, value);
            }
        }

        String receivedHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        boolean validSignature = receivedHash != null
                && VNPayUtil.verifySignature(fields, receivedHash, VNPayConfig.getHashSecret());

        String resultTitle;
        String resultMessage;
        boolean success = false;

        if (!validSignature) {
            resultTitle = "Chữ ký không hợp lệ";
            resultMessage = "Dữ liệu trả về không đáng tin cậy, giao dịch không được xử lý. Nếu tiền đã bị trừ, vui lòng liên hệ quản trị viên kèm mã giao dịch.";
        } else {
            String txnRef = fields.get("vnp_TxnRef");
            String responseCode = fields.get("vnp_ResponseCode");
            String vnpTransactionNo = fields.get("vnp_TransactionNo");
            String vnpAmountRaw = fields.get("vnp_Amount");

            PaymentOrder order = paymentOrderDAO.findByTxnRef(txnRef);

            if (order == null) {
                resultTitle = "Không tìm thấy đơn hàng";
                resultMessage = "Không tìm thấy đơn hàng tương ứng trong hệ thống (mã: " + txnRef + ").";
            } else if (!"PENDING".equals(order.getStatus())) {
                // Da xu ly truoc do (nguoi dung bam F5 / back lai trang nay) -> chi hien lai ket qua, KHONG cong tien lai
                success = "SUCCESS".equals(order.getStatus());
                resultTitle = success ? "Thanh toán thành công" : "Thanh toán thất bại";
                resultMessage = "Đơn hàng này đã được xử lý trước đó.";
            } else {
                long vnpAmount = 0;
                try {
                    vnpAmount = Long.parseLong(vnpAmountRaw) / 100;
                } catch (Exception ignored) {
                }

                if (vnpAmount != order.getAmount()) {
                    paymentOrderDAO.markResult(txnRef, "FAILED", vnpTransactionNo);
                    resultTitle = "Sai lệch số tiền";
                    resultMessage = "Số tiền VNPay xác nhận không khớp với đơn hàng, giao dịch bị từ chối.";
                } else if ("00".equals(responseCode)) {
                    boolean firstTime = paymentOrderDAO.markResult(txnRef, "SUCCESS", vnpTransactionNo);
                    if (firstTime) {
                        applySuccessfulOrder(order, txnRef);
                    }
                    success = true;
                    resultTitle = "Thanh toán thành công";
                    resultMessage = describeOrder(order) + " Mã giao dịch VNPay: " + vnpTransactionNo;
                } else {
                    paymentOrderDAO.markResult(txnRef, "FAILED", vnpTransactionNo);
                    resultTitle = "Thanh toán thất bại";
                    resultMessage = "Mã lỗi VNPay: " + responseCode + ". Vui lòng thử lại.";
                }
            }
        }

        req.setAttribute("success", success);
        req.setAttribute("resultTitle", resultTitle);
        req.setAttribute("resultMessage", resultMessage);
        req.getRequestDispatcher("/views/payment-result.jsp").forward(req, resp);
    }

    /** Cộng tiền ví / kích hoạt hội viên sau khi xác nhận thanh toán VNPay thành công. */
    private void applySuccessfulOrder(PaymentOrder order, String txnRef) {
        int userId = order.getUserId();

        if ("WALLET_TOPUP".equals(order.getOrderType())) {
            userDAO.topUpWallet(userId, order.getAmount());

            Transaction t = new Transaction();
            t.setUserId(userId);
            t.setType("TOPUP");
            t.setAmount(order.getAmount());
            t.setDescription("Nạp tiền qua VNPay (mã đơn: " + txnRef + ")");
            transactionDAO.insert(t);

        } else { // MEMBERSHIP_MONTHLY hoặc MEMBERSHIP_YEARLY
            boolean isMonthly = "MEMBERSHIP_MONTHLY".equals(order.getOrderType());

            Membership current = membershipDAO.findActiveByUser(userId);
            LocalDateTime start = (current != null) ? current.getEndDate() : LocalDateTime.now();
            LocalDateTime end = isMonthly ? start.plusMonths(1) : start.plusYears(1);

            Membership m = new Membership();
            m.setUserId(userId);
            m.setPlanType(isMonthly ? "MONTHLY" : "YEARLY");
            m.setPrice(order.getAmount());
            m.setStartDate(start);
            m.setEndDate(end);
            membershipDAO.insert(m);

            Transaction t = new Transaction();
            t.setUserId(userId);
            t.setType(order.getOrderType());
            t.setAmount(-order.getAmount());
            t.setDescription((isMonthly ? "Đăng ký hội viên tháng" : "Đăng ký hội viên năm")
                    + " qua VNPay (mã đơn: " + txnRef + ")");
            transactionDAO.insert(t);
        }

        // Đồng bộ số dư ví trong session hiện tại (nếu vẫn cùng phiên đăng nhập lúc thanh toán)
        // để người dùng thấy số dư mới ngay, không cần đăng nhập lại.
    }

    private String describeOrder(PaymentOrder order) {
        switch (order.getOrderType()) {
            case "MEMBERSHIP_MONTHLY":
                return "Đăng ký hội viên tháng (" + order.getAmount() + "đ) thành công.";
            case "MEMBERSHIP_YEARLY":
                return "Đăng ký hội viên năm (" + order.getAmount() + "đ) thành công.";
            default:
                return "Nạp " + order.getAmount() + "đ vào ví thành công.";
        }
    }
}
