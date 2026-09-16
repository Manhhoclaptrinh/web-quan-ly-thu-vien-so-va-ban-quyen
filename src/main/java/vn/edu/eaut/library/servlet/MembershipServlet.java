package vn.edu.eaut.library.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.library.dao.MembershipDAO;
import vn.edu.eaut.library.dao.TransactionDAO;
import vn.edu.eaut.library.model.Membership;
import vn.edu.eaut.library.model.Transaction;
import vn.edu.eaut.library.model.User;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * /membership - Đăng ký hội viên theo tháng (100.000đ) hoặc theo năm (500.000đ).
 * Hội viên được xem PDF và tải tài liệu THOẢI MÁI, không bị tính phí theo lượt.
 *
 * Đây là luồng thanh toán trực tiếp (không trừ vào Ví) - giả lập cho đồ án môn học:
 * bấm "Xác nhận thanh toán" coi như thanh toán thành công ngay.
 */
@WebServlet("/membership")
public class MembershipServlet extends HttpServlet {

    private static final long PRICE_MONTHLY = 100_000L;
    private static final long PRICE_YEARLY = 500_000L;

    private final MembershipDAO membershipDAO = new MembershipDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        Membership active = membershipDAO.findActiveByUser(user.getUserId());
        req.setAttribute("activeMembership", active);
        req.setAttribute("priceMonthly", PRICE_MONTHLY);
        req.setAttribute("priceYearly", PRICE_YEARLY);
        req.getRequestDispatcher("/views/membership.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = currentUser(req);
        String planType = req.getParameter("planType");

        if (!"MONTHLY".equals(planType) && !"YEARLY".equals(planType)) {
            resp.sendRedirect(req.getContextPath() + "/membership?error=invalid-plan");
            return;
        }

        long price = "MONTHLY".equals(planType) ? PRICE_MONTHLY : PRICE_YEARLY;

        // Nếu đang là hội viên còn hạn thì gia hạn tiếp từ ngày hết hạn cũ (không mất thời gian còn lại).
        // Nếu chưa phải hội viên / đã hết hạn thì tính từ thời điểm hiện tại.
        Membership current = membershipDAO.findActiveByUser(user.getUserId());
        LocalDateTime start = (current != null) ? current.getEndDate() : LocalDateTime.now();
        LocalDateTime end = "MONTHLY".equals(planType) ? start.plusMonths(1) : start.plusYears(1);

        Membership m = new Membership();
        m.setUserId(user.getUserId());
        m.setPlanType(planType);
        m.setPrice(price);
        m.setStartDate(start);
        m.setEndDate(end);
        membershipDAO.insert(m);

        Transaction t = new Transaction();
        t.setUserId(user.getUserId());
        t.setType("MONTHLY".equals(planType) ? "MEMBERSHIP_MONTHLY" : "MEMBERSHIP_YEARLY");
        t.setAmount(-price);
        t.setDescription(("MONTHLY".equals(planType) ? "Đăng ký hội viên tháng" : "Đăng ký hội viên năm")
                + " (thanh toán trực tiếp - giả lập)");
        transactionDAO.insert(t);

        resp.sendRedirect(req.getContextPath() + "/membership?success=1");
    }

    private User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (User) session.getAttribute("currentUser");
    }
}
