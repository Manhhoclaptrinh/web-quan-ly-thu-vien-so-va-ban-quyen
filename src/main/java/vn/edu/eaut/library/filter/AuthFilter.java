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

    // Các đường dẫn không cần đăng nhập
    private static final String[] PUBLIC_PATHS = {
            "/login", "/css/", "/views/login.jsp"
    };

    // Các đường dẫn chỉ ADMIN/LIBRARIAN được truy cập
    private static final String[] STAFF_ONLY_PATHS = {
            "/license", "/permission", "/categories"
    };

    private static final String[] ADMIN_ONLY_PATHS = { "/users", "/audit-logs" };

    @Override
    public void init(FilterConfig filterConfig) {
        // không cần khởi tạo gì thêm
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());

        // Cho qua các đường dẫn public
        if (isPublicPath(path)) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            response.sendRedirect(contextPath + "/login");
            return;
        }

        if (isStaffOnlyPath(path) && !(currentUser.isAdmin() || currentUser.isLibrarian())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng này.");
            return;
        }
        if (isAdminOnlyPath(path) && !currentUser.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chức năng này chỉ dành cho ADMIN.");
            return;
        }

        try { request.setAttribute("unreadNotifications", new vn.edu.eaut.library.dao.NotificationDAO().countUnread(currentUser.getUserId())); } catch (Exception ignored) { request.setAttribute("unreadNotifications", 0); }
        chain.doFilter(req, res);
    }

    private boolean isPublicPath(String path) {
        for (String p : PUBLIC_PATHS) {
            if (path.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStaffOnlyPath(String path) {
        for (String p : STAFF_ONLY_PATHS) {
            // Phải khớp chính xác hoặc là sub-path.
            // Tránh lỗi "/permission" vô tình chặn "/permission-request".
            if (path.equals(p) || path.startsWith(p + "/")) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdminOnlyPath(String path) {
        for (String p : ADMIN_ONLY_PATHS) {
            if (path.equals(p) || path.startsWith(p + "/")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        // không cần giải phóng gì thêm
    }
}
