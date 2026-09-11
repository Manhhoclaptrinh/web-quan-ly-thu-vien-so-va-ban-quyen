package vn.edu.eaut.library.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.eaut.library.dao.BookDAO;
import vn.edu.eaut.library.dao.BookLicenseDAO;
import vn.edu.eaut.library.model.BookLicense;
import vn.edu.eaut.library.model.User;

@WebServlet("/book-license/*")
public class BookLicenseServlet extends HttpServlet {

    private final BookLicenseDAO licenseDAO = new BookLicenseDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final vn.edu.eaut.library.dao.AuditLogDAO auditLogDAO = new vn.edu.eaut.library.dao.AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            if (!((User) req.getSession().getAttribute("currentUser")).isAuditor()) licenseDAO.markExpiredLicenses();
            List<BookLicense> licenses = licenseDAO.findAll();
            req.setAttribute("licenses", licenses);
            req.setAttribute("books", bookDAO.findAll());
            req.getRequestDispatcher("/views/book-license.jsp").forward(req, resp);
        } else if (pathInfo.equals("/edit")) {
            int id = Integer.parseInt(req.getParameter("id"));
            req.setAttribute("editLicense", licenseDAO.findById(id));
            req.setAttribute("licenses", licenseDAO.findAll());
            req.setAttribute("books", bookDAO.findAll());
            req.getRequestDispatcher("/views/book-license.jsp").forward(req, resp);
        } else if (pathInfo.equals("/delete")) {
            int id = Integer.parseInt(req.getParameter("id"));
            licenseDAO.delete(id);
            resp.sendRedirect(req.getContextPath() + "/book-license");
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        if ("/save".equals(pathInfo)) {
            saveLicense(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void saveLicense(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("licenseId");
        int bookId = Integer.parseInt(req.getParameter("bookId"));
        String licenseType = req.getParameter("licenseType");
        String licenseCode = req.getParameter("licenseCode");
        String issuedDateStr = req.getParameter("issuedDate");
        String expiryDateStr = req.getParameter("expiryDate");
        String terms = req.getParameter("terms");
        String status = req.getParameter("status");

        BookLicense license = new BookLicense();
        license.setBookId(bookId);
        license.setLicenseType(licenseType);
        license.setLicenseCode(licenseCode);
        license.setIssuedDate(parseDateOrNull(issuedDateStr));
        license.setExpiryDate(parseDateOrNull(expiryDateStr));
        license.setTerms(terms);
        license.setStatus(status != null ? status : "VALID");

        if (idParam == null || idParam.trim().isEmpty()) {
            licenseDAO.insert(license); User current=(User)req.getSession().getAttribute("currentUser"); auditLogDAO.insert(current.getUserId(),"CREATE_BOOK_LICENSE","BOOK",bookId,"Thêm bản quyền cho sách #"+bookId,req.getRemoteAddr());
        } else {
            license.setLicenseId(Integer.parseInt(idParam));
            licenseDAO.update(license); User current=(User)req.getSession().getAttribute("currentUser"); auditLogDAO.insert(current.getUserId(),"UPDATE_BOOK_LICENSE","BOOK_LICENSE",license.getLicenseId(),"Cập nhật bản quyền sách #"+license.getLicenseId(),req.getRemoteAddr());
        }

        resp.sendRedirect(req.getContextPath() + "/book-license");
    }

    private LocalDate parseDateOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(value.trim());
    }
}