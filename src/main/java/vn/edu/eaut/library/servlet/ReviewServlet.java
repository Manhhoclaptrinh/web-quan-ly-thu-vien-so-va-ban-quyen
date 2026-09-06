package vn.edu.eaut.library.servlet;
import jakarta.servlet.annotation.WebServlet; import jakarta.servlet.http.*; import java.io.IOException;
import vn.edu.eaut.library.dao.ReviewDAO; import vn.edu.eaut.library.model.Review; import vn.edu.eaut.library.model.User;
@WebServlet("/reviews/*") public class ReviewServlet extends HttpServlet{
 private final ReviewDAO dao=new ReviewDAO();
 protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  User u=(User)req.getSession().getAttribute("currentUser"); if(u==null){resp.sendError(403);return;}
  try{int doc=Integer.parseInt(req.getParameter("documentId"));int rating=Integer.parseInt(req.getParameter("rating"));
   if(rating<1||rating>5){resp.sendError(400,"Điểm đánh giá phải từ 1 đến 5.");return;}
   Review r=new Review();r.setUserId(u.getUserId());r.setDocumentId(doc);r.setRating(rating);r.setComment(req.getParameter("comment"));
   dao.upsert(r);resp.sendRedirect(req.getContextPath()+"/documents/detail?id="+doc+"#reviews");
  }catch(NumberFormatException e){resp.sendError(400,"Dữ liệu không hợp lệ.");}
 }
}
