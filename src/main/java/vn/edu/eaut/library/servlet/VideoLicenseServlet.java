package vn.edu.eaut.library.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.eaut.library.dao.VideoDAO;
import vn.edu.eaut.library.dao.VideoLicenseDAO;
import vn.edu.eaut.library.model.User;
import vn.edu.eaut.library.model.VideoLicense;

@WebServlet("/video-license/*")
public class VideoLicenseServlet extends HttpServlet {

    private final VideoLicenseDAO licenseDAO = new VideoLicenseDAO();
    private final VideoDAO videoDAO = new VideoDAO();
    private final vn.edu.eaut.library.dao.AuditLogDAO auditLogDAO = new vn.edu.eaut.library.dao.AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            licenseDAO.markExpiredLicenses();
            List<VideoLicense> licenses = licenseDAO.findAll();
            req.setAttribute("licenses", licenses);
            req.setAttribute("videos", videoDAO.findAll());
            req.getRequestDispatcher("/views/video-license.jsp").forward(req, resp);
        } else if (pathInfo.equals("/edit")) {
            int id = Integer.parseInt(req.getParameter("id"));
            req.setAttribute("editLicense", licenseDAO.findById(id));
            req.setAttribute("licenses", licenseDAO.findAll());
            req.setAttribute("videos", videoDAO.findAll());
            req.getRequestDispatcher("/views/video-license.jsp").forward(req, resp);
        } else if (pathInfo.equals("/delete")) {
            int id = Integer.parseInt(req.getParameter("id"));
            licenseDAO.delete(id);
            resp.sendRedirect(req.getContextPath() + "/video-license");
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
        int videoId = Integer.parseInt(req.getParameter("videoId"));
        String licenseType = req.getParameter("licenseType");
        String licenseCode = req.getParameter("licenseCode");
        String issuedDateStr = req.getParameter("issuedDate");
        String expiryDateStr = req.getParameter("expiryDate");
        String terms = req.getParameter("terms");
        String status = req.getParameter("status");

        VideoLicense license = new VideoLicense();
        license.setVideoId(videoId);
        license.setLicenseType(licenseType);
        license.setLicenseCode(licenseCode);
        license.setIssuedDate(parseDateOrNull(issuedDateStr));
        license.setExpiryDate(parseDateOrNull(expiryDateStr));
        license.setTerms(terms);
        license.setStatus(status != null ? status : "VALID");

        if (idParam == null || idParam.trim().isEmpty()) {
            licenseDAO.insert(license); User current=(User)req.getSession().getAttribute("currentUser"); auditLogDAO.insert(current.getUserId(),"CREATE_VIDEO_LICENSE","VIDEO",videoId,"Thêm bản quyền cho video #"+videoId,req.getRemoteAddr());
        } else {
            license.setLicenseId(Integer.parseInt(idParam));
            licenseDAO.update(license); User current=(User)req.getSession().getAttribute("currentUser"); auditLogDAO.insert(current.getUserId(),"UPDATE_VIDEO_LICENSE","VIDEO_LICENSE",license.getLicenseId(),"Cập nhật bản quyền video #"+license.getLicenseId(),req.getRemoteAddr());
        }

        resp.sendRedirect(req.getContextPath() + "/video-license");
    }

    private LocalDate parseDateOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(value.trim());
    }
}