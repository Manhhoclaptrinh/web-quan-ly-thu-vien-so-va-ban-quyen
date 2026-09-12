package vn.edu.eaut.library.servlet;

import java.io.*;
import java.nio.file.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.eaut.library.dao.*;
import vn.edu.eaut.library.model.*;

@WebServlet("/admin/*")
@MultipartConfig(maxFileSize=50*1024*1024, maxRequestSize=60*1024*1024)
public class AdminServlet extends HttpServlet {
    private final AdminDashboardDAO dashboardDAO=new AdminDashboardDAO();
    private final AdminSettingsDAO settingsDAO=new AdminSettingsDAO();
    private final UserDAO userDAO=new UserDAO();
    private final CategoryDAO categoryDAO=new CategoryDAO();
    private final DocumentDAO documentDAO=new DocumentDAO();
    private final VideoDAO videoDAO=new VideoDAO();
    private final BookDAO bookDAO=new BookDAO();
    private final PermissionDAO permissionDAO=new PermissionDAO();
    private final PermissionRequestDAO requestDAO=new PermissionRequestDAO();
    private final LicenseDAO licenseDAO=new LicenseDAO();
    private final VideoPermissionDAO videoPermissionDAO=new VideoPermissionDAO();
    private final VideoPermissionRequestDAO videoRequestDAO=new VideoPermissionRequestDAO();
    private final VideoLicenseDAO videoLicenseDAO=new VideoLicenseDAO();
    private final BookPermissionDAO bookPermissionDAO=new BookPermissionDAO();
    private final BookPermissionRequestDAO bookRequestDAO=new BookPermissionRequestDAO();
    private final BookLicenseDAO bookLicenseDAO=new BookLicenseDAO();
    private final AuditLogDAO auditDAO=new AuditLogDAO();

