package vn.edu.eaut.library.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.eaut.library.dao.HistoryDAO;
import vn.edu.eaut.library.dao.UserDAO;
import vn.edu.eaut.library.model.AccessHistory;
import vn.edu.eaut.library.model.User;
import vn.edu.eaut.library.utils.PasswordUtil;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final HistoryDAO historyDAO = new HistoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            resp.sendRedirect(req.getContextPath() + ("ADMIN".equalsIgnoreCase(((User) session.getAttribute("currentUser")).getRole()) ? "/admin" : "/home"));
            return;
        }
        req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
            return;
        }

        User user = userDAO.findByUsername(username.trim());

        if (user == null || !PasswordUtil.matches(password, user.getPassword())) {
            req.setAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng.");
            req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
            return;
        }

        if ("LOCKED".equalsIgnoreCase(user.getStatus())) {
            req.setAttribute("error", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
            req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("currentUser", user);
        session.setMaxInactiveInterval(30 * 60); // 30 phút

        // Ghi lịch sử đăng nhập (document_id = null vì hành động không gắn tài liệu cụ thể)
        AccessHistory history = new AccessHistory();
        history.setUserId(user.getUserId());
        history.setDocumentId(null);
        history.setActionType("LOGIN");
        history.setIpAddress(req.getRemoteAddr());
        safeLog(history);

        resp.sendRedirect(req.getContextPath() + (user.isAdmin() ? "/admin" : "/home"));
    }

    private void safeLog(AccessHistory history) {
        try {
            historyDAO.insert(history);
        } catch (Exception e) {
            // Không để lỗi ghi log ảnh hưởng luồng đăng nhập chính
            e.printStackTrace();
        }
    }
}
