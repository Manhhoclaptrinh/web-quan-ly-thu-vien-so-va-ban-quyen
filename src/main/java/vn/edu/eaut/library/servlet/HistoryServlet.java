package vn.edu.eaut.library.servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.eaut.library.dao.HistoryDAO;
import vn.edu.eaut.library.model.AccessHistory;
import vn.edu.eaut.library.model.User;

@WebServlet("/history")
public class HistoryServlet extends HttpServlet {

    private final HistoryDAO historyDAO = new HistoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User currentUser = (User) req.getSession().getAttribute("currentUser");

        Integer userId = null, documentId = null;
        String action = req.getParameter("actionType");
        String fromDate = req.getParameter("fromDate"), toDate = req.getParameter("toDate");
        if (currentUser.isAdmin() || currentUser.isLibrarian()) {
            try { if(req.getParameter("userId")!=null&&!req.getParameter("userId").isEmpty()) userId=Integer.parseInt(req.getParameter("userId")); } catch(Exception ignored){}
        } else userId=currentUser.getUserId();
        try { if(req.getParameter("documentId")!=null&&!req.getParameter("documentId").isEmpty()) documentId=Integer.parseInt(req.getParameter("documentId")); } catch(Exception ignored){}
        List<AccessHistory> historyList=historyDAO.search(userId,documentId,action,fromDate,toDate);
        req.setAttribute("historyList", historyList); req.setAttribute("selectedAction",action); req.setAttribute("fromDate",fromDate); req.setAttribute("toDate",toDate); req.setAttribute("selectedUserId",userId); req.setAttribute("selectedDocumentId",documentId);
        req.getRequestDispatcher("/views/history.jsp").forward(req, resp);
    }
}
