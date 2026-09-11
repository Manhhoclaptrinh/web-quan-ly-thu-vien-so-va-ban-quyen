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
import vn.edu.eaut.library.dao.BookDAO;
import vn.edu.eaut.library.dao.BookLicenseDAO;
import vn.edu.eaut.library.dao.BookPermissionRequestDAO;
import vn.edu.eaut.library.dao.NotificationDAO;
import vn.edu.eaut.library.model.Book;
import vn.edu.eaut.library.model.BookPermissionRequest;
import vn.edu.eaut.library.model.User;

@WebServlet("/book-permission-request/*")
public class BookPermissionRequestServlet extends HttpServlet {
    private final BookPermissionRequestDAO dao = new BookPermissionRequestDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final BookLicenseDAO licenseDAO = new BookLicenseDAO();
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
                req.setAttribute("requests", dao.findAll());
            } else {
                req.setAttribute("requests", dao.findByUserId(user.getUserId()));
                req.setAttribute("books", bookDAO.findAll());
            }
            req.getRequestDispatcher("/views/book-permission-requests.jsp").forward(req, resp);
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

        String bookParam = req.getParameter("bookId");
        String permissionType = req.getParameter("permissionType");

        if (bookParam == null || bookParam.trim().isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn sách.");
        }

        int bookId = Integer.parseInt(bookParam);
        Book book = bookDAO.findById(bookId);

        if (book == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy sách.");
            return;
        }

        if (!"AVAILABLE".equalsIgnoreCase(book.getStatus())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Sách đang bị vô hiệu hóa.");
            return;
        }

        if (!"VIEW".equalsIgnoreCase(permissionType) && !"DOWNLOAD".equalsIgnoreCase(permissionType)) {
            throw new IllegalArgumentException("Loại quyền không hợp lệ.");
        }

        permissionType = permissionType.toUpperCase();

        vn.edu.eaut.library.dao.BookPermissionDAO permissionDAO =
                new vn.edu.eaut.library.dao.BookPermissionDAO();
        if (permissionDAO.hasPermission(user.getUserId(), bookId, permissionType)) {
            resp.sendRedirect(req.getContextPath() + "/book-permission-request?error=already-granted");
            return;
        }

        if (dao.hasPending(user.getUserId(), bookId, permissionType)) {
            resp.sendRedirect(req.getContextPath() + "/book-permission-request?error=pending");
            return;
        }

        BookPermissionRequest request = new BookPermissionRequest();
        request.setUserId(user.getUserId());
        request.setBookId(bookId);
        request.setPermissionType(permissionType);
        request.setReason(req.getParameter("reason"));

        if (!dao.insert(request)) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không thể tạo yêu cầu cấp quyền.");
            return;
        }

        try {
            auditLogDAO.insert(user.getUserId(), "REQUEST_BOOK_PERMISSION", "BOOK", bookId,
                    "Gửi yêu cầu quyền " + permissionType + " cho sách", req.getRemoteAddr());
        } catch (Exception ignored) {
        }

        resp.sendRedirect(req.getContextPath() + "/book-permission-request?success=created");
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

        BookPermissionRequest request = dao.findById(requestId);
        if (request == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy yêu cầu cấp quyền.");
            return;
        }

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            resp.sendRedirect(req.getContextPath() + "/book-permission-request?error=processed");
            return;
        }

        if (approve) {
            Timestamp expiry = parseExpiry(req.getParameter("expiryDate"));

            if (!licenseDAO.hasValidLicense(request.getBookId())) {
                resp.sendRedirect(req.getContextPath() + "/book-permission-request?error=no-license");
                return;
            }

            if (dao.approve(requestId, user.getUserId(), expiry)) {
                String expiryMsg = expiry == null ? "không thời hạn" : expiry.toLocalDateTime().toString();
                try {
                    notificationDAO.create(
                            request.getUserId(),
                            "Yêu cầu quyền sách được duyệt",
                            "Yêu cầu " + request.getPermissionType() + " cho sách \""
                                    + request.getBookTitle() + "\" đã được duyệt. Hạn quyền: " + expiryMsg + ".",
                            "PERMISSION"
                    );
                    auditLogDAO.insert(user.getUserId(), "APPROVE_BOOK_PERMISSION", "BOOK_PERMISSION_REQUEST",
                            requestId, "Duyệt yêu cầu " + request.getPermissionType() + " cho " + request.getBookTitle(),
                            req.getRemoteAddr());
                } catch (Exception ignored) {
                }
            }
        } else {
            if (dao.reject(requestId, user.getUserId())) {
                try {
                    notificationDAO.create(
                            request.getUserId(),
                            "Yêu cầu quyền sách bị từ chối",
                            "Yêu cầu " + request.getPermissionType() + " cho sách \""
                                    + request.getBookTitle() + "\" đã bị từ chối.",
                            "PERMISSION"
                    );
                    auditLogDAO.insert(user.getUserId(), "REJECT_BOOK_PERMISSION", "BOOK_PERMISSION_REQUEST",
                            requestId, "Từ chối yêu cầu quyền cho " + request.getBookTitle(),
                            req.getRemoteAddr());
                } catch (Exception ignored) {
                }
            }
        }

        resp.sendRedirect(req.getContextPath() + "/book-permission-request");
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