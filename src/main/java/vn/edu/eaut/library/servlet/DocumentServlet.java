package vn.edu.eaut.library.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import vn.edu.eaut.library.dao.AuditLogDAO;
import vn.edu.eaut.library.dao.CategoryDAO;
import vn.edu.eaut.library.dao.DocumentDAO;
import vn.edu.eaut.library.dao.FavoriteDAO;
import vn.edu.eaut.library.dao.HistoryDAO;
import vn.edu.eaut.library.dao.LicenseDAO;
import vn.edu.eaut.library.dao.PermissionDAO;
import vn.edu.eaut.library.dao.ReviewDAO;
import vn.edu.eaut.library.model.AccessHistory;
import vn.edu.eaut.library.model.Document;
import vn.edu.eaut.library.model.User;

@WebServlet("/documents/*")
@MultipartConfig(maxFileSize = 20 * 1024 * 1024, maxRequestSize = 25 * 1024 * 1024)
public class DocumentServlet extends HttpServlet {
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();
    private final HistoryDAO historyDAO = new HistoryDAO();
    private final LicenseDAO licenseDAO = new LicenseDAO();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        User user = (User) req.getSession().getAttribute("currentUser");
        if (path == null || "/".equals(path)) listDocuments(req, resp);
        else if ("/detail".equals(path)) viewDetail(req, resp, user);
        else if ("/add".equals(path)) { if (!staff(user, resp)) return; req.setAttribute("categories", categoryDAO.findAll()); req.getRequestDispatcher("/views/document-detail.jsp").forward(req, resp); }
        else if ("/edit".equals(path)) { if (!staff(user, resp)) return; int id=parseId(req,resp); if(id<0)return; req.setAttribute("document",documentDAO.findById(id)); req.setAttribute("categories",categoryDAO.findAll()); req.getRequestDispatcher("/views/document-detail.jsp").forward(req,resp); }
        else if ("/delete".equals(path)) { if (!staff(user,resp))return; int id=parseId(req,resp); if(id<0)return; documentDAO.delete(id); auditLogDAO.insert(user.getUserId(),"DELETE_DOCUMENT","DOCUMENT",id,"Xóa tài liệu #"+id,req.getRemoteAddr()); resp.sendRedirect(req.getContextPath()+"/documents"); }
        else resp.sendError(404);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user=(User)req.getSession().getAttribute("currentUser"); if(!staff(user,resp))return;
        if("/save".equals(req.getPathInfo())) save(req,resp,user); else resp.sendError(404);
    }

    private void listDocuments(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        String keyword=req.getParameter("keyword"), cp=req.getParameter("categoryId");
        String access=req.getParameter("accessLevel"), status=req.getParameter("status"), author=req.getParameter("author");
        Integer categoryId=null; try{if(cp!=null&&!cp.isEmpty())categoryId=Integer.parseInt(cp);}catch(NumberFormatException ignored){}
        int page=1; try{page=Math.max(1,Integer.parseInt(req.getParameter("page")));}catch(Exception ignored){}
        int pageSize=10; try{pageSize=Math.max(1,Math.min(50,Integer.parseInt(req.getParameter("pageSize"))));}catch(Exception ignored){}
        int total=documentDAO.countSearch(keyword,categoryId,access,status,author);
        int totalPages=Math.max(1,(int)Math.ceil(total/(double)pageSize)); if(page>totalPages)page=totalPages;
        req.setAttribute("documents",documentDAO.search(keyword,categoryId,access,status,author,page,pageSize));
        req.setAttribute("categories",categoryDAO.findAll()); req.setAttribute("keyword",keyword); req.setAttribute("author",author);
        req.setAttribute("selectedCategoryId",categoryId); req.setAttribute("selectedAccessLevel",access); req.setAttribute("selectedStatus",status);
        req.setAttribute("currentPage",page); req.setAttribute("pageSize",pageSize); req.setAttribute("totalDocuments",total); req.setAttribute("totalPages",totalPages);
        req.getRequestDispatcher("/views/documents.jsp").forward(req,resp);
    }
    private void viewDetail(HttpServletRequest req,HttpServletResponse resp,User user)throws ServletException,IOException{
        int id=parseId(req,resp); if(id<0)return; Document doc=documentDAO.findById(id);
        if(doc==null){resp.sendError(404,"Không tìm thấy tài liệu.");return;}
        boolean canView = canAccess(user, doc, "VIEW");
        // READER vẫn được xem trang chi tiết để biết tài liệu và gửi yêu cầu cấp quyền.
        // Chỉ nội dung/file PDF mới bị chặn nếu chưa có quyền VIEW.
        if (!canView && (doc.getAccessLevel() == null || "PUBLIC".equalsIgnoreCase(doc.getAccessLevel()))) {
            resp.sendError(403,"Bạn không có quyền xem tài liệu này.");
            return;
        }
        if (canView) {
            AccessHistory h=new AccessHistory();
            h.setUserId(user.getUserId());h.setDocumentId(id);h.setActionType("VIEW");h.setIpAddress(req.getRemoteAddr());safeLog(h);
        }
        req.setAttribute("document",doc);
        req.setAttribute("licenses", licenseDAO.findByDocumentId(id));
        req.setAttribute("licenseValid", licenseDAO.hasValidLicense(id));
        req.setAttribute("canDownload",canAccess(user,doc,"DOWNLOAD"));
        req.setAttribute("canView", canView);
        req.setAttribute("canRequestView", !canView);
        req.setAttribute("canRequestDownload", canView && !canAccess(user,doc,"DOWNLOAD"));
        req.setAttribute("isFavorite", favoriteDAO.exists(user.getUserId(), id));
        req.setAttribute("reviews", reviewDAO.findByDocumentId(id));
        req.setAttribute("averageRating", reviewDAO.average(id));
        req.setAttribute("myReview", reviewDAO.findByUser(user.getUserId(), id));
        req.getRequestDispatcher("/views/document-detail.jsp").forward(req,resp);
    }
    private void save(HttpServletRequest req,HttpServletResponse resp,User user)throws IOException,ServletException{
        req.setCharacterEncoding("UTF-8"); String id=req.getParameter("documentId");
        String title=req.getParameter("title"),author=req.getParameter("author"),desc=req.getParameter("description"),access=req.getParameter("accessLevel");
        int cat; try{cat=Integer.parseInt(req.getParameter("categoryId"));}catch(Exception e){resp.sendError(400,"Danh mục không hợp lệ.");return;}
        Document doc=new Document();doc.setTitle(title);doc.setAuthor(author);doc.setCategoryId(cat);doc.setDescription(desc);doc.setAccessLevel(access);doc.setStatus("AVAILABLE");
        if(id==null||id.trim().isEmpty()){
            doc.setUploadedBy(user.getUserId()); String stored=saveUploadedFile(req);
            if(stored==null){req.setAttribute("error","Tài liệu mới bắt buộc phải chọn file.");req.setAttribute("categories",categoryDAO.findAll());req.getRequestDispatcher("/views/document-detail.jsp").forward(req,resp);return;}
            doc.setFilePath(stored);documentDAO.insert(doc); auditLogDAO.insert(user.getUserId(),"CREATE_DOCUMENT","DOCUMENT",null,"Thêm tài liệu: "+title,req.getRemoteAddr());
        }else{
            int did=Integer.parseInt(id);Document old=documentDAO.findById(did);if(old==null){resp.sendError(404);return;}
            String stored=saveUploadedFile(req);doc.setDocumentId(did);doc.setFilePath(stored!=null?stored:old.getFilePath());documentDAO.updateWithFile(doc); auditLogDAO.insert(user.getUserId(),"UPDATE_DOCUMENT","DOCUMENT",did,"Cập nhật tài liệu: "+title,req.getRemoteAddr());
        }
        resp.sendRedirect(req.getContextPath()+"/documents");
    }
    private String saveUploadedFile(HttpServletRequest req)throws IOException,ServletException{
        Part part=req.getPart("documentFile"); if(part==null||part.getSize()==0)return null;
        String original=Paths.get(part.getSubmittedFileName()).getFileName().toString();
        String lower=original.toLowerCase(); if(!(lower.endsWith(".pdf")||lower.endsWith(".doc")||lower.endsWith(".docx")||lower.endsWith(".txt")))throw new ServletException("Chỉ cho phép PDF, DOC, DOCX, TXT.");
        String ext=lower.substring(lower.lastIndexOf('.')); String fileName=UUID.randomUUID()+ext;
        String real=getServletContext().getRealPath("/WEB-INF/uploads"); if(real==null)throw new IOException("Không xác định được thư mục upload.");
        Path root=Paths.get(real);Files.createDirectories(root);try(InputStream in=part.getInputStream()){Files.copy(in,root.resolve(fileName),StandardCopyOption.REPLACE_EXISTING);}
        return "/WEB-INF/uploads/"+fileName;
    }
    private boolean canAccess(User u,Document d,String type){
        if(u==null || d==null || !"AVAILABLE".equalsIgnoreCase(d.getStatus())) return false;
        if(u.isAdmin()||u.isLibrarian()) return true;
        if(u.isAuditor()) return "VIEW".equalsIgnoreCase(type);
        if(!licenseDAO.hasValidLicense(d.getDocumentId())) return false;
        if("PUBLIC".equalsIgnoreCase(d.getAccessLevel())&&"VIEW".equalsIgnoreCase(type)) return true;
        return permissionDAO.hasPermission(u.getUserId(),d.getDocumentId(),type);
    }
    private boolean staff(User u,HttpServletResponse r)throws IOException{if(u==null||!(u.isAdmin()||u.isLibrarian())){r.sendError(403,"Chức năng chỉ dành cho ADMIN/LIBRARIAN.");return false;}return true;}
    private int parseId(HttpServletRequest req,HttpServletResponse resp)throws IOException{try{return Integer.parseInt(req.getParameter("id"));}catch(Exception e){resp.sendError(400,"ID không hợp lệ.");return -1;}}
    private void safeLog(AccessHistory h){try{historyDAO.insert(h);}catch(Exception ignored){}}
}