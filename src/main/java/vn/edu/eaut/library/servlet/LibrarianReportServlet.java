package vn.edu.eaut.library.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.eaut.library.model.User;
import vn.edu.eaut.library.dao.LibrarianReportDAO;

/**
 * Báo cáo thống kê nghiệp vụ của Thủ thư.
 * Chỉ LIBRARIAN được truy cập; ADMIN dùng Admin Panel riêng.
 */
@WebServlet("/librarian/reports")
public class LibrarianReportServlet extends HttpServlet {

    private final LibrarianReportDAO reportDAO = new LibrarianReportDAO();

    private User requireLibrarian(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("currentUser");

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }

        if (!user.isLibrarian()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Báo cáo thống kê này chỉ dành cho LIBRARIAN.");
            return null;
        }

        return user;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (requireLibrarian(req, resp) == null) return;

        try {
            req.setAttribute("overview", reportDAO.getOverview());
            req.setAttribute("accessLast7Days", reportDAO.accessLast7Days());
            req.setAttribute("contentByType", reportDAO.contentByType());
            req.setAttribute("topContent", reportDAO.topContent(10));
            req.setAttribute("categoryStats", reportDAO.categoryStats());
            req.setAttribute("requestStats", reportDAO.requestStats());
            req.setAttribute("reviewStats", reportDAO.reviewStats());
            req.setAttribute("transactionStats", reportDAO.transactionStats());
            req.setAttribute("licenseStats", reportDAO.licenseStats());

            req.getRequestDispatcher("/WEB-INF/views/librarian-reports.jsp")
                    .forward(req, resp);
        } catch (RuntimeException e) {
            throw new ServletException("Không thể tải báo cáo thống kê thủ thư.", e);
        }
    }
}
