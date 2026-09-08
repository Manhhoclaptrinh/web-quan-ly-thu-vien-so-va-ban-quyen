package vn.edu.eaut.library.servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.eaut.library.dao.DashboardDAO;
import vn.edu.eaut.library.dao.DocumentDAO;
import vn.edu.eaut.library.dao.HistoryDAO;
import vn.edu.eaut.library.dao.LicenseDAO;
import vn.edu.eaut.library.dao.VideoDAO;
import vn.edu.eaut.library.dao.VideoLicenseDAO;
import vn.edu.eaut.library.model.AccessHistory;
import vn.edu.eaut.library.model.Document;
import vn.edu.eaut.library.model.Video;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private final DocumentDAO documentDAO = new DocumentDAO();
    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final HistoryDAO historyDAO = new HistoryDAO();
    private final LicenseDAO licenseDAO = new LicenseDAO();
    private final VideoDAO videoDAO = new VideoDAO();
    private final VideoLicenseDAO videoLicenseDAO = new VideoLicenseDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        licenseDAO.markExpiredLicenses();
        videoLicenseDAO.markExpiredLicenses();

        // KPI cards
        req.setAttribute("totalDocuments", dashboardDAO.countDocuments());
        req.setAttribute("availableDocuments", dashboardDAO.countAvailableDocuments());
        req.setAttribute("totalCategories", dashboardDAO.countCategories());
        req.setAttribute("totalUsers", dashboardDAO.countUsers());
        req.setAttribute("activeUsers", dashboardDAO.countActiveUsers());
        req.setAttribute("totalPermissions", dashboardDAO.countPermissions());
        req.setAttribute("validLicenses", dashboardDAO.countValidLicenses());
        req.setAttribute("totalDownloads", dashboardDAO.countDownloads());
        req.setAttribute("totalViews", dashboardDAO.countViews());
        req.setAttribute("totalVideos", dashboardDAO.countVideos());
        req.setAttribute("availableVideos", dashboardDAO.countAvailableVideos());
        req.setAttribute("validVideoLicenses", dashboardDAO.countValidVideoLicenses());
        vn.edu.eaut.library.model.User current = (vn.edu.eaut.library.model.User) req.getSession().getAttribute("currentUser");
        if (current != null) req.setAttribute("unreadNotifications", new vn.edu.eaut.library.dao.NotificationDAO().countUnread(current.getUserId()));
        req.setAttribute("documentsByCategory", dashboardDAO.documentsByCategory());
        req.setAttribute("activitySummary", dashboardDAO.activitySummary());
        req.setAttribute("topDocuments", dashboardDAO.topDocuments(5));

        List<Document> recentDocuments = documentDAO.findAll();
        if (recentDocuments.size() > 6) {
            recentDocuments = recentDocuments.subList(0, 6);
        }
        req.setAttribute("recentDocuments", recentDocuments);

        List<Video> recentVideos = videoDAO.findAll();
        if (recentVideos.size() > 6) {
            recentVideos = recentVideos.subList(0, 6);
        }
        req.setAttribute("recentVideos", recentVideos);

        List<AccessHistory> recentActivities;
        Object currentUser = req.getSession().getAttribute("currentUser");

        if (currentUser instanceof vn.edu.eaut.library.model.User) {
            vn.edu.eaut.library.model.User user =
                    (vn.edu.eaut.library.model.User) currentUser;

            if ("READER".equals(user.getRole())) {
                recentActivities = historyDAO.findByUserId(user.getUserId());
            } else {
                recentActivities = historyDAO.findAll();
            }
        } else {
            recentActivities = historyDAO.findAll();
        }
        
        if (recentActivities.size() > 8) {
            recentActivities = recentActivities.subList(0, 8);
        }
        req.setAttribute("recentActivities", recentActivities);

        req.getRequestDispatcher("/views/home.jsp").forward(req, resp);
    }
}
