package vn.edu.eaut.library.servlet;
import jakarta.servlet.annotation.WebServlet; import jakarta.servlet.http.*; import java.io.IOException;
import vn.edu.eaut.library.dao.NotificationDAO; import vn.edu.eaut.library.model.User;
@WebServlet("/notifications/*") public class NotificationServlet extends HttpServlet{
 private final NotificationDAO dao=new NotificationDAO();
 protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException, jakarta.servlet.ServletException{
  User u=(User)req.getSession().getAttribute("currentUser");if(u==null){resp.sendError(403);return;}
  String p=req.getPathInfo(); if(p==null||"/".equals(p)){req.setAttribute("notifications",dao.findByUserId(u.getUserId()));req.getRequestDispatcher("/views/notifications.jsp").forward(req,resp);return;}
  if("/read".equals(p)){try{dao.markRead(Integer.parseInt(req.getParameter("id")),u.getUserId());}catch(Exception ignored){}resp.sendRedirect(req.getContextPath()+"/notifications");return;}
  if("/read-all".equals(p)){dao.markAllRead(u.getUserId());resp.sendRedirect(req.getContextPath()+"/notifications");return;}resp.sendError(404);
 }
}
