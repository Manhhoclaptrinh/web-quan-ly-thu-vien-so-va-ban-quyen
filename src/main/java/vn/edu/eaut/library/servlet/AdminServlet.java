package vn.edu.eaut.library.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.eaut.library.dao.AdminDashboardDAO;
import vn.edu.eaut.library.dao.AuditLogDAO;
import vn.edu.eaut.library.dao.BookDAO;
import vn.edu.eaut.library.dao.BookLicenseDAO;
import vn.edu.eaut.library.dao.BookPermissionDAO;
import vn.edu.eaut.library.dao.BookPermissionRequestDAO;
import vn.edu.eaut.library.dao.DocumentDAO;
import vn.edu.eaut.library.dao.LicenseDAO;
import vn.edu.eaut.library.dao.PermissionDAO;
import vn.edu.eaut.library.dao.PermissionRequestDAO;
import vn.edu.eaut.library.dao.UserDAO;
import vn.edu.eaut.library.dao.VideoDAO;
import vn.edu.eaut.library.dao.VideoLicenseDAO;
import vn.edu.eaut.library.dao.VideoPermissionDAO;
import vn.edu.eaut.library.dao.VideoPermissionRequestDAO;
import vn.edu.eaut.library.model.User;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {

    private final AdminDashboardDAO dashboardDAO = new AdminDashboardDAO();
    private final UserDAO userDAO = new UserDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final VideoDAO videoDAO = new VideoDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();
    private final PermissionRequestDAO requestDAO = new PermissionRequestDAO();
    private final LicenseDAO licenseDAO = new LicenseDAO();
    private final VideoPermissionDAO videoPermissionDAO = new VideoPermissionDAO();
    private final VideoPermissionRequestDAO videoRequestDAO = new VideoPermissionRequestDAO();
    private final VideoLicenseDAO videoLicenseDAO = new VideoLicenseDAO();
    private final BookPermissionDAO bookPermissionDAO = new BookPermissionDAO();
    private final BookPermissionRequestDAO bookRequestDAO = new BookPermissionRequestDAO();
    private final BookLicenseDAO bookLicenseDAO = new BookLicenseDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!currentUser.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin Panel chỉ dành cho ADMIN.");
            return;
        }

        String path = request.getPathInfo();
        if (path == null || "/".equals(path) || "".equals(path)) {
            renderDashboard(request, response);
            return;
        }

        switch (path) {
            case "/users": renderUsers(request, response); break;
            case "/content": renderContent(request, response); break;
            case "/permissions": renderPermissions(request, response); break;
            case "/reports": renderReports(request, response); break;
            case "/audit-logs": renderAuditLogs(request, response); break;
            default: response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void renderDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Map<String, Integer> overview = dashboardDAO.getOverview();
            request.setAttribute("overview", overview);
            request.getRequestDispatcher("/WEB-INF/views/admin-dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Unable to load admin dashboard", e);
        }
    }

    private void renderUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("users", userDAO.findAll());
        request.getRequestDispatcher("/WEB-INF/views/admin-users.jsp").forward(request, response);
    }

    private void renderContent(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("documents", documentDAO.findAll());
        request.setAttribute("videos", videoDAO.findAll());
        request.setAttribute("books", bookDAO.findAll());
        request.getRequestDispatcher("/WEB-INF/views/admin-content.jsp").forward(request, response);
    }

    private void renderPermissions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("permissions", permissionDAO.findAll());
        request.setAttribute("permissionRequests", requestDAO.findAll());
        request.setAttribute("videoPermissions", videoPermissionDAO.findAll());
        request.setAttribute("videoPermissionRequests", videoRequestDAO.findAll());
        request.setAttribute("bookPermissions", bookPermissionDAO.findAll());
        request.setAttribute("bookPermissionRequests", bookRequestDAO.findAll());
        request.setAttribute("licenses", licenseDAO.findAll());
        request.setAttribute("videoLicenses", videoLicenseDAO.findAll());
        request.setAttribute("bookLicenses", bookLicenseDAO.findAll());
        request.getRequestDispatcher("/WEB-INF/views/admin-permissions.jsp").forward(request, response);
    }

    private void renderReports(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("overview", dashboardDAO.getOverview());
            request.getRequestDispatcher("/WEB-INF/views/admin-reports.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Unable to load reports", e);
        }
    }

    private void renderAuditLogs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("auditLogs", auditLogDAO.findAll());
        request.getRequestDispatcher("/WEB-INF/views/admin-audit-logs.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Admin Panel là chế độ giám sát, không có thao tác ghi.");
    }
}
