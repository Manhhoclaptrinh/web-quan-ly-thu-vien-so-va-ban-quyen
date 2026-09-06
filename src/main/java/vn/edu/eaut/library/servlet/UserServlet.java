package vn.edu.eaut.library.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.eaut.library.dao.UserDAO;
import vn.edu.eaut.library.model.User;
import vn.edu.eaut.library.utils.PasswordUtil;

import java.io.IOException;

@WebServlet("/users/*")
public class UserServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final vn.edu.eaut.library.dao.AuditLogDAO auditLogDAO = new vn.edu.eaut.library.dao.AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) {
            req.setAttribute("users", userDAO.findAll());
            req.getRequestDispatcher("/views/users.jsp").forward(req, resp);
            return;
        }
        if ("/add".equals(path)) {
            req.getRequestDispatcher("/views/user-form.jsp").forward(req, resp);
            return;
        }
        if ("/edit".equals(path)) {
            User user = findUser(req, resp);
            if (user == null) return;
            req.setAttribute("user", user);
            req.getRequestDispatcher("/views/user-form.jsp").forward(req, resp);
            return;
        }
        if ("/delete".equals(path)) {
            int id = parseId(req, resp);
            if (id < 0) return;
            User current = (User) req.getSession().getAttribute("currentUser");
            if (id == current.getUserId()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Không thể tự xóa tài khoản đang đăng nhập.");
                return;
            }
            userDAO.delete(id); auditLogDAO.insert(current.getUserId(),"DELETE_USER","USER",id,"Xóa người dùng #"+id,req.getRemoteAddr());
            resp.sendRedirect(req.getContextPath() + "/users");
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        if ("/save".equals(path)) {
            save(req, resp);
            return;
        }
        if ("/toggle-status".equals(path)) {
            int id = Integer.parseInt(req.getParameter("id"));
            User current = (User) req.getSession().getAttribute("currentUser");
            if (id == current.getUserId()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Không thể tự khóa/mở khóa tài khoản đang đăng nhập.");
                return;
            }
            userDAO.toggleStatus(id); auditLogDAO.insert(current.getUserId(),"TOGGLE_USER_STATUS","USER",id,"Thay đổi trạng thái người dùng #"+id,req.getRemoteAddr());
            resp.sendRedirect(req.getContextPath() + "/users");
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void save(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String idParam = trim(req.getParameter("userId"));
        String username = trim(req.getParameter("username"));
        String password = req.getParameter("password");
        String fullName = trim(req.getParameter("fullName"));
        String email = trim(req.getParameter("email"));
        String role = trim(req.getParameter("role"));
        String status = trim(req.getParameter("status"));

        if (username == null || username.isEmpty() || fullName == null || fullName.isEmpty()
                || role == null || status == null) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ các trường bắt buộc.");
            req.getRequestDispatcher("/views/user-form.jsp").forward(req, resp);
            return;
        }

        try {
            User user = new User();
            user.setUsername(username);
            user.setFullName(fullName);
            user.setEmail(email == null || email.isEmpty() ? null : email);
            user.setRole(role);
            user.setStatus(status);

            if (idParam == null || idParam.isEmpty()) {
                if (password == null || password.trim().isEmpty()) {
                    req.setAttribute("error", "Tài khoản mới bắt buộc phải có mật khẩu.");
                    req.getRequestDispatcher("/views/user-form.jsp").forward(req, resp);
                    return;
                }
                user.setPassword(PasswordUtil.hash(password));
                userDAO.insert(user); User current=(User)req.getSession().getAttribute("currentUser"); auditLogDAO.insert(current.getUserId(),"CREATE_USER","USER",null,"Tạo người dùng: "+username,req.getRemoteAddr());
            } else {
                int id = Integer.parseInt(idParam);
                user.setUserId(id);
                User old = userDAO.findById(id);
                if (old == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                if (password != null && !password.trim().isEmpty()) {
                    user.setPassword(PasswordUtil.hash(password));
                    userDAO.updateWithPassword(user); auditLogDAO.insert(((User)req.getSession().getAttribute("currentUser")).getUserId(),"UPDATE_USER","USER",id,"Cập nhật người dùng #"+id,req.getRemoteAddr());
                } else {
                    userDAO.update(user); auditLogDAO.insert(((User)req.getSession().getAttribute("currentUser")).getUserId(),"UPDATE_USER","USER",id,"Cập nhật người dùng #"+id,req.getRemoteAddr());
                }
                User current = (User) req.getSession().getAttribute("currentUser");
                if (current.getUserId() == id) {
                    current.setFullName(user.getFullName());
                    current.setEmail(user.getEmail());
                    current.setRole(user.getRole());
                    current.setStatus(user.getStatus());
                    req.getSession().setAttribute("currentUser", current);
                }
            }
            resp.sendRedirect(req.getContextPath() + "/users");
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID không hợp lệ.");
        } catch (RuntimeException e) {
            req.setAttribute("error", "Không thể lưu tài khoản. Username/email có thể đã tồn tại.");
            req.setAttribute("user", idParam == null || idParam.isEmpty() ? null : userDAO.findById(Integer.parseInt(idParam)));
            req.getRequestDispatcher("/views/user-form.jsp").forward(req, resp);
        }
    }

    private User findUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = parseId(req, resp);
        return id < 0 ? null : userDAO.findById(id);
    }

    private int parseId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try { return Integer.parseInt(req.getParameter("id")); }
        catch (Exception e) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID không hợp lệ."); return -1; }
    }

    private String trim(String value) { return value == null ? null : value.trim(); }
}
