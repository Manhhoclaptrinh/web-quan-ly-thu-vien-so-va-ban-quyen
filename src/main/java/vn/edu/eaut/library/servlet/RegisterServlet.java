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

/**
 * Đăng ký tài khoản tự do (READER) - KHÔNG cần quản trị viên duyệt/cấp.
 * Theo yêu cầu: bỏ hẳn cơ chế "chờ admin tạo tài khoản" trước đây.
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = trim(req.getParameter("username"));
        String fullName = trim(req.getParameter("fullName"));
        String email = trim(req.getParameter("email"));
        String password = req.getParameter("password");

        String error = validate(username, fullName, email, password);

        if (error == null && userDAO.findByUsername(username) != null) {
            error = "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác.";
        }

        if (error != null) {
            req.setAttribute("registerError", error);
            req.setAttribute("oldUsername", username);
            req.setAttribute("oldFullName", fullName);
            req.setAttribute("oldEmail", email);
            req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(PasswordUtil.hash(password));
        user.setRole("READER");
        user.setStatus("ACTIVE");

        try {
            userDAO.insert(user);
        } catch (RuntimeException e) {
            // Khả năng cao nhất là trùng email (UNIQUE) vì username đã kiểm tra ở trên
            req.setAttribute("registerError", "Không thể tạo tài khoản (có thể email đã được dùng). Vui lòng thử lại.");
            req.setAttribute("oldUsername", username);
            req.setAttribute("oldFullName", fullName);
            req.setAttribute("oldEmail", email);
            req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/login?registered=1");
    }

    private String validate(String username, String fullName, String email, String password) {
        if (username == null || username.length() < 4) {
            return "Tên đăng nhập cần ít nhất 4 ký tự.";
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return "Tên đăng nhập chỉ được chứa chữ, số và dấu gạch dưới.";
        }
        if (fullName == null || fullName.isEmpty()) {
            return "Vui lòng nhập họ và tên.";
        }
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "Email không hợp lệ.";
        }
        if (password == null || password.length() < 6) {
            return "Mật khẩu cần ít nhất 6 ký tự.";
        }
        return null;
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