    private User admin(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        HttpSession s=req.getSession(false); User u=s==null?null:(User)s.getAttribute("currentUser");
        if(u==null){resp.sendRedirect(req.getContextPath()+"/login");return null;}
        if(!u.isAdmin()){resp.sendError(403,"Admin Panel chỉ dành cho ADMIN.");return null;}
        return u;
    }
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User u=admin(req,resp);if(u==null)return;
        String p=req.getPathInfo(); if(p==null||"/".equals(p)){dashboard(req,resp);return;}
        switch(p){
            case "/users": users(req,resp);break;
            case "/content": content(req,resp);break;
            case "/content/form": contentForm(req,resp);break;
            case "/permissions": permissions(req,resp);break;
            case "/reports": reports(req,resp);break;
            case "/audit-logs": auditLogs(req,resp);break;
            case "/security": security(req,resp);break;
            case "/settings": settings(req,resp);break;
            case "/notifications": notifications(req,resp);break;
            case "/search": search(req,resp);break;
            default:resp.sendError(404);
        }
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User u=admin(req,resp);if(u==null)return;
        req.setCharacterEncoding("UTF-8");
        String action=req.getParameter("action");
        try{
            if("user-save".equals(action)) saveUser(req,resp,u);
            else if("user-toggle".equals(action)) toggleUser(req,resp,u);
            else if("user-delete".equals(action)) deleteUser(req,resp,u);
            else if("user-reset-password".equals(action)) resetPassword(req,resp,u);
            else if("content-save".equals(action)) saveContent(req,resp,u);
            else if("content-delete".equals(action)) deleteContent(req,resp,u);
            else if("content-toggle".equals(action)) toggleContent(req,resp,u);
            else if("permission-grant".equals(action)) grantPermission(req,resp,u);
            else if("permission-revoke".equals(action)) revokePermission(req,resp,u);
            else if("request-approve".equals(action)) processRequest(req,resp,u,true);
            else if("request-reject".equals(action)) processRequest(req,resp,u,false);
            else if("license-save".equals(action)) saveLicense(req,resp,u);
            else if("license-delete".equals(action)) deleteLicense(req,resp,u);
            else if("license-revoke".equals(action)) revokeLicense(req,resp,u);
            else if("settings-save".equals(action)) saveSettings(req,resp,u);
            else resp.sendError(400,"Thao tác quản trị không hợp lệ.");
        }catch(RuntimeException e){resp.sendError(500,e.getMessage());}
    }
    private void dashboard(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{req.setAttribute("overview",dashboardDAO.getOverview());req.setAttribute("accessChart",dashboardDAO.accessLast7Days());req.setAttribute("securityEvents",dashboardDAO.recentSecurityEvents());req.setAttribute("settings",settingsDAO.findAll());forward(req,resp,"admin-dashboard.jsp");}catch(Exception e){throw new ServletException(e);}
    }
    private void users(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.setAttribute("users",userDAO.findAll());
        String edit=req.getParameter("edit"); if(edit!=null&&!edit.isEmpty()) req.setAttribute("editUser",userDAO.findById(parse(edit)));
        forward(req,resp,"admin-users.jsp");
    }
    private void content(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.setAttribute("documents",documentDAO.findAll());req.setAttribute("videos",videoDAO.findAll());req.setAttribute("books",bookDAO.findAll());forward(req,resp,"admin-content.jsp");
    }
    private void contentForm(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        String type=req.getParameter("type");if(type==null)type="DOCUMENT";req.setAttribute("type",type.toUpperCase());req.setAttribute("categories",categoryDAO.findAll());
        String id=req.getParameter("id");
        if(id!=null&&!id.isEmpty()){int n=parse(id);if("DOCUMENT".equalsIgnoreCase(type))req.setAttribute("item",documentDAO.findById(n));else if("VIDEO".equalsIgnoreCase(type))req.setAttribute("item",videoDAO.findById(n));else req.setAttribute("item",bookDAO.findById(n));}
        forward(req,resp,"admin-content-form.jsp");
    }
    private void permissions(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.setAttribute("permissions",permissionDAO.findAll());req.setAttribute("permissionRequests",requestDAO.findAll());
        req.setAttribute("videoPermissions",videoPermissionDAO.findAll());req.setAttribute("videoPermissionRequests",videoRequestDAO.findAll());
        req.setAttribute("bookPermissions",bookPermissionDAO.findAll());req.setAttribute("bookPermissionRequests",bookRequestDAO.findAll());
        req.setAttribute("licenses",licenseDAO.findAll());req.setAttribute("videoLicenses",videoLicenseDAO.findAll());req.setAttribute("bookLicenses",bookLicenseDAO.findAll());
        req.setAttribute("users",userDAO.findAll());req.setAttribute("documents",documentDAO.findAll());req.setAttribute("videos",videoDAO.findAll());req.setAttribute("books",bookDAO.findAll());
        forward(req,resp,"admin-permissions.jsp");
    }
    private void reports(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{req.setAttribute("overview",dashboardDAO.getOverview());req.setAttribute("accessChart",dashboardDAO.accessLast7Days());forward(req,resp,"admin-reports.jsp");}catch(Exception e){throw new ServletException(e);}
    }
    private void auditLogs(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{req.setAttribute("auditLogs",auditDAO.findAll());forward(req,resp,"admin-audit-logs.jsp");}
    private void security(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{req.setAttribute("overview",dashboardDAO.getOverview());req.setAttribute("events",dashboardDAO.recentSecurityEvents());forward(req,resp,"admin-security.jsp");}catch(Exception e){throw new ServletException(e);}
    }
    private void settings(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{req.setAttribute("settings",settingsDAO.findAll());forward(req,resp,"admin-settings.jsp");}
    private void notifications(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{req.setAttribute("overview",dashboardDAO.getOverview());req.setAttribute("events",dashboardDAO.recentSecurityEvents());forward(req,resp,"admin-notifications.jsp");}catch(Exception e){throw new ServletException(e);}
    }
    private void search(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        resp.setContentType("application/json;charset=UTF-8");String q=req.getParameter("q");StringBuilder b=new StringBuilder("[");
        for(Map<String,Object> m:dashboardDAO.globalSearch(q)){if(b.length()>1)b.append(',');b.append("{\"id\":").append(m.get("id")).append(",\"name\":\"").append(json(m.get("name"))).append("\",\"type\":\"").append(json(m.get("type"))).append("\"}");}
        b.append(']');resp.getWriter().print(b);
    }
    private void saveUser(HttpServletRequest req,HttpServletResponse resp,User admin)throws IOException{
        String id=req.getParameter("userId"),username=req.getParameter("username"),name=req.getParameter("fullName"),email=req.getParameter("email"),role=req.getParameter("role"),status=req.getParameter("status");
        if(name==null||name.trim().isEmpty()||role==null){resp.sendError(400,"Thiếu thông tin người dùng.");return;}
        User x=new User();x.setFullName(name.trim());x.setEmail(email);x.setRole(role);x.setStatus(status==null?"ACTIVE":status);
        if(id==null||id.isEmpty()){
            if(username==null||username.trim().isEmpty()){resp.sendError(400,"Username bắt buộc.");return;}
            x.setUsername(username.trim());String pw=req.getParameter("password");if(pw==null||pw.isEmpty()){resp.sendError(400,"Mật khẩu bắt buộc khi tạo tài khoản.");return;}x.setPassword(vn.edu.eaut.library.utils.PasswordUtil.hash(pw));userDAO.insert(x);
            log(admin,"CREATE_USER","USER",null,"Tạo tài khoản "+x.getUsername(),req);
        }else{x.setUserId(parse(id));userDAO.update(x);log(admin,"UPDATE_USER","USER",x.getUserId(),"Cập nhật "+x.getFullName(),req);}
        resp.sendRedirect(req.getContextPath()+"/admin/users");
    }
    private void toggleUser(HttpServletRequest req,HttpServletResponse resp,User admin)throws IOException{
        int id=parse(req.getParameter("userId"));if(id==admin.getUserId()){resp.sendError(400,"Không thể tự khóa tài khoản ADMIN đang đăng nhập.");return;}userDAO.toggleStatus(id);log(admin,"TOGGLE_USER_STATUS","USER",id,"Thay đổi trạng thái tài khoản",req);resp.sendRedirect(req.getContextPath()+"/admin/users");
    }
    private void deleteUser(HttpServletRequest req,HttpServletResponse resp,User admin)throws IOException{
        int id=parse(req.getParameter("userId"));if(id==admin.getUserId()){resp.sendError(400,"Không thể xóa tài khoản ADMIN đang đăng nhập.");return;}userDAO.delete(id);log(admin,"DELETE_USER","USER",id,"Xóa tài khoản",req);resp.sendRedirect(req.getContextPath()+"/admin/users");
    }
    private void resetPassword(HttpServletRequest req,HttpServletResponse resp,User admin)throws IOException{
        int id=parse(req.getParameter("userId"));String pw=req.getParameter("newPassword");
        if(pw==null||pw.length()<6){resp.sendError(400,"Mật khẩu mới phải có ít nhất 6 ký tự.");return;}
        userDAO.updatePassword(id,vn.edu.eaut.library.utils.PasswordUtil.hash(pw));
        log(admin,"RESET_PASSWORD","USER",id,"Admin đặt lại mật khẩu tài khoản",req);
        resp.sendRedirect(req.getContextPath()+"/admin/users");
    }
    private void saveContent(HttpServletRequest req,HttpServletResponse resp,User admin)throws IOException,ServletException{
        String type=req.getParameter("type");String id=req.getParameter("id");String title=req.getParameter("title"),author=req.getParameter("author"),desc=req.getParameter("description"),access=req.getParameter("accessLevel");
        int cat=parse(req.getParameter("categoryId"));Part file=req.getPart("contentFile");String stored=null;
        if(file!=null&&file.getSize()>0)stored=saveUpload(req,file,type);
        if("DOCUMENT".equalsIgnoreCase(type)){
            Document d=new Document();d.setTitle(title);d.setAuthor(author);d.setCategoryId(cat);d.setDescription(desc);d.setAccessLevel(access);d.setStatus(req.getParameter("status")==null?"AVAILABLE":req.getParameter("status"));d.setUploadedBy(admin.getUserId());
            if(id==null||id.isEmpty()){if(stored==null){resp.sendError(400,"Tài liệu mới phải có file.");return;}d.setFilePath(stored);documentDAO.insert(d);log(admin,"CREATE_DOCUMENT","DOCUMENT",null,"Tạo tài liệu "+title,req);}
            else{int n=parse(id);Document old=documentDAO.findById(n);d.setDocumentId(n);d.setFilePath(stored==null?old.getFilePath():stored);documentDAO.updateWithFile(d);log(admin,"UPDATE_DOCUMENT","DOCUMENT",n,"Cập nhật tài liệu "+title,req);}
        }else if("VIDEO".equalsIgnoreCase(type)){
            Video v=new Video();v.setTitle(title);v.setAuthor(author);v.setCategoryId(cat);v.setDescription(desc);v.setAccessLevel(access);v.setStatus(req.getParameter("status")==null?"AVAILABLE":req.getParameter("status"));v.setUploadedBy(admin.getUserId());
            if(id==null||id.isEmpty()){if(stored==null){resp.sendError(400,"Video mới phải có file.");return;}v.setFilePath(stored);videoDAO.insert(v);log(admin,"CREATE_VIDEO","VIDEO",null,"Tạo video "+title,req);}
            else{int n=parse(id);Video old=videoDAO.findById(n);v.setVideoId(n);v.setFilePath(stored==null?old.getFilePath():stored);videoDAO.updateWithFile(v);log(admin,"UPDATE_VIDEO","VIDEO",n,"Cập nhật video "+title,req);}
        }else{
            Book b=new Book();b.setTitle(title);b.setAuthor(author);b.setCategoryId(cat);b.setDescription(desc);b.setAccessLevel(access);b.setStatus(req.getParameter("status")==null?"AVAILABLE":req.getParameter("status"));b.setUploadedBy(admin.getUserId());b.setIsbn(req.getParameter("isbn"));b.setPublisher(req.getParameter("publisher"));Integer year=null;try{year=Integer.valueOf(req.getParameter("publishYear"));}catch(Exception ignored){}b.setPublishYear(year);int total=1;try{total=Math.max(1,Integer.parseInt(req.getParameter("totalCopies")));}catch(Exception ignored){}b.setTotalCopies(total);
            if(id==null||id.isEmpty()){if(stored==null){resp.sendError(400,"Sách mới phải có file.");return;}b.setAvailableCopies(total);b.setFilePath(stored);bookDAO.insert(b);log(admin,"CREATE_BOOK","BOOK",null,"Tạo sách "+title,req);}
            else{int n=parse(id);Book old=bookDAO.findById(n);b.setBookId(n);b.setAvailableCopies(Math.max(0,old.getAvailableCopies()+total-old.getTotalCopies()));b.setFilePath(stored==null?old.getFilePath():stored);bookDAO.updateWithFile(b);log(admin,"UPDATE_BOOK","BOOK",n,"Cập nhật sách "+title,req);}
        }
        resp.sendRedirect(req.getContextPath()+"/admin/content");
    }
    private void deleteContent(HttpServletRequest req,HttpServletResponse resp,User admin)throws IOException{
        String type=req.getParameter("type");int id=parse(req.getParameter("id"));
        if("DOCUMENT".equalsIgnoreCase(type))documentDAO.delete(id);else if("VIDEO".equalsIgnoreCase(type))videoDAO.delete(id);else bookDAO.delete(id);
        log(admin,"DELETE_"+type.toUpperCase(),"CONTENT",id,"Xóa nội dung #"+id,req);resp.sendRedirect(req.getContextPath()+"/admin/content");
    }
    private void toggleContent(HttpServletRequest req,HttpServletResponse resp,User admin)throws IOException{
        String type=req.getParameter("type");int id=parse(req.getParameter("id"));String status=req.getParameter("status");
        if("DOCUMENT".equalsIgnoreCase(type)){Document d=documentDAO.findById(id);d.setStatus(status);documentDAO.update(d);}else if("VIDEO".equalsIgnoreCase(type)){Video v=videoDAO.findById(id);v.setStatus(status);videoDAO.update(v);}else{Book b=bookDAO.findById(id);b.setStatus(status);bookDAO.update(b);}
        log(admin,"CHANGE_CONTENT_STATUS","CONTENT",id,"Đổi trạng thái nội dung #"+id+" thành "+status,req);resp.sendRedirect(req.getContextPath()+"/admin/content");
    }
    private void grantPermission(HttpServletRequest req,HttpServletResponse resp,User admin)throws IOException{
        String type=req.getParameter("type");int uid=parse(req.getParameter("userId"));String perm=req.getParameter("permissionType");LocalDateTime exp=parseDateTime(req.getParameter("expiryDate"));
        if("DOCUMENT".equalsIgnoreCase(type)){Permission p=new Permission();p.setUserId(uid);p.setDocumentId(parse(req.getParameter("contentId")));p.setPermissionType(perm);p.setGrantedBy(admin.getUserId());p.setExpiryDate(exp);permissionDAO.insert(p);}
        else if("VIDEO".equalsIgnoreCase(type)){VideoPermission p=new VideoPermission();p.setUserId(uid);p.setVideoId(parse(req.getParameter("contentId")));p.setPermissionType(perm);p.setGrantedBy(admin.getUserId());p.setExpiryDate(exp);videoPermissionDAO.insert(p);}
        else{BookPermission p=new BookPermission();p.setUserId(uid);p.setBookId(parse(req.getParameter("contentId")));p.setPermissionType(perm);p.setGrantedBy(admin.getUserId());p.setExpiryDate(exp);bookPermissionDAO.insert(p);}
        log(admin,"GRANT_PERMISSION","CONTENT",parse(req.getParameter("contentId")),"Cấp "+perm+" cho user #"+uid,req);resp.sendRedirect(req.getContextPath()+"/admin/permissions");
    }
    private void revokePermission(HttpServletRequest req,HttpServletResponse resp,User admin)throws IOException{
        String type=req.getParameter("type");int id=parse(req.getParameter("permissionId"));
        if("DOCUMENT".equalsIgnoreCase(type))permissionDAO.delete(id);else if("VIDEO".equalsIgnoreCase(type))videoPermissionDAO.delete(id);else bookPermissionDAO.delete(id);
        log(admin,"REVOKE_PERMISSION","PERMISSION",id,"Thu hồi quyền #"+id,req);resp.sendRedirect(req.getContextPath()+"/admin/permissions");
    }
    private void processRequest(HttpServletRequest req,HttpServletResponse resp,User admin,boolean approve)throws IOException{
        String type=req.getParameter("type");int id=parse(req.getParameter("requestId"));Timestamp exp=null;String e=req.getParameter("expiryDate");if(e!=null&&!e.isEmpty()){String ts=e.replace("T"," ");if(ts.length()==16)ts+=":00";exp=Timestamp.valueOf(ts);}
        boolean ok;
        if("DOCUMENT".equalsIgnoreCase(type))ok=approve?requestDAO.approve(id,admin.getUserId(),exp):requestDAO.reject(id,admin.getUserId());
        else if("VIDEO".equalsIgnoreCase(type))ok=approve?videoRequestDAO.approve(id,admin.getUserId(),exp):videoRequestDAO.reject(id,admin.getUserId());
        else ok=approve?bookRequestDAO.approve(id,admin.getUserId(),exp):bookRequestDAO.reject(id,admin.getUserId());
        if(ok)log(admin,approve?"APPROVE_REQUEST":"REJECT_REQUEST","PERMISSION_REQUEST",id,(approve?"Duyệt":"Từ chối")+" request #"+id,req);
        resp.sendRedirect(req.getContextPath()+"/admin/permissions");
    }
    private void saveLicense(HttpServletRequest req,HttpServletResponse resp,User admin)throws IOException{
        String type=req.getParameter("type");String id=req.getParameter("licenseId");int contentId=parse(req.getParameter("contentId"));String lt=req.getParameter("licenseType"),code=req.getParameter("licenseCode"),terms=req.getParameter("terms"),status=req.getParameter("status");
        LocalDate issued=parseDate(req.getParameter("issuedDate")),expiry=parseDate(req.getParameter("expiryDate"));
        if("DOCUMENT".equalsIgnoreCase(type)){License l=new License();l.setDocumentId(contentId);l.setLicenseType(lt);l.setLicenseCode(code);l.setIssuedDate(issued);l.setExpiryDate(expiry);l.setTerms(terms);l.setStatus(status==null?"VALID":status);if(id==null||id.isEmpty())licenseDAO.insert(l);else{l.setLicenseId(parse(id));licenseDAO.update(l);}}
        else if("VIDEO".equalsIgnoreCase(type)){VideoLicense l=new VideoLicense();l.setVideoId(contentId);l.setLicenseType(lt);l.setLicenseCode(code);l.setIssuedDate(issued);l.setExpiryDate(expiry);l.setTerms(terms);l.setStatus(status==null?"VALID":status);if(id==null||id.isEmpty())videoLicenseDAO.insert(l);else{l.setLicenseId(parse(id));videoLicenseDAO.update(l);}}
        else{BookLicense l=new BookLicense();l.setBookId(contentId);l.setLicenseType(lt);l.setLicenseCode(code);l.setIssuedDate(issued);l.setExpiryDate(expiry);l.setTerms(terms);l.setStatus(status==null?"VALID":status);if(id==null||id.isEmpty())bookLicenseDAO.insert(l);else{l.setLicenseId(parse(id));bookLicenseDAO.update(l);}}
        log(admin,id==null?"CREATE_LICENSE":"UPDATE_LICENSE","LICENSE",id==null?null:parse(id),"Quản lý license cho "+type+" #"+contentId,req);resp.sendRedirect(req.getContextPath()+"/admin/permissions");
    }
    private void deleteLicense(HttpServletRequest req,HttpServletResponse resp,User admin)throws IOException{
        String type=req.getParameter("type");int id=parse(req.getParameter("licenseId"));if("DOCUMENT".equalsIgnoreCase(type))licenseDAO.delete(id);else if("VIDEO".equalsIgnoreCase(type))videoLicenseDAO.delete(id);else bookLicenseDAO.delete(id);log(admin,"DELETE_LICENSE","LICENSE",id,"Xóa license #"+id,req);resp.sendRedirect(req.getContextPath()+"/admin/permissions");
    }
    private void revokeLicense(HttpServletRequest req,HttpServletResponse resp,User admin)throws IOException{
        String type=req.getParameter("type");int id=parse(req.getParameter("licenseId"));
        if("DOCUMENT".equalsIgnoreCase(type)){License l=licenseDAO.findById(id);l.setStatus("REVOKED");licenseDAO.update(l);}else if("VIDEO".equalsIgnoreCase(type)){VideoLicense l=videoLicenseDAO.findById(id);l.setStatus("REVOKED");videoLicenseDAO.update(l);}else{BookLicense l=bookLicenseDAO.findById(id);l.setStatus("REVOKED");bookLicenseDAO.update(l);}
        log(admin,"REVOKE_LICENSE","LICENSE",id,"Thu hồi license #"+id,req);resp.sendRedirect(req.getContextPath()+"/admin/permissions");
    }
    private void saveSettings(HttpServletRequest req,HttpServletResponse resp,User admin)throws IOException{
        Map<String,String> m=new LinkedHashMap<>();String[] keys={"libraryName","libraryDescription","maxUploadMb","defaultAccess","permissionExpiryDays","requireApproval","downloadAllowed","sessionTimeout"};
        for(String k:keys){String v=req.getParameter(k);if(v!=null)m.put(k,v);}
        settingsDAO.saveAll(m);log(admin,"UPDATE_SETTINGS","SYSTEM",null,"Cập nhật cấu hình hệ thống",req);resp.sendRedirect(req.getContextPath()+"/admin/settings?success=1");
    }
    private String saveUpload(HttpServletRequest req,Part part,String type)throws IOException,ServletException{
        String original=Paths.get(part.getSubmittedFileName()).getFileName().toString();String low=original.toLowerCase();String[] allowed="VIDEO".equalsIgnoreCase(type)?new String[]{".mp4",".webm",".mov"}:new String[]{".pdf",".doc",".docx",".txt"};
        boolean ok=false;for(String e:allowed)if(low.endsWith(e))ok=true;if(!ok)throw new ServletException("Định dạng file không được phép.");
        String ext=low.substring(low.lastIndexOf('.'));String name=UUID.randomUUID()+ext;String folder="VIDEO".equalsIgnoreCase(type)?"videos":("BOOK".equalsIgnoreCase(type)?"books":"documents");
        String real=getServletContext().getRealPath("/WEB-INF/uploads/"+folder);if(real==null)throw new IOException("Không xác định được thư mục upload.");Path dir=Paths.get(real);Files.createDirectories(dir);Files.copy(part.getInputStream(),dir.resolve(name),StandardCopyOption.REPLACE_EXISTING);return "/WEB-INF/uploads/"+folder+"/"+name;
    }
    private void forward(HttpServletRequest req,HttpServletResponse resp,String view)throws ServletException,IOException{req.getRequestDispatcher("/WEB-INF/views/"+view).forward(req,resp);}
    private int parse(String s){try{return Integer.parseInt(s);}catch(Exception e){throw new IllegalArgumentException("ID không hợp lệ.");}}
    private LocalDate parseDate(String s){try{return s==null||s.isEmpty()?null:LocalDate.parse(s);}catch(Exception e){return null;}}
    private LocalDateTime parseDateTime(String s){try{return s==null||s.isEmpty()?null:LocalDateTime.parse(s);}catch(Exception e){return null;}}
    private void log(User u,String a,String t,Integer id,String d,HttpServletRequest r){try{auditDAO.insert(u.getUserId(),a,t,id,d,r.getRemoteAddr());}catch(Exception ignored){}}
    private String json(Object x){return String.valueOf(x==null?"":x).replace("\\","\\\\").replace("\"","\\\"").replace("\n"," ");}

}
