package vn.edu.eaut.library.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.eaut.library.dao.DocumentDAO;
import vn.edu.eaut.library.dao.LicenseDAO;
import vn.edu.eaut.library.model.License;
import vn.edu.eaut.library.model.User;

@WebServlet("/license/*")
public class LicenseServlet extends HttpServlet {

    private final LicenseDAO licenseDAO = new LicenseDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final vn.edu.eaut.library.dao.AuditLogDAO auditLogDAO = new vn.edu.eaut.library.dao.AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            licenseDAO.markExpiredLicenses();
            List<License> licenses = licenseDAO.findAll();
            req.setAttribute("licenses", licenses);
            req.setAttribute("documents", documentDAO.findAll());
            req.getRequestDispatcher("/views/license.jsp").forward(req, resp);
        } else if (pathInfo.equals("/edit")) {
            int id = Integer.parseInt(req.getParameter("id"));
            req.setAttribute("editLicense", licenseDAO.findById(id));
            req.setAttribute("licenses", licenseDAO.findAll());
            req.setAttribute("documents", documentDAO.findAll());
            req.getRequestDispatcher("/views/license.jsp").forward(req, resp);
        } else if (pathInfo.equals("/delete")) {
            int id = Integer.parseInt(req.getParameter("id"));
            licenseDAO.delete(id);
            resp.sendRedirect(req.getContextPath() + "/license");
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
        int documentId = Integer.parseInt(req.getParameter("documentId"));
        String licenseType = req.getParameter("licenseType");
        String licenseCode = req.getParameter("licenseCode");
        String issuedDateStr = req.getParameter("issuedDate");
        String expiryDateStr = req.getParameter("expiryDate");
        String terms = req.getParameter("terms");
        String status = req.getParameter("status");

        License license = new License();
        license.setDocumentId(documentId);
        license.setLicenseType(licenseType);
        license.setLicenseCode(licenseCode);
        license.setIssuedDate(parseDateOrNull(issuedDateStr));
        license.setExpiryDate(parseDateOrNull(expiryDateStr));
        license.setTerms(terms);
        license.setStatus(status != null ? status : "VALID");

        if (idParam == null || idParam.trim().isEmpty()) {
            licenseDAO.insert(license); User current=(User)req.getSession().getAttribute("currentUser"); auditLogDAO.insert(current.getUserId(),"CREATE_LICENSE","DOCUMENT",documentId,"Thêm bản quyền cho tài liệu #"+documentId,req.getRemoteAddr());
        } else {
            license.setLicenseId(Integer.parseInt(idParam));
            licenseDAO.update(license); User current=(User)req.getSession().getAttribute("currentUser"); auditLogDAO.insert(current.getUserId(),"UPDATE_LICENSE","LICENSE",license.getLicenseId(),"Cập nhật bản quyền #"+license.getLicenseId(),req.getRemoteAddr());
        }

        resp.sendRedirect(req.getContextPath() + "/license");
    }

    private LocalDate parseDateOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(value.trim());
    }
}
