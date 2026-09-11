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
import vn.edu.eaut.library.dao.HistoryDAO;
import vn.edu.eaut.library.model.AccessHistory;
import vn.edu.eaut.library.model.Book;
import vn.edu.eaut.library.model.User;

@WebServlet("/books/download")
public class BookDownloadServlet extends HttpServlet {
    private final BookDAO bookDAO = new BookDAO();
    private final BookPermissionDAO permissionDAO = new BookPermissionDAO();
    private final BookLicenseDAO licenseDAO = new BookLicenseDAO();
    private final HistoryDAO historyDAO = new HistoryDAO();

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
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID sách không hợp lệ.");
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

        // AUDITOR là read-only, không tải xuống.
        if (user.isAuditor()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "AUDITOR chỉ có quyền xem, không được tải file.");
            return;
        }

        boolean staff = user.isAdmin() || user.isLibrarian();
        if (!staff) {
            if (!licenseDAO.hasValidLicense(id)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Sách chưa có bản quyền đang hiệu lực.");
                return;
            }
            if (!permissionDAO.hasPermission(user.getUserId(), id, "DOWNLOAD")) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Bạn chưa được cấp quyền tải sách này.");
                return;
            }
        }

        Path file = resolveSafeFile(req.getServletContext(), book.getFilePath());
        if (file == null || !Files.isRegularFile(file)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy file sách.");
            return;
        }

        String fileName = file.getFileName().toString().replace("\"", "");
        String mime = req.getServletContext().getMimeType(fileName);
        resp.setContentType(mime != null ? mime : "application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setContentLengthLong(Files.size(file));

        try (InputStream in = Files.newInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[65536];
            int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
        }

        AccessHistory h = new AccessHistory();
        h.setUserId(user.getUserId());
        h.setTargetType("BOOK");
        h.setBookId(id);
        h.setActionType("DOWNLOAD");
        h.setIpAddress(req.getRemoteAddr());
        try { historyDAO.insert(h); } catch (Exception ignored) {}
    }

    private Path resolveSafeFile(ServletContext context, String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) return null;
        String normalizedPath = filePath.startsWith("/uploads/")
                ? "/WEB-INF" + filePath : filePath;
        if (!normalizedPath.startsWith("/WEB-INF/uploads/books/")) return null;

        String real = context.getRealPath(normalizedPath);
        String root = context.getRealPath("/WEB-INF/uploads/books/");
        if (real == null || root == null) return null;

        Path file = Paths.get(real).normalize();
        Path uploadRoot = Paths.get(root).normalize();
        return file.startsWith(uploadRoot) ? file : null;
    }
}
