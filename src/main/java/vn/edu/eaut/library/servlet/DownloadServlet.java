package vn.edu.eaut.library.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.library.dao.DocumentDAO;
import vn.edu.eaut.library.dao.HistoryDAO;
import vn.edu.eaut.library.dao.LicenseDAO;
import vn.edu.eaut.library.dao.PermissionDAO;
import vn.edu.eaut.library.model.AccessHistory;
import vn.edu.eaut.library.model.Document;
import vn.edu.eaut.library.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet("/documents/download")
public class DownloadServlet extends HttpServlet {
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();
    private final HistoryDAO historyDAO = new HistoryDAO();
    private final LicenseDAO licenseDAO = new LicenseDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("currentUser");

        // Không để servlet tự gây NullPointerException khi session hết hạn.
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=session");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID tài liệu không hợp lệ.");
            return;
        }

        Document doc = documentDAO.findById(id);
        if (doc == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tài liệu.");
            return;
        }

        if (!"AVAILABLE".equalsIgnoreCase(doc.getStatus())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Tài liệu đang bị vô hiệu hóa.");
            return;
        }

        boolean staff = user.isAdmin() || user.isLibrarian();

        // ADMIN/LIBRARIAN được xem/tải tài liệu mà không cần permission riêng.
        // READER phải có license hợp lệ và permission DOWNLOAD.
        if (!staff) {
            if (!licenseDAO.hasValidLicense(id)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Tài liệu chưa có bản quyền đang hiệu lực.");
                return;
            }

            if (!permissionDAO.hasPermission(user.getUserId(), id, "DOWNLOAD")) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Bạn chưa được cấp quyền tải tài liệu này.");
                return;
            }
        }

        String filePath = doc.getFilePath();
        if (filePath == null || filePath.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Tài liệu chưa có file.");
            return;
        }

        // Database của project có thể đang lưu /uploads/xxx.pdf hoặc
        // /WEB-INF/uploads/xxx.pdf. Chuẩn hóa cả hai về vùng an toàn.
        if (filePath.startsWith("/uploads/")) {
            filePath = "/WEB-INF" + filePath;
        } else if (!filePath.startsWith("/WEB-INF/uploads/")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Đường dẫn file không hợp lệ.");
            return;
        }

        ServletContext context = getServletContext();
        String real = context.getRealPath(filePath);
        String uploadRootReal = context.getRealPath("/WEB-INF/uploads/");

        if (real == null || uploadRootReal == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không xác định được file trên server.");
            return;
        }

        Path path = Paths.get(real).normalize();
        Path uploadRoot = Paths.get(uploadRootReal).normalize();

        // Chặn path traversal và chỉ cho phép file bên trong WEB-INF/uploads.
        if (!path.startsWith(uploadRoot) || !Files.isRegularFile(path)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy file.");
            return;
        }

        String fileName = path.getFileName().toString();
        String mimeType = context.getMimeType(fileName);
        resp.setContentType(mimeType != null ? mimeType : "application/octet-stream");
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName.replace("\"", "") + "\"");
        resp.setContentLengthLong(Files.size(path));

        try (InputStream in = Files.newInputStream(path);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        }

        AccessHistory h = new AccessHistory();
        h.setUserId(user.getUserId());
        h.setDocumentId(id);
        h.setActionType("DOWNLOAD");
        h.setIpAddress(req.getRemoteAddr());
        try {
            historyDAO.insert(h);
        } catch (Exception ignored) {
            // Không làm hỏng việc tải file nếu ghi lịch sử thất bại.
        }
    }
}
