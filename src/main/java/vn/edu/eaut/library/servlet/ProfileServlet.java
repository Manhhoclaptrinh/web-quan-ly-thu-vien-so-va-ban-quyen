package vn.edu.eaut.library.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.eaut.library.dao.UserDAO;
import vn.edu.eaut.library.model.User;
import vn.edu.eaut.library.utils.PasswordUtil;

import java.io.IOException;

@WebServlet("/profile/*")
public class ProfileServlet extends HttpServlet {
    private final UserDAO userDAO=new UserDAO();
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User user=(User)req.getSession().getAttribute("currentUser");
        String path=req.getPathInfo();
        if(path==null||"/".equals(path)){req.setAttribute("profile",userDAO.findById(user.getUserId()));req.getRequestDispatcher("/views/profile.jsp").forward(req,resp);return;}
        if("/edit".equals(path)){req.setAttribute("profile",userDAO.findById(user.getUserId()));req.getRequestDispatcher("/views/profile-edit.jsp").forward(req,resp);return;}
        if("/change-password".equals(path)){req.getRequestDispatcher("/views/change-password.jsp").forward(req,resp);return;}
        resp.sendError(404);
    }
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.setCharacterEncoding("UTF-8"); User sessionUser=(User)req.getSession().getAttribute("currentUser"); String path=req.getPathInfo();
        if("/save".equals(path)){
            String full=trim(req.getParameter("fullName")),email=trim(req.getParameter("email"));
            if(full==null||full.isEmpty()){req.setAttribute("error","Họ tên không được để trống.");req.setAttribute("profile",sessionUser);req.getRequestDispatcher("/views/profile-edit.jsp").forward(req,resp);return;}
            try{User u=userDAO.findById(sessionUser.getUserId());u.setFullName(full);u.setEmail(email==null||email.isEmpty()?null:email);userDAO.updateProfile(u);
                sessionUser.setFullName(u.getFullName());sessionUser.setEmail(u.getEmail());req.getSession().setAttribute("currentUser",sessionUser);
                resp.sendRedirect(req.getContextPath()+"/profile?success=updated");
            }catch(RuntimeException e){req.setAttribute("error","Không thể cập nhật thông tin. Email có thể đã tồn tại.");req.setAttribute("profile",sessionUser);req.getRequestDispatcher("/views/profile-edit.jsp").forward(req,resp);}return;
        }
        if("/change-password".equals(path)){
            String current=req.getParameter("currentPassword"),next=req.getParameter("newPassword"),confirm=req.getParameter("confirmPassword");
            User u=userDAO.findById(sessionUser.getUserId());
            if(current==null||!PasswordUtil.matches(current,u.getPassword())){req.setAttribute("error","Mật khẩu hiện tại không đúng.");req.getRequestDispatcher("/views/change-password.jsp").forward(req,resp);return;}
            if(next==null||next.length()<4){req.setAttribute("error","Mật khẩu mới phải có ít nhất 4 ký tự.");req.getRequestDispatcher("/views/change-password.jsp").forward(req,resp);return;}
            if(!next.equals(confirm)){req.setAttribute("error","Xác nhận mật khẩu mới không khớp.");req.getRequestDispatcher("/views/change-password.jsp").forward(req,resp);return;}
            userDAO.updatePassword(u.getUserId(),PasswordUtil.hash(next));resp.sendRedirect(req.getContextPath()+"/profile?success=password");return;
        }
        resp.sendError(404);
    }
    private String trim(String s){return s==null?null:s.trim();}
}
