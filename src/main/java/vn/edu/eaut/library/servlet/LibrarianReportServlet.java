package vn.edu.eaut.library.servlet;

import java.io.IOException;
import java.time.LocalDate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.library.dao.LibrarianReportDAO;
import vn.edu.eaut.library.model.User;

/** Báo cáo thống kê nghiệp vụ của LIBRARIAN. */
@WebServlet("/librarian/reports")
public class LibrarianReportServlet extends HttpServlet {

    private final LibrarianReportDAO reportDAO = new LibrarianReportDAO();

    private User requireLibrarian(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("currentUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        if (!user.isLibrarian()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Báo cáo thống kê này chỉ dành cho LIBRARIAN.");
            return null;
        }
        return user;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (requireLibrarian(req, resp) == null) return;

        try {
            LocalDate today = LocalDate.now();
            String range = req.getParameter("range");
            if (range == null || range.isBlank()) range = "30days";

            LocalDate startDate;
            LocalDate endDate = today;

            switch (range) {

                case "today":
                    startDate = today;
                    break;

                case "7days":
                    startDate = today.minusDays(6);
                    break;

                case "30days":
                    startDate = today.minusDays(29);
                    break;

                case "year":
                    startDate = LocalDate.of(
                            today.getYear(),
                            1,
                            1
                    );
                    break;

                case "5years":
                    startDate = LocalDate.of(
                            today.getYear() - 4,
                            1,
                            1
                    );
                    break;

                case "custom":
                    try {
                        String startParam = req.getParameter("start");
                        String endParam = req.getParameter("end");

                        if (startParam == null || endParam == null
                                || startParam.isBlank()
                                || endParam.isBlank()) {

                            throw new IllegalArgumentException(
                                    "Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc."
                            );
                        }

                        startDate = LocalDate.parse(startParam);
                        endDate = LocalDate.parse(endParam);

                        if (endDate.isBefore(startDate)) {
                            throw new IllegalArgumentException(
                                    "Khoảng ngày không hợp lệ."
                            );
                        }

                    } catch (Exception e) {

                        range = "30days";
                        startDate = today.minusDays(29);
                        endDate = today;
                    }

                    break;

                default:
                    range = "30days";
                    startDate = today.minusDays(29);
                    endDate = today;
                    break;
            }

            req.setAttribute("selectedRange", range);
            req.setAttribute("startDate", startDate);
            req.setAttribute("endDate", endDate);
            req.setAttribute("reportYear", today.getYear());

            req.setAttribute("overview", reportDAO.getOverview());
            req.setAttribute("activityReport", reportDAO.accessByDate(startDate, endDate));
            req.setAttribute("yearlyReport", reportDAO.yearlyReport(endDate));
            req.setAttribute("monthlyReport", reportDAO.monthlyReport(today.getYear()));
            req.setAttribute("contentByType", reportDAO.contentByType());
            req.setAttribute("topContent", reportDAO.topContent(10, startDate, endDate));
            req.setAttribute("categoryStats", reportDAO.categoryStats());
            req.setAttribute("requestStats", reportDAO.requestStats());
            req.setAttribute("reviewStats", reportDAO.reviewStats());
            req.setAttribute("transactionStats", reportDAO.transactionStats(startDate, endDate));
            req.setAttribute("licenseStats", reportDAO.licenseStats());

            req.getRequestDispatcher("/WEB-INF/views/librarian-reports.jsp").forward(req, resp);
        } catch (RuntimeException e) {
            throw new ServletException("Không thể tải báo cáo thống kê thủ thư.", e);
        }
    }
}
