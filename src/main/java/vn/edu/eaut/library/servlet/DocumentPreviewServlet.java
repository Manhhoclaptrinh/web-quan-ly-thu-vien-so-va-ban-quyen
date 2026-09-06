package vn.edu.eaut.library.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.library.dao.DocumentDAO;
import vn.edu.eaut.library.dao.LicenseDAO;
import vn.edu.eaut.library.dao.PermissionDAO;
import vn.edu.eaut.library.model.Document;
import vn.edu.eaut.library.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet("/documents/preview")
public class DocumentPreviewServlet extends HttpServlet {
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();
    private final LicenseDAO licenseDAO = new LicenseDAO();

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

        Document document = documentDAO.findById(id);
        if (document == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tài liệu.");
            return;
        }

        if (!"AVAILABLE".equalsIgnoreCase(document.getStatus())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Tài liệu đang bị vô hiệu hóa.");
            return;
        }

        boolean staff = user.isAdmin() || user.isLibrarian();
        boolean allowed;

        if (staff) {
            allowed = true;
        } else if (!licenseDAO.hasValidLicense(id)) {
            allowed = false;
        } else {
            boolean isPublic = "PUBLIC".equalsIgnoreCase(document.getAccessLevel());
            allowed = isPublic
                    || permissionDAO.hasPermission(user.getUserId(), id, "VIEW");
        }

        if (!allowed) {
            // Không chuyển về /login hoặc /home: người dùng đã đăng nhập nhưng thiếu quyền.
            resp.sendRedirect(req.getContextPath()
                    + "/documents/detail?id=" + id + "&error=no-view-permission");
            return;
        }

        String filePath = document.getFilePath();
        if (filePath == null || filePath.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Tài liệu chưa có file.");
            return;
        }

        if (filePath.startsWith("/uploads/")) {
            filePath = "/WEB-INF" + filePath;
        } else if (!filePath.startsWith("/WEB-INF/uploads/")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Đường dẫn file không hợp lệ.");
            return;
        }

        if (!filePath.toLowerCase().endsWith(".pdf")) {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    "Chỉ hỗ trợ xem trước file PDF.");
            return;
        }

        ServletContext context = getServletContext();
        String real = context.getRealPath(filePath);
        String uploadRootReal = context.getRealPath("/WEB-INF/uploads/");

        if (real == null || uploadRootReal == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Không xác định được file trên server.");
            return;
        }

        Path file = Paths.get(real).normalize();
        Path uploadRoot = Paths.get(uploadRootReal).normalize();

        if (!file.startsWith(uploadRoot) || !Files.isRegularFile(file)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy file PDF.");
            return;
        }

        String fileName = file.getFileName().toString().replace("\"", "");
        resp.reset();
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        resp.setContentLengthLong(Files.size(file));
        resp.setHeader("X-Content-Type-Options", "nosniff");

        try (InputStream in = Files.newInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        }
    }
}
