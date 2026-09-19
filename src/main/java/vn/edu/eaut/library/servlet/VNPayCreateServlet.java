package vn.edu.eaut.library.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.library.dao.PaymentOrderDAO;
import vn.edu.eaut.library.model.PaymentOrder;
import vn.edu.eaut.library.model.User;
import vn.edu.eaut.library.payment.VNPayConfig;
import vn.edu.eaut.library.payment.VNPayUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * /payment/vnpay/create - Tạo 1 đơn thanh toán (PENDING) và chuyển hướng
 * người dùng sang trang thanh toán VNPay.
 *
 * Nhận từ form (wallet.jsp / membership.jsp):
 *   - orderType: WALLET_TOPUP | MEMBERSHIP_MONTHLY | MEMBERSHIP_YEARLY
 *   - amount   : số tiền VND (bên membership.jsp gửi cứng theo giá gói)
 */
@WebServlet("/payment/vnpay/create")
public class VNPayCreateServlet extends HttpServlet {

    private final PaymentOrderDAO paymentOrderDAO = new PaymentOrderDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String orderType = req.getParameter("orderType");
        long amount;
        try {
            amount = Long.parseLong(req.getParameter("amount"));
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/wallet?error=invalid-amount");
            return;
        }

        if (amount <= 0 || amount > 50_000_000L
                || (!"WALLET_TOPUP".equals(orderType)
                    && !"MEMBERSHIP_MONTHLY".equals(orderType)
                    && !"MEMBERSHIP_YEARLY".equals(orderType))) {
            resp.sendRedirect(req.getContextPath() + "/wallet?error=invalid-amount");
            return;
        }

        String txnRef = VNPayUtil.generateTxnRef();

        PaymentOrder order = new PaymentOrder();
        order.setTxnRef(txnRef);
        order.setUserId(user.getUserId());
        order.setOrderType(orderType);
        order.setAmount(amount);
        paymentOrderDAO.insert(order);

        String ipAddr = req.getRemoteAddr();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", VNPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu nhân 100
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", buildOrderInfo(orderType, amount));
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", VNPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", (ipAddr == null || ipAddr.isEmpty()) ? "127.0.0.1" : ipAddr);
        vnpParams.put("vnp_CreateDate", now.format(fmt));
        vnpParams.put("vnp_ExpireDate", now.plusMinutes(15).format(fmt));
        // Không set vnp_BankCode -> VNPay tự hiển thị đầy đủ màn hình chọn
        // QR / thẻ ATM nội địa / thẻ quốc tế cho khách chọn.

        String paymentUrl = VNPayUtil.buildPaymentUrl(vnpParams, VNPayConfig.getHashSecret(), VNPayConfig.getPayUrl());
        resp.sendRedirect(paymentUrl);
    }

    private String buildOrderInfo(String orderType, long amount) {
        // Luu y: VNPAY yeu cau OrderInfo la tieng Viet KHONG DAU, khong ky tu dac biet
        switch (orderType) {
            case "MEMBERSHIP_MONTHLY":
                return "Thanh toan hoi vien thang " + amount + " VND";
            case "MEMBERSHIP_YEARLY":
                return "Thanh toan hoi vien nam " + amount + " VND";
            default:
                return "Nap tien vi thu vien so " + amount + " VND";
        }
    }
}
