package vn.edu.eaut.library.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.eaut.library.dao.HistoryDAO;
import vn.edu.eaut.library.model.AccessHistory;
import vn.edu.eaut.library.model.User;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private final HistoryDAO historyDAO = new HistoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session != null) {
            User currentUser = (User) session.getAttribute("currentUser");
            if (currentUser != null) {
                AccessHistory history = new AccessHistory();
                history.setUserId(currentUser.getUserId());
                history.setDocumentId(null);
                history.setActionType("LOGOUT");
                history.setIpAddress(req.getRemoteAddr());
                safeLog(history);
            }
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    private void safeLog(AccessHistory history) {
        try {
            historyDAO.insert(history);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
