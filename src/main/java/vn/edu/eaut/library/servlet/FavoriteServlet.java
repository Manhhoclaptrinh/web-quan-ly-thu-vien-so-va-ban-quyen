package vn.edu.eaut.library.servlet;
import java.io.IOException;

 import jakarta.servlet.annotation.WebServlet;
 import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
 import jakarta.servlet.http.HttpServletResponse;
import vn.edu.eaut.library.dao.FavoriteDAO;
import vn.edu.eaut.library.model.User;
@WebServlet("/favorites/*") public class FavoriteServlet extends HttpServlet{
 private final FavoriteDAO dao=new FavoriteDAO();
 
 protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException, jakarta.servlet.ServletException{
  User u=(User)req.getSession().getAttribute("currentUser"); if(u==null){resp.sendError(403);return;}
  if("/".equals(req.getPathInfo())||req.getPathInfo()==null){req.setAttribute("favorites",dao.findByUserId(u.getUserId()));req.getRequestDispatcher("/views/favorites.jsp").forward(req,resp);return;}
  resp.sendError(404);
 }

 protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  User u=(User)req.getSession().getAttribute("currentUser"); if(u==null){resp.sendError(403);return;}
  String itemType=req.getParameter("itemType");
  boolean isVideo = "VIDEO".equalsIgnoreCase(itemType);
  boolean isBook = "BOOK".equalsIgnoreCase(itemType);
  try{
   String action=req.getParameter("action");
   if(isVideo){
    int id=Integer.parseInt(req.getParameter("videoId"));
    if("add".equals(action))dao.addVideo(u.getUserId(),id);else if("remove".equals(action))dao.removeVideo(u.getUserId(),id);else{resp.sendError(400);return;}
    resp.sendRedirect(req.getContextPath()+"/videos/detail?id="+id);
   }else if(isBook){
    int id=Integer.parseInt(req.getParameter("bookId"));
    if("add".equals(action))dao.addBook(u.getUserId(),id);else if("remove".equals(action))dao.removeBook(u.getUserId(),id);else{resp.sendError(400);return;}
    resp.sendRedirect(req.getContextPath()+"/books/detail?id="+id);
   }else{
    int id=Integer.parseInt(req.getParameter("documentId"));
    if("add".equals(action))dao.add(u.getUserId(),id);else if("remove".equals(action))dao.remove(u.getUserId(),id);else{resp.sendError(400);return;}
    resp.sendRedirect(req.getContextPath()+"/documents/detail?id="+id);
   }
  }catch(NumberFormatException e){resp.sendError(400,"ID không hợp lệ.");}
 }
}