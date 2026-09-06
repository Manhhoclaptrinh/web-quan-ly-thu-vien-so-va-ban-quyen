package vn.edu.eaut.library.servlet;
import jakarta.servlet.annotation.WebServlet; import jakarta.servlet.http.*; import java.io.IOException;
import vn.edu.eaut.library.dao.FavoriteDAO; import vn.edu.eaut.library.model.User;
@WebServlet("/favorites/*") public class FavoriteServlet extends HttpServlet{
 private final FavoriteDAO dao=new FavoriteDAO();
 protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException, jakarta.servlet.ServletException{
  User u=(User)req.getSession().getAttribute("currentUser"); if(u==null){resp.sendError(403);return;}
  if("/".equals(req.getPathInfo())||req.getPathInfo()==null){req.setAttribute("favorites",dao.findByUserId(u.getUserId()));req.getRequestDispatcher("/views/favorites.jsp").forward(req,resp);return;}
  resp.sendError(404);
 }
 protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  User u=(User)req.getSession().getAttribute("currentUser"); if(u==null){resp.sendError(403);return;}
  try{int id=Integer.parseInt(req.getParameter("documentId"));String action=req.getParameter("action");
   if("add".equals(action))dao.add(u.getUserId(),id);else if("remove".equals(action))dao.remove(u.getUserId(),id);else{resp.sendError(400);return;}
   resp.sendRedirect(req.getContextPath()+"/documents/detail?id="+id);
  }catch(NumberFormatException e){resp.sendError(400,"ID không hợp lệ.");}
 }
}
