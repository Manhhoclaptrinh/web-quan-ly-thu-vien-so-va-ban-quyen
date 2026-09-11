package vn.edu.eaut.library.servlet;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.eaut.library.dao.BookDAO;
import vn.edu.eaut.library.dao.BookPermissionDAO;
import vn.edu.eaut.library.dao.UserDAO;
import vn.edu.eaut.library.model.BookPermission;
import vn.edu.eaut.library.model.User;

@WebServlet("/book-permission/*")
public class BookPermissionServlet extends HttpServlet {

    private final vn.edu.eaut.library.dao.AuditLogDAO auditLogDAO = new vn.edu.eaut.library.dao.AuditLogDAO();
    private final BookPermissionDAO permissionDAO = new BookPermissionDAO();
    private final UserDAO userDAO = new UserDAO();
    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            List<BookPermission> permissions = permissionDAO.findAll();
            req.setAttribute("permissions", permissions);
            req.setAttribute("users", userDAO.findAll());
            req.setAttribute("books", bookDAO.findAll());
            req.getRequestDispatcher("/views/book-permissions.jsp").forward(req, resp);
        } else if (pathInfo.equals("/delete")) {
            int id = Integer.parseInt(req.getParameter("id"));
            permissionDAO.delete(id);
            User current = (User) req.getSession().getAttribute("currentUser");
            auditLogDAO.insert(current.getUserId(), "REVOKE_BOOK_PERMISSION", "BOOK_PERMISSION", id,
                    "Thu hồi quyền sách #" + id, req.getRemoteAddr());
            resp.sendRedirect(req.getContextPath() + "/book-permission");
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        if ("/save".equals(pathInfo)) {
            savePermission(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void savePermission(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int userId = Integer.parseInt(req.getParameter("userId"));
        int bookId = Integer.parseInt(req.getParameter("bookId"));
        String permissionType = req.getParameter("permissionType");
        String expiryDateStr = req.getParameter("expiryDate"); // định dạng yyyy-MM-ddTHH:mm (datetime-local)

        User currentUser = (User) req.getSession().getAttribute("currentUser");

        BookPermission permission = new BookPermission();
        permission.setUserId(userId);
        permission.setBookId(bookId);
        permission.setPermissionType(permissionType);
        permission.setGrantedBy(currentUser.getUserId());

        if (expiryDateStr != null && !expiryDateStr.trim().isEmpty()) {
            permission.setExpiryDate(LocalDateTime.parse(expiryDateStr));
        }

        permissionDAO.insert(permission);
        auditLogDAO.insert(currentUser.getUserId(), "GRANT_BOOK_PERMISSION", "BOOK", bookId,
                "Cấp quyền " + permissionType + " sách cho user #" + userId, req.getRemoteAddr());
        resp.sendRedirect(req.getContextPath() + "/book-permission");
    }
}