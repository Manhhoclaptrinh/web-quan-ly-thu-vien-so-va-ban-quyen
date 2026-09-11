package vn.edu.eaut.library.servlet;

import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.eaut.library.dao.BookReviewDAO;
import vn.edu.eaut.library.model.BookReview;
import vn.edu.eaut.library.model.User;

@WebServlet("/book-reviews/*")
public class BookReviewServlet extends HttpServlet {
    private final BookReviewDAO dao = new BookReviewDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = (User) req.getSession().getAttribute("currentUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=session");
            return;
        }

        try {
            int bookId = Integer.parseInt(req.getParameter("bookId"));
            int rating = Integer.parseInt(req.getParameter("rating"));

            if (rating < 1 || rating > 5) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Điểm đánh giá phải từ 1 đến 5.");
                return;
            }

            BookReview review = new BookReview();
            review.setUserId(user.getUserId());
            review.setBookId(bookId);
            review.setRating(rating);
            review.setComment(req.getParameter("comment"));

            dao.upsert(review);
            resp.sendRedirect(req.getContextPath()
                    + "/books/detail?id=" + bookId + "#reviews");
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Dữ liệu không hợp lệ.");
        }
    }
}
