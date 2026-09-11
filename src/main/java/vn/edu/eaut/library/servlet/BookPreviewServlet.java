package vn.edu.eaut.library.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.library.dao.BookDAO;
import vn.edu.eaut.library.dao.BookLicenseDAO;
import vn.edu.eaut.library.dao.BookPermissionDAO;
import vn.edu.eaut.library.model.Book;
import vn.edu.eaut.library.model.User;

@WebServlet("/books/preview")
public class BookPreviewServlet extends HttpServlet {
    private final BookDAO bookDAO = new BookDAO();
    private final BookPermissionDAO permissionDAO = new BookPermissionDAO();
    private final BookLicenseDAO licenseDAO = new BookLicenseDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("currentUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=session");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID không hợp lệ.");
            return;
        }

        Book book = bookDAO.findById(id);
        if (book == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy sách.");
            return;
        }
        if (!"AVAILABLE".equalsIgnoreCase(book.getStatus())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Sách đang bị vô hiệu hóa.");
            return;
        }

        boolean allowed;
        if (user.isAdmin() || user.isLibrarian() || user.isAuditor()) {
            allowed = true;
        } else if (!licenseDAO.hasValidLicense(id)) {
            allowed = false;
        } else {
            allowed = "PUBLIC".equalsIgnoreCase(book.getAccessLevel())
                    || permissionDAO.hasPermission(user.getUserId(), id, "VIEW");
        }

        if (!allowed) {
            resp.sendRedirect(req.getContextPath()
                    + "/books/detail?id=" + id + "&error=no-view-permission");
            return;
        }

        String filePath = book.getFilePath();
        if (filePath == null || !filePath.toLowerCase().endsWith(".pdf")) {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    "Chỉ hỗ trợ xem trước file PDF.");
            return;
        }

        String normalized = filePath.startsWith("/uploads/")
                ? "/WEB-INF" + filePath : filePath;
        if (!normalized.startsWith("/WEB-INF/uploads/books/")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Đường dẫn file không hợp lệ.");
            return;
        }

        ServletContext context = getServletContext();
        String real = context.getRealPath(normalized);
        String root = context.getRealPath("/WEB-INF/uploads/books/");
        if (real == null || root == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Không xác định được file trên server.");
            return;
        }

        Path file = Paths.get(real).normalize();
        Path uploadRoot = Paths.get(root).normalize();
        if (!file.startsWith(uploadRoot) || !Files.isRegularFile(file)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy file PDF.");
            return;
        }

        resp.reset();
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition",
                "inline; filename=\"" + file.getFileName().toString().replace("\"", "") + "\"");
        resp.setContentLengthLong(Files.size(file));

        try (InputStream in = Files.newInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
        }
    }
}
