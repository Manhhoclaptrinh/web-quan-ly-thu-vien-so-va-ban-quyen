package vn.edu.eaut.library.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.library.dao.VideoDAO;
import vn.edu.eaut.library.dao.VideoLicenseDAO;
import vn.edu.eaut.library.dao.VideoPermissionDAO;
import vn.edu.eaut.library.model.User;
import vn.edu.eaut.library.model.Video;

@WebServlet("/videos/stream")
public class VideoStreamServlet extends HttpServlet {
    private final VideoDAO videoDAO = new VideoDAO();
    private final VideoPermissionDAO permissionDAO = new VideoPermissionDAO();
    private final VideoLicenseDAO licenseDAO = new VideoLicenseDAO();

    private static final int BUFFER_SIZE = 64 * 1024; // 64KB mỗi lần đọc/ghi

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

        Video video = videoDAO.findById(id);
        if (video == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy video.");
            return;
        }

        if (!"AVAILABLE".equalsIgnoreCase(video.getStatus())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Video đang bị vô hiệu hóa.");
            return;
        }

        boolean staff = user.isAdmin() || user.isLibrarian();
        boolean allowed;
        if (staff) {
            allowed = true;
        } else if (!licenseDAO.hasValidLicense(id)) {
            allowed = false;
        } else {
            boolean isPublic = "PUBLIC".equalsIgnoreCase(video.getAccessLevel());
            allowed = isPublic || permissionDAO.hasPermission(user.getUserId(), id, "VIEW");
        }

        if (!allowed) {
            resp.sendRedirect(req.getContextPath()
                    + "/videos/detail?id=" + id + "&error=no-view-permission");
            return;
        }

        String filePath = video.getFilePath();
        if (filePath == null || filePath.trim().isEmpty() || !filePath.startsWith("/WEB-INF/uploads/videos/")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Đường dẫn video không hợp lệ.");
            return;
        }

        ServletContext context = getServletContext();
        String real = context.getRealPath(filePath);
        String uploadRootReal = context.getRealPath("/WEB-INF/uploads/videos/");
        if (real == null || uploadRootReal == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không xác định được file trên server.");
            return;
        }

        Path file = Paths.get(real).normalize();
        Path uploadRoot = Paths.get(uploadRootReal).normalize();
        if (!file.startsWith(uploadRoot) || !Files.isRegularFile(file)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy file video.");
            return;
        }

        String mimeType = context.getMimeType(file.getFileName().toString());
        if (mimeType == null) mimeType = guessVideoMime(file.getFileName().toString());

        long fileSize = Files.size(file);
        String range = req.getHeader("Range");

        resp.setHeader("Accept-Ranges", "bytes");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setContentType(mimeType);

        if (range == null) {
            // Không có Range: trả về toàn bộ file (trình duyệt sẽ tự gửi Range ở lần sau nếu cần tua).
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentLengthLong(fileSize);
            try (InputStream in = Files.newInputStream(file);
                 OutputStream out = resp.getOutputStream()) {
                copy(in, out, fileSize);
            }
            return;
        }

        long start, end;
        try {
            String[] parts = range.replace("bytes=", "").split("-");
            start = Long.parseLong(parts[0]);
            end = parts.length > 1 && !parts[1].isEmpty() ? Long.parseLong(parts[1]) : fileSize - 1;
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            resp.setHeader("Content-Range", "bytes */" + fileSize);
            return;
        }

        if (start < 0 || end >= fileSize || start > end) {
            resp.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            resp.setHeader("Content-Range", "bytes */" + fileSize);
            return;
        }

        long contentLength = end - start + 1;
        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        resp.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
        resp.setContentLengthLong(contentLength);

        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r");
             OutputStream out = resp.getOutputStream()) {
            raf.seek(start);
            byte[] buffer = new byte[BUFFER_SIZE];
            long remaining = contentLength;
            while (remaining > 0) {
                int toRead = (int) Math.min(buffer.length, remaining);
                int read = raf.read(buffer, 0, toRead);
                if (read == -1) break;
                out.write(buffer, 0, read);
                remaining -= read;
            }
        }
    }

    private void copy(InputStream in, OutputStream out, long limit) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
    }

    private String guessVideoMime(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".ogg")) return "video/ogg";
        if (lower.endsWith(".mov")) return "video/quicktime";
        if (lower.endsWith(".mkv")) return "video/x-matroska";
        return "application/octet-stream";
    }
}