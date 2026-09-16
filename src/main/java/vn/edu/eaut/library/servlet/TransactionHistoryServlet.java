package vn.edu.eaut.library.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.eaut.library.dao.TransactionDAO;

import java.io.IOException;

/**
 * /transaction-history - Bên thủ thư: xem toàn bộ lịch sử giao dịch của mọi Reader
 * (nạp ví, đăng ký hội viên, phí xem PDF, phí tải xuống), tìm theo tên tài khoản.
 * Quyền truy cập do AuthFilter (STAFF_ONLY_PATHS) đảm nhiệm - chỉ ADMIN/LIBRARIAN vào được.
 */
@WebServlet("/transaction-history")
public class TransactionHistoryServlet extends HttpServlet {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        req.setAttribute("keyword", username);
        req.setAttribute("transactions", transactionDAO.search(username));
        req.getRequestDispatcher("/views/transaction-history.jsp").forward(req, resp);
    }
}
