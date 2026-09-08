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
import vn.edu.eaut.library.dao.HistoryDAO;
import vn.edu.eaut.library.dao.VideoDAO;
import vn.edu.eaut.library.dao.VideoLicenseDAO;
import vn.edu.eaut.library.dao.VideoPermissionDAO;
import vn.edu.eaut.library.model.AccessHistory;
import vn.edu.eaut.library.model.User;
import vn.edu.eaut.library.model.Video;

@WebServlet("/videos/download")
public class VideoDownloadServlet extends HttpServlet {
    private final VideoDAO videoDAO = new VideoDAO();
    private final VideoPermissionDAO permissionDAO = new VideoPermissionDAO();
    private final HistoryDAO historyDAO = new HistoryDAO();
    private final VideoLicenseDAO licenseDAO = new VideoLicenseDAO();

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
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID video không hợp lệ.");
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

        if (!staff) {
            if (!licenseDAO.hasValidLicense(id)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Video chưa có bản quyền đang hiệu lực.");
                return;
            }
            if (!permissionDAO.hasPermission(user.getUserId(), id, "DOWNLOAD")) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Bạn chưa được cấp quyền tải video này.");
                return;
            }
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

        Path path = Paths.get(real).normalize();
        Path uploadRoot = Paths.get(uploadRootReal).normalize();
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
            byte[] buffer = new byte[65536];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        }

        AccessHistory h = new AccessHistory();
        h.setUserId(user.getUserId());
        h.setTargetType("VIDEO");
        h.setVideoId(id);
        h.setActionType("DOWNLOAD");
        h.setIpAddress(req.getRemoteAddr());
        try {
            historyDAO.insert(h);
        } catch (Exception ignored) {
        }
    }
}