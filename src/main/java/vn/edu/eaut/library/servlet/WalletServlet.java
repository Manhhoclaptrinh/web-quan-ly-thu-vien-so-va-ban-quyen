package vn.edu.eaut.library.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.library.dao.TransactionDAO;
import vn.edu.eaut.library.dao.UserDAO;
import vn.edu.eaut.library.model.Transaction;
import vn.edu.eaut.library.model.User;

import java.io.IOException;

/**
 * /wallet - Ví điện tử của Reader.
 * Dành cho người dùng không muốn đăng ký hội viên dài hạn: nạp tiền vào ví,
 * dùng để trả phí xem PDF (500đ/giờ) và tải tài liệu (50.000đ/lượt) theo nhu cầu.
 *
 * Đây là ví "giả lập" cho đồ án môn học: bấm Nạp tiền coi như thanh toán thành công ngay,
 * KHÔNG kết nối cổng thanh toán thật.
 */
@WebServlet("/wallet")
public class WalletServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);

        long balance = userDAO.getWalletBalance(user.getUserId());
        java.util.List<Transaction> history = transactionDAO.findByUser(user.getUserId());

        req.setAttribute("walletBalance", balance);
        req.setAttribute("transactions", history);
        req.getRequestDispatcher("/views/wallet.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = currentUser(req);

        long amount;
        try {
            amount = Long.parseLong(req.getParameter("amount"));
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/wallet?error=invalid-amount");
            return;
        }

        if (amount <= 0 || amount > 50_000_000L) {
            resp.sendRedirect(req.getContextPath() + "/wallet?error=invalid-amount");
            return;
        }

        userDAO.topUpWallet(user.getUserId(), amount);

        Transaction t = new Transaction();
        t.setUserId(user.getUserId());
        t.setType("TOPUP");
        t.setAmount(amount);
        t.setDescription("Nạp tiền vào ví (giả lập thanh toán)");
        transactionDAO.insert(t);

        // Đồng bộ lại số dư trong session để hiển thị đúng ngay lập tức
        user.setWalletBalance(userDAO.getWalletBalance(user.getUserId()));

        resp.sendRedirect(req.getContextPath() + "/wallet?success=1");
    }

    private User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (User) session.getAttribute("currentUser");
    }
}
