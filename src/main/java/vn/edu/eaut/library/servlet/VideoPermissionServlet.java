package vn.edu.eaut.library.servlet;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.eaut.library.dao.UserDAO;
import vn.edu.eaut.library.dao.VideoDAO;
import vn.edu.eaut.library.dao.VideoPermissionDAO;
import vn.edu.eaut.library.model.User;
import vn.edu.eaut.library.model.VideoPermission;

@WebServlet("/video-permission/*")
public class VideoPermissionServlet extends HttpServlet {

    private final vn.edu.eaut.library.dao.AuditLogDAO auditLogDAO = new vn.edu.eaut.library.dao.AuditLogDAO();
    private final VideoPermissionDAO permissionDAO = new VideoPermissionDAO();
    private final UserDAO userDAO = new UserDAO();
    private final VideoDAO videoDAO = new VideoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            List<VideoPermission> permissions = permissionDAO.findAll();
            req.setAttribute("permissions", permissions);
            req.setAttribute("users", userDAO.findAll());
            req.setAttribute("videos", videoDAO.findAll());
            req.getRequestDispatcher("/views/video-permissions.jsp").forward(req, resp);
        } else if (pathInfo.equals("/delete")) {
            int id = Integer.parseInt(req.getParameter("id"));
            permissionDAO.delete(id);
            User current = (User) req.getSession().getAttribute("currentUser");
            auditLogDAO.insert(current.getUserId(), "REVOKE_VIDEO_PERMISSION", "VIDEO_PERMISSION", id,
                    "Thu hồi quyền video #" + id, req.getRemoteAddr());
            resp.sendRedirect(req.getContextPath() + "/video-permission");
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
        int videoId = Integer.parseInt(req.getParameter("videoId"));
        String permissionType = req.getParameter("permissionType");
        String expiryDateStr = req.getParameter("expiryDate"); // định dạng yyyy-MM-ddTHH:mm (datetime-local)

        User currentUser = (User) req.getSession().getAttribute("currentUser");

        VideoPermission permission = new VideoPermission();
        permission.setUserId(userId);
        permission.setVideoId(videoId);
        permission.setPermissionType(permissionType);
        permission.setGrantedBy(currentUser.getUserId());

        if (expiryDateStr != null && !expiryDateStr.trim().isEmpty()) {
            permission.setExpiryDate(LocalDateTime.parse(expiryDateStr));
        }

        permissionDAO.insert(permission);
        auditLogDAO.insert(currentUser.getUserId(), "GRANT_VIDEO_PERMISSION", "VIDEO", videoId,
                "Cấp quyền " + permissionType + " video cho user #" + userId, req.getRemoteAddr());
        resp.sendRedirect(req.getContextPath() + "/video-permission");
    }
}
