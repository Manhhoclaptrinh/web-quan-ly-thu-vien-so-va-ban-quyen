package vn.edu.eaut.library.servlet;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.library.dao.AuditLogDAO;
import vn.edu.eaut.library.dao.NotificationDAO;
import vn.edu.eaut.library.dao.VideoDAO;
import vn.edu.eaut.library.dao.VideoLicenseDAO;
import vn.edu.eaut.library.dao.VideoPermissionRequestDAO;
import vn.edu.eaut.library.model.User;
import vn.edu.eaut.library.model.Video;
import vn.edu.eaut.library.model.VideoPermissionRequest;

@WebServlet("/video-permission-request/*")
public class VideoPermissionRequestServlet extends HttpServlet {
    private final VideoPermissionRequestDAO dao = new VideoPermissionRequestDAO();
    private final VideoDAO videoDAO = new VideoDAO();
    private final VideoLicenseDAO licenseDAO = new VideoLicenseDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (User) session.getAttribute("currentUser");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=session");
            return;
        }

        String path = req.getPathInfo();
        if (path == null || "/".equals(path) || path.isEmpty()) {
            if (user.isAdmin() || user.isLibrarian()) {
                req.setAttribute("requests", dao.findAll());
            } else {
                req.setAttribute("requests", dao.findByUserId(user.getUserId()));
                req.setAttribute("videos", videoDAO.findAll());
            }
            req.getRequestDispatcher("/views/video-permission-requests.jsp").forward(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        User user = getCurrentUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=session");
            return;
        }

        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();

        try {
            if ("/create".equals(path)) {
                createRequest(req, resp, user);
                return;
            }
            if ("/approve".equals(path)) {
                processRequest(req, resp, user, true);
                return;
            }
            if ("/reject".equals(path)) {
                processRequest(req, resp, user, false);
                return;
            }
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID không hợp lệ.");
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void createRequest(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        if (user.isAdmin() || user.isLibrarian()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "ADMIN/LIBRARIAN không cần gửi yêu cầu cấp quyền.");
            return;
        }

        String videoParam = req.getParameter("videoId");
        String permissionType = req.getParameter("permissionType");

        if (videoParam == null || videoParam.trim().isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn video.");
        }

        int videoId = Integer.parseInt(videoParam);
        Video video = videoDAO.findById(videoId);

        if (video == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy video.");
            return;
        }

        if (!"AVAILABLE".equalsIgnoreCase(video.getStatus())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Video đang bị vô hiệu hóa.");
            return;
        }

        if (!"VIEW".equalsIgnoreCase(permissionType) && !"DOWNLOAD".equalsIgnoreCase(permissionType)) {
            throw new IllegalArgumentException("Loại quyền không hợp lệ.");
        }

        permissionType = permissionType.toUpperCase();

        vn.edu.eaut.library.dao.VideoPermissionDAO permissionDAO =
                new vn.edu.eaut.library.dao.VideoPermissionDAO();
        if (permissionDAO.hasPermission(user.getUserId(), videoId, permissionType)) {
            resp.sendRedirect(req.getContextPath() + "/video-permission-request?error=already-granted");
            return;
        }

        if (dao.hasPending(user.getUserId(), videoId, permissionType)) {
            resp.sendRedirect(req.getContextPath() + "/video-permission-request?error=pending");
            return;
        }

        VideoPermissionRequest request = new VideoPermissionRequest();
        request.setUserId(user.getUserId());
        request.setVideoId(videoId);
        request.setPermissionType(permissionType);
        request.setReason(req.getParameter("reason"));

        if (!dao.insert(request)) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không thể tạo yêu cầu cấp quyền.");
            return;
        }

        try {
            auditLogDAO.insert(user.getUserId(), "REQUEST_VIDEO_PERMISSION", "VIDEO", videoId,
                    "Gửi yêu cầu quyền " + permissionType + " cho video", req.getRemoteAddr());
        } catch (Exception ignored) {
        }

        resp.sendRedirect(req.getContextPath() + "/video-permission-request?success=created");
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp,
                                 User user, boolean approve) throws IOException {
        if (!(user.isAdmin() || user.isLibrarian())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ ADMIN/LIBRARIAN được xử lý yêu cầu.");
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu ID yêu cầu.");
        }
        int requestId = Integer.parseInt(idParam);

        VideoPermissionRequest request = dao.findById(requestId);
        if (request == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy yêu cầu cấp quyền.");
            return;
        }

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            resp.sendRedirect(req.getContextPath() + "/video-permission-request?error=processed");
            return;
        }

        if (approve) {
            Timestamp expiry = parseExpiry(req.getParameter("expiryDate"));

            if (!licenseDAO.hasValidLicense(request.getVideoId())) {
                resp.sendRedirect(req.getContextPath() + "/video-permission-request?error=no-license");
                return;
            }

            if (dao.approve(requestId, user.getUserId(), expiry)) {
                String expiryMsg = expiry == null ? "không thời hạn" : expiry.toLocalDateTime().toString();
                try {
                    notificationDAO.create(
                            request.getUserId(),
                            "Yêu cầu quyền video được duyệt",
                            "Yêu cầu " + request.getPermissionType() + " cho video \""
                                    + request.getVideoTitle() + "\" đã được duyệt. Hạn quyền: " + expiryMsg + ".",
                            "PERMISSION"
                    );
                    auditLogDAO.insert(user.getUserId(), "APPROVE_VIDEO_PERMISSION", "VIDEO_PERMISSION_REQUEST",
                            requestId, "Duyệt yêu cầu " + request.getPermissionType() + " cho " + request.getVideoTitle(),
                            req.getRemoteAddr());
                } catch (Exception ignored) {
                }
            }
        } else {
            if (dao.reject(requestId, user.getUserId())) {
                try {
                    notificationDAO.create(
                            request.getUserId(),
                            "Yêu cầu quyền video bị từ chối",
                            "Yêu cầu " + request.getPermissionType() + " cho video \""
                                    + request.getVideoTitle() + "\" đã bị từ chối.",
                            "PERMISSION"
                    );
                    auditLogDAO.insert(user.getUserId(), "REJECT_VIDEO_PERMISSION", "VIDEO_PERMISSION_REQUEST",
                            requestId, "Từ chối yêu cầu quyền cho " + request.getVideoTitle(),
                            req.getRemoteAddr());
                } catch (Exception ignored) {
                }
            }
        }

        resp.sendRedirect(req.getContextPath() + "/video-permission-request");
    }

    private Timestamp parseExpiry(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Timestamp.valueOf(LocalDateTime.parse(value));
        } catch (Exception e) {
            throw new IllegalArgumentException("Ngày hết hạn không hợp lệ.");
        }
    }
}