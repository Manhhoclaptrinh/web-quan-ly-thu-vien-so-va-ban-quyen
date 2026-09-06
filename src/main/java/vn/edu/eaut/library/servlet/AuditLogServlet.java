package vn.edu.eaut.library.servlet;
import jakarta.servlet.annotation.WebServlet; import jakarta.servlet.http.*; import java.io.IOException;
import vn.edu.eaut.library.dao.AuditLogDAO; import vn.edu.eaut.library.model.User;
@WebServlet("/audit-logs") public class AuditLogServlet extends HttpServlet{
 private final AuditLogDAO dao=new AuditLogDAO();
 protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException, jakarta.servlet.ServletException{
  User u=(User)req.getSession().getAttribute("currentUser");if(u==null||!u.isAdmin()){resp.sendError(403,"Chỉ ADMIN được xem audit log.");return;}
  req.setAttribute("auditLogs",dao.findAll());req.getRequestDispatcher("/views/audit-logs.jsp").forward(req,resp);
 }
}
