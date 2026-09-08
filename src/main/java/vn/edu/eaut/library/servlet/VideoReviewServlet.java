package vn.edu.eaut.library.servlet;
import java.io.IOException;

 import jakarta.servlet.annotation.WebServlet;
 import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
 import jakarta.servlet.http.HttpServletResponse;
 import vn.edu.eaut.library.dao.VideoReviewDAO;
import vn.edu.eaut.library.model.User;
import vn.edu.eaut.library.model.VideoReview;
@WebServlet("/video-reviews/*") public class VideoReviewServlet extends HttpServlet{
 private final VideoReviewDAO dao=new VideoReviewDAO();
 protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  User u=(User)req.getSession().getAttribute("currentUser"); if(u==null){resp.sendError(403);return;}
  try{int vid=Integer.parseInt(req.getParameter("videoId"));int rating=Integer.parseInt(req.getParameter("rating"));
   if(rating<1||rating>5){resp.sendError(400,"Điểm đánh giá phải từ 1 đến 5.");return;}
   VideoReview r=new VideoReview();r.setUserId(u.getUserId());r.setVideoId(vid);r.setRating(rating);r.setComment(req.getParameter("comment"));
   dao.upsert(r);resp.sendRedirect(req.getContextPath()+"/videos/detail?id="+vid+"#reviews");
  }catch(NumberFormatException e){resp.sendError(400,"Dữ liệu không hợp lệ.");}
 }
}