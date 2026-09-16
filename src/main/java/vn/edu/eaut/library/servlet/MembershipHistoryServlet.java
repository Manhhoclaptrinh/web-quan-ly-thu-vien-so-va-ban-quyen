package vn.edu.eaut.library.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.eaut.library.dao.MembershipDAO;

import java.io.IOException;

/**
 * /membership-history - Bên thủ thư: xem toàn bộ lịch sử đăng ký/gia hạn hội viên
 * của mọi Reader, tìm theo tên tài khoản.
 * Quyền truy cập do AuthFilter (STAFF_ONLY_PATHS) đảm nhiệm - chỉ ADMIN/LIBRARIAN vào được.
 */
@WebServlet("/membership-history")
public class MembershipHistoryServlet extends HttpServlet {

    private final MembershipDAO membershipDAO = new MembershipDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        req.setAttribute("keyword", username);
        req.setAttribute("memberships", membershipDAO.search(username));
        req.getRequestDispatcher("/views/membership-history.jsp").forward(req, resp);
    }
}
