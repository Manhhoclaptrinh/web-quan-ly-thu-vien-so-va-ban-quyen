package vn.edu.eaut.library.filter;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.library.model.User;

@WebFilter("/*")
public class AuthFilter implements Filter {

    private static final String[] PUBLIC_PATHS = {
            "/login", "/css/", "/js/", "/views/login.jsp", "/views/errors/"
    };

    private static final String[] STAFF_ONLY_PATHS = {
            "/license", "/permission", "/categories", "/video-license",
            "/video-permission", "/book-license", "/book-permission"
    };

    private static final String[] ADMIN_ONLY_PATHS = {
            "/users", "/audit-logs"
    };

    // ADMIN có khu vực riêng; không được đi vào giao diện nghiệp vụ của Reader/Librarian.
    private static final String[] ADMIN_BLOCKED_PATHS = {
            "/home", "/documents", "/videos", "/books", "/favorites", "/history",
            "/notifications", "/permission-request", "/video-permission-request",
            "/book-permission-requests", "/upload", "/download", "/video-download",
            "/book-download", "/document-preview", "/video-stream", "/book-preview",
            "/license", "/permission", "/categories", "/video-license",
            "/video-permission", "/book-license", "/book-permission"
    };

    @Override public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());

        if (isPublicPath(path)) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            response.sendRedirect(contextPath + "/login");
            return;
        }

        if (currentUser.isAdmin() && isAdminBlockedPath(path)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "ADMIN chỉ sử dụng Admin Panel để giám sát hệ thống.");
            return;
        }

        if (isStaffOnlyPath(path)
                && !(currentUser.isAdmin() || currentUser.isLibrarian())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập chức năng này.");
            return;
        }

        if (isAdminOnlyPath(path) && !currentUser.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chức năng này chỉ dành cho ADMIN.");
            return;
        }

        // AUDITOR nếu vẫn tồn tại là role riêng, không còn là alias của ADMIN.
        if (currentUser.isAuditor() && isAuditorWriteRequest(request, path)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "AUDITOR chỉ có quyền xem, không được thêm/sửa/xóa/duyệt.");
            return;
        }

        try {
            request.setAttribute("unreadNotifications",
                    new vn.edu.eaut.library.dao.NotificationDAO().countUnread(currentUser.getUserId()));
        } catch (Exception ignored) {
            request.setAttribute("unreadNotifications", 0);
        }
        chain.doFilter(req, res);
    }

    private boolean isAdminBlockedPath(String path) {
        for (String p : ADMIN_BLOCKED_PATHS) {
            if (path.equals(p) || path.startsWith(p + "/")) return true;
        }
        return false;
    }

    private boolean isAuditorWriteRequest(HttpServletRequest request, String path) {
        String method = request.getMethod();
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) return true;
        String normalized = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        String[] writeSuffixes = {"/add","/edit","/delete","/save","/approve","/reject","/toggle-status","/remove","/create","/update","/grant","/revoke","/disable","/enable"};
        for (String suffix : writeSuffixes) if (normalized.endsWith(suffix)) return true;
        String action = request.getParameter("action");
        if (action != null && action.trim().toLowerCase().matches("add|remove|save|update|create|edit|delete|approve|reject|grant|revoke|toggle-status|disable|enable|mark-read|read")) return true;
        return normalized.equals("/download") || normalized.startsWith("/download/")
                || normalized.equals("/video-download") || normalized.startsWith("/video-download/")
                || normalized.equals("/book-download") || normalized.startsWith("/book-download/")
                || normalized.equals("/profile/edit") || normalized.equals("/change-password");
    }

    private boolean isPublicPath(String path) {
        for (String p : PUBLIC_PATHS) if (path.startsWith(p)) return true;
        return false;
    }
    private boolean isStaffOnlyPath(String path) {
        for (String p : STAFF_ONLY_PATHS) if (path.equals(p) || path.startsWith(p + "/")) return true;
        return false;
    }
    private boolean isAdminOnlyPath(String path) {
        for (String p : ADMIN_ONLY_PATHS) if (path.equals(p) || path.startsWith(p + "/")) return true;
        return false;
    }
    @Override public void destroy() {}
}
