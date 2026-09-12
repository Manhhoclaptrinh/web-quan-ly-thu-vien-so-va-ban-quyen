package vn.edu.eaut.library.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.library.dao.AdminDashboardDAO;
import vn.edu.eaut.library.dao.AdminSettingsDAO;
import vn.edu.eaut.library.dao.AuditLogDAO;
import vn.edu.eaut.library.model.User;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {

    private final AdminDashboardDAO dashboardDAO = new AdminDashboardDAO();
    private final AdminSettingsDAO settingsDAO = new AdminSettingsDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    /**
     * Admin Panel is a system-administration area only.
     * It deliberately does not expose user, content, permission or license CRUD.
     */
    private User admin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("currentUser");

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }

        if (!user.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin Panel chỉ dành cho ADMIN.");
            return null;
        }

        return user;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User admin = admin(req, resp);
        if (admin == null) return;

        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) {
            dashboard(req, resp);
            return;
        }

        switch (path) {
            case "/reports":
                reports(req, resp);
                break;
            case "/audit-logs":
                auditLogs(req, resp);
                break;
            case "/security":
                security(req, resp);
                break;
            case "/settings":
                settings(req, resp);
                break;
            case "/notifications":
                notifications(req, resp);
                break;

            // Intentionally removed from Admin Panel:
            // /users, /content, /content/form, /permissions, /search
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User admin = admin(req, resp);
        if (admin == null) return;

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        try {
            if ("settings-save".equals(action)) {
                saveSettings(req, resp, admin);
            } else {
                // User/content/permission/license actions are intentionally not supported.
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Admin Panel không hỗ trợ chức năng nghiệp vụ này.");
            }
        } catch (RuntimeException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void dashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("overview", dashboardDAO.getOverview());
            req.setAttribute("accessChart", dashboardDAO.accessLast7Days());
            req.setAttribute("securityEvents", dashboardDAO.recentSecurityEvents());
            req.setAttribute("settings", settingsDAO.findAll());
            forward(req, resp, "admin-dashboard.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void reports(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("overview", dashboardDAO.getOverview());
            req.setAttribute("accessChart", dashboardDAO.accessLast7Days());
            forward(req, resp, "admin-reports.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void auditLogs(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("auditLogs", auditDAO.findAll());
        forward(req, resp, "admin-audit-logs.jsp");
    }

    private void security(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("overview", dashboardDAO.getOverview());
            req.setAttribute("events", dashboardDAO.recentSecurityEvents());
            forward(req, resp, "admin-security.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void settings(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("settings", settingsDAO.findAll());
        forward(req, resp, "admin-settings.jsp");
    }

    private void notifications(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("overview", dashboardDAO.getOverview());
            req.setAttribute("events", dashboardDAO.recentSecurityEvents());
            forward(req, resp, "admin-notifications.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void saveSettings(HttpServletRequest req, HttpServletResponse resp, User admin)
            throws IOException {
        Map<String, String> values = new LinkedHashMap<>();
        String[] keys = {
                "libraryName",
                "libraryDescription",
                "maxUploadMb",
                "sessionTimeout"
        };

        for (String key : keys) {
            String value = req.getParameter(key);
            if (value != null) {
                values.put(key, value);
            }
        }

        settingsDAO.saveAll(values);
        log(admin, "UPDATE_SETTINGS", "SYSTEM", null,
                "Cập nhật cấu hình hệ thống", req);

        resp.sendRedirect(req.getContextPath() + "/admin/settings?success=1");
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, resp);
    }

    private void log(User user, String action, String targetType, Integer targetId,
                     String description, HttpServletRequest req) {
        try {
            auditDAO.insert(
                    user.getUserId(),
                    action,
                    targetType,
                    targetId,
                    description,
                    req.getRemoteAddr()
            );
        } catch (Exception ignored) {
            // Audit failure must not break the admin page.
        }
    }
}
