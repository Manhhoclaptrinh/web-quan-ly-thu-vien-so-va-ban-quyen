package vn.edu.eaut.library.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.library.dao.AuditLogDAO;
import vn.edu.eaut.library.dao.DocumentDAO;
import vn.edu.eaut.library.dao.LicenseDAO;
import vn.edu.eaut.library.dao.NotificationDAO;
import vn.edu.eaut.library.dao.PermissionRequestDAO;
import vn.edu.eaut.library.model.Document;
import vn.edu.eaut.library.model.PermissionRequest;
import vn.edu.eaut.library.model.User;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@WebServlet("/permission-request/*")
public class PermissionRequestServlet extends HttpServlet {
    private final PermissionRequestDAO dao = new PermissionRequestDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final LicenseDAO licenseDAO = new LicenseDAO();
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
            if (user.isAdmin() || user.isLibrarian() || user.isAuditor()) {
                // STAFF luôn lấy toàn bộ request để hiển thị cho Admin/Librarian.
                req.setAttribute("requests", dao.findAll());
            } else {
                req.setAttribute("requests", dao.findByUserId(user.getUserId()));
                req.setAttribute("documents", documentDAO.findAll());
            }

            req.getRequestDispatcher("/views/permission-requests.jsp").forward(req, resp);
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

        String documentParam = req.getParameter("documentId");
        String permissionType = req.getParameter("permissionType");

        if (documentParam == null || documentParam.trim().isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn tài liệu.");
        }

        int documentId = Integer.parseInt(documentParam);
        Document document = documentDAO.findById(documentId);

        if (document == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tài liệu.");
            return;
        }

        if (!"AVAILABLE".equalsIgnoreCase(document.getStatus())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Tài liệu đang bị vô hiệu hóa.");
            return;
        }

        if (!"VIEW".equalsIgnoreCase(permissionType)
                && !"DOWNLOAD".equalsIgnoreCase(permissionType)) {
            throw new IllegalArgumentException("Loại quyền không hợp lệ.");
        }

        permissionType = permissionType.toUpperCase();

        // Nếu user đã có quyền thì không cần tạo request mới.
        vn.edu.eaut.library.dao.PermissionDAO permissionDAO =
                new vn.edu.eaut.library.dao.PermissionDAO();
        if (permissionDAO.hasPermission(user.getUserId(), documentId, permissionType)) {
            resp.sendRedirect(req.getContextPath()
                    + "/permission-request?error=already-granted");
            return;
        }

        // Không chặn request chỉ vì license hiện tại chưa hợp lệ.
        // Admin/Librarian có thể xử lý request sau khi tài liệu có license.
        if (dao.hasPending(user.getUserId(), documentId, permissionType)) {
            resp.sendRedirect(req.getContextPath()
                    + "/permission-request?error=pending");
            return;
        }

        PermissionRequest request = new PermissionRequest();
        request.setUserId(user.getUserId());
        request.setDocumentId(documentId);
        request.setPermissionType(permissionType);
        request.setReason(req.getParameter("reason"));

        if (!dao.insert(request)) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể tạo yêu cầu cấp quyền.");
            return;
        }

        try {
            auditLogDAO.insert(
                    user.getUserId(),
                    "REQUEST_PERMISSION",
                    "DOCUMENT",
                    documentId,
                    "Gửi yêu cầu quyền " + permissionType,
                    req.getRemoteAddr()
            );
        } catch (Exception ignored) {
        }

        resp.sendRedirect(req.getContextPath()
                + "/permission-request?success=created");
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp,
                                User user, boolean approve) throws IOException {
        if (!(user.isAdmin() || user.isLibrarian())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chỉ ADMIN/LIBRARIAN được xử lý yêu cầu.");
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu ID yêu cầu.");
        }
        int requestId = Integer.parseInt(idParam);

        PermissionRequest request = dao.findById(requestId);
        if (request == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy yêu cầu cấp quyền.");
            return;
        }

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            resp.sendRedirect(req.getContextPath()
                    + "/permission-request?error=processed");
            return;
        }

        if (approve) {
            Timestamp expiry = parseExpiry(req.getParameter("expiryDate"));

            // Chỉ duyệt khi tài liệu đang có license hợp lệ.
            if (!licenseDAO.hasValidLicense(request.getDocumentId())) {
                resp.sendRedirect(req.getContextPath()
                        + "/permission-request?error=no-license");
                return;
            }

            if (dao.approve(requestId, user.getUserId(), expiry)) {
                String expiryMsg = expiry == null
                        ? "không thời hạn"
                        : expiry.toLocalDateTime().toString();

                try {
                    notificationDAO.create(
                            request.getUserId(),
                            "Yêu cầu quyền được duyệt",
                            "Yêu cầu " + request.getPermissionType()
                                    + " cho tài liệu \"" + request.getDocumentTitle()
                                    + "\" đã được duyệt. Hạn quyền: " + expiryMsg + ".",
                            "PERMISSION"
                    );
                    auditLogDAO.insert(
                            user.getUserId(),
                            "APPROVE_PERMISSION",
                            "PERMISSION_REQUEST",
                            requestId,
                            "Duyệt yêu cầu " + request.getPermissionType()
                                    + " cho " + request.getDocumentTitle(),
                            req.getRemoteAddr()
                    );
                } catch (Exception ignored) {
                }
            }
        } else {
            if (dao.reject(requestId, user.getUserId())) {
                try {
                    notificationDAO.create(
                            request.getUserId(),
                            "Yêu cầu quyền bị từ chối",
                            "Yêu cầu " + request.getPermissionType()
                                    + " cho tài liệu \"" + request.getDocumentTitle()
                                    + "\" đã bị từ chối.",
                            "PERMISSION"
                    );
                    auditLogDAO.insert(
                            user.getUserId(),
                            "REJECT_PERMISSION",
                            "PERMISSION_REQUEST",
                            requestId,
                            "Từ chối yêu cầu quyền cho " + request.getDocumentTitle(),
                            req.getRemoteAddr()
                    );
                } catch (Exception ignored) {
                }
            }
        }

        resp.sendRedirect(req.getContextPath() + "/permission-request");
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
