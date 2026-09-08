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
import vn.edu.eaut.library.dao.FavoriteDAO;
import vn.edu.eaut.library.dao.HistoryDAO;
import vn.edu.eaut.library.dao.VideoDAO;
import vn.edu.eaut.library.dao.VideoLicenseDAO;
import vn.edu.eaut.library.dao.VideoPermissionDAO;
import vn.edu.eaut.library.dao.VideoReviewDAO;
import vn.edu.eaut.library.model.AccessHistory;
import vn.edu.eaut.library.model.User;
import vn.edu.eaut.library.model.Video;

@WebServlet("/videos/*")
@MultipartConfig(
        maxFileSize = 1024L * 1024 * 1024,       // 1GB / video
        maxRequestSize = 1024L * 1024 * 1024 + 5 * 1024 * 1024, // + 5MB cho các field khác
        fileSizeThreshold = 5 * 1024 * 1024      // ghi ra đĩa tạm sau 5MB thay vì giữ trong RAM
)
public class VideoServlet extends HttpServlet {
    private final VideoDAO videoDAO = new VideoDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final VideoPermissionDAO permissionDAO = new VideoPermissionDAO();
    private final HistoryDAO historyDAO = new HistoryDAO();
    private final VideoLicenseDAO licenseDAO = new VideoLicenseDAO();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();
    private final VideoReviewDAO reviewDAO = new VideoReviewDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private static final long MAX_VIDEO_SIZE = 1024L * 1024 * 1024; // 1GB

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        User user = (User) req.getSession().getAttribute("currentUser");
        if (path == null || "/".equals(path)) listVideos(req, resp);
        else if ("/detail".equals(path)) viewDetail(req, resp, user);
        else if ("/add".equals(path)) { if (!staff(user, resp)) return; req.setAttribute("categories", categoryDAO.findAll()); req.getRequestDispatcher("/views/video-detail.jsp").forward(req, resp); }
        else if ("/edit".equals(path)) { if (!staff(user, resp)) return; int id=parseId(req,resp); if(id<0)return; req.setAttribute("video",videoDAO.findById(id)); req.setAttribute("categories",categoryDAO.findAll()); req.getRequestDispatcher("/views/video-detail.jsp").forward(req,resp); }
        else if ("/delete".equals(path)) { if (!staff(user,resp))return; int id=parseId(req,resp); if(id<0)return; videoDAO.delete(id); auditLogDAO.insert(user.getUserId(),"DELETE_VIDEO","VIDEO",id,"Xóa video #"+id,req.getRemoteAddr()); resp.sendRedirect(req.getContextPath()+"/videos"); }
        else resp.sendError(404);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user=(User)req.getSession().getAttribute("currentUser"); if(!staff(user,resp))return;
        if("/save".equals(req.getPathInfo())) save(req,resp,user); else resp.sendError(404);
    }

    private void listVideos(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        String keyword=req.getParameter("keyword"), cp=req.getParameter("categoryId");
        String access=req.getParameter("accessLevel"), status=req.getParameter("status"), author=req.getParameter("author");
        Integer categoryId=null; try{if(cp!=null&&!cp.isEmpty())categoryId=Integer.parseInt(cp);}catch(NumberFormatException ignored){}
        int page=1; try{page=Math.max(1,Integer.parseInt(req.getParameter("page")));}catch(Exception ignored){}
        int pageSize=10; try{pageSize=Math.max(1,Math.min(50,Integer.parseInt(req.getParameter("pageSize"))));}catch(Exception ignored){}
        int total=videoDAO.countSearch(keyword,categoryId,access,status,author);
        int totalPages=Math.max(1,(int)Math.ceil(total/(double)pageSize)); if(page>totalPages)page=totalPages;
        req.setAttribute("videos",videoDAO.search(keyword,categoryId,access,status,author,page,pageSize));
        req.setAttribute("categories",categoryDAO.findAll()); req.setAttribute("keyword",keyword); req.setAttribute("author",author);
        req.setAttribute("selectedCategoryId",categoryId); req.setAttribute("selectedAccessLevel",access); req.setAttribute("selectedStatus",status);
        req.setAttribute("currentPage",page); req.setAttribute("pageSize",pageSize); req.setAttribute("totalVideos",total); req.setAttribute("totalPages",totalPages);
        req.getRequestDispatcher("/views/videos.jsp").forward(req,resp);
    }

    private void viewDetail(HttpServletRequest req,HttpServletResponse resp,User user)throws ServletException,IOException{
        int id=parseId(req,resp); if(id<0)return; Video video=videoDAO.findById(id);
        if(video==null){resp.sendError(404,"Không tìm thấy video.");return;}
        boolean canView = canAccess(user, video, "VIEW");
        // READER vẫn được xem trang chi tiết để biết video và gửi yêu cầu cấp quyền.
        if (!canView && (video.getAccessLevel() == null || "PUBLIC".equalsIgnoreCase(video.getAccessLevel()))) {
            resp.sendError(403,"Bạn không có quyền xem video này.");
            return;
        }
        if (canView) {
            AccessHistory h=new AccessHistory();
            h.setUserId(user.getUserId());h.setTargetType("VIDEO");h.setVideoId(id);h.setActionType("VIEW");h.setIpAddress(req.getRemoteAddr());safeLog(h);
        }
        req.setAttribute("video",video);
        req.setAttribute("licenses", licenseDAO.findByVideoId(id));
        req.setAttribute("licenseValid", licenseDAO.hasValidLicense(id));
        req.setAttribute("canDownload",canAccess(user,video,"DOWNLOAD"));
        req.setAttribute("canView", canView);
        req.setAttribute("canRequestView", !canView);
        req.setAttribute("canRequestDownload", canView && !canAccess(user,video,"DOWNLOAD"));
        req.setAttribute("isFavorite", favoriteDAO.existsVideo(user.getUserId(), id));
        req.setAttribute("reviews", reviewDAO.findByVideoId(id));
        req.setAttribute("averageRating", reviewDAO.average(id));
        req.setAttribute("myReview", reviewDAO.findByUser(user.getUserId(), id));
        req.getRequestDispatcher("/views/video-detail.jsp").forward(req,resp);
    }

    private void save(HttpServletRequest req,HttpServletResponse resp,User user)throws IOException,ServletException{
        req.setCharacterEncoding("UTF-8"); String id=req.getParameter("videoId");
        String title=req.getParameter("title"),author=req.getParameter("author"),desc=req.getParameter("description"),access=req.getParameter("accessLevel");
        int cat; try{cat=Integer.parseInt(req.getParameter("categoryId"));}catch(Exception e){resp.sendError(400,"Danh mục không hợp lệ.");return;}
        Video v=new Video();v.setTitle(title);v.setAuthor(author);v.setCategoryId(cat);v.setDescription(desc);v.setAccessLevel(access);v.setStatus("AVAILABLE");
        if(id==null||id.trim().isEmpty()){
            v.setUploadedBy(user.getUserId());
            String stored;
            try {
                stored = saveUploadedFile(req);
            } catch (ServletException e) {
                req.setAttribute("error", e.getMessage());
                req.setAttribute("categories", categoryDAO.findAll());
                req.getRequestDispatcher("/views/video-detail.jsp").forward(req, resp);
                return;
            }
            if(stored==null){req.setAttribute("error","Video mới bắt buộc phải chọn file.");req.setAttribute("categories",categoryDAO.findAll());req.getRequestDispatcher("/views/video-detail.jsp").forward(req,resp);return;}
            v.setFilePath(stored);videoDAO.insert(v); auditLogDAO.insert(user.getUserId(),"CREATE_VIDEO","VIDEO",null,"Thêm video: "+title,req.getRemoteAddr());
        }else{
            int vid=Integer.parseInt(id);Video old=videoDAO.findById(vid);if(old==null){resp.sendError(404);return;}
            String stored;
            try {
                stored = saveUploadedFile(req);
            } catch (ServletException e) {
                req.setAttribute("error", e.getMessage());
                req.setAttribute("video", old);
                req.setAttribute("categories", categoryDAO.findAll());
                req.getRequestDispatcher("/views/video-detail.jsp").forward(req, resp);
                return;
            }
            v.setVideoId(vid);v.setFilePath(stored!=null?stored:old.getFilePath());videoDAO.updateWithFile(v); auditLogDAO.insert(user.getUserId(),"UPDATE_VIDEO","VIDEO",vid,"Cập nhật video: "+title,req.getRemoteAddr());
        }
        resp.sendRedirect(req.getContextPath()+"/videos");
    }

    private String saveUploadedFile(HttpServletRequest req)throws IOException,ServletException{
        Part part=req.getPart("videoFile"); if(part==null||part.getSize()==0)return null;
        if (part.getSize() > MAX_VIDEO_SIZE) throw new ServletException("Video vượt quá dung lượng tối đa cho phép (1GB).");
        String original=Paths.get(part.getSubmittedFileName()).getFileName().toString();
        String lower=original.toLowerCase();
        if(!(lower.endsWith(".mp4")||lower.endsWith(".webm")||lower.endsWith(".ogg")||lower.endsWith(".mov")||lower.endsWith(".mkv")))
            throw new ServletException("Chỉ cho phép định dạng video MP4, WEBM, OGG, MOV, MKV.");
        String ext=lower.substring(lower.lastIndexOf('.')); String fileName=UUID.randomUUID()+ext;
        String real=getServletContext().getRealPath("/WEB-INF/uploads/videos"); if(real==null)throw new IOException("Không xác định được thư mục upload video.");
        Path root=Paths.get(real);Files.createDirectories(root);try(InputStream in=part.getInputStream()){Files.copy(in,root.resolve(fileName),StandardCopyOption.REPLACE_EXISTING);}
        return "/WEB-INF/uploads/videos/"+fileName;
    }

    private boolean canAccess(User u,Video v,String type){
        if(u==null || v==null || !"AVAILABLE".equalsIgnoreCase(v.getStatus())) return false;
        if(u.isAdmin()||u.isLibrarian()) return true;
        if(!licenseDAO.hasValidLicense(v.getVideoId())) return false;
        if("PUBLIC".equalsIgnoreCase(v.getAccessLevel())&&"VIEW".equalsIgnoreCase(type)) return true;
        return permissionDAO.hasPermission(u.getUserId(),v.getVideoId(),type);
    }
    private boolean staff(User u,HttpServletResponse r)throws IOException{if(u==null||!(u.isAdmin()||u.isLibrarian())){r.sendError(403,"Chức năng chỉ dành cho ADMIN/LIBRARIAN.");return false;}return true;}
    private int parseId(HttpServletRequest req,HttpServletResponse resp)throws IOException{try{return Integer.parseInt(req.getParameter("id"));}catch(Exception e){resp.sendError(400,"ID không hợp lệ.");return -1;}}
    private void safeLog(AccessHistory h){try{historyDAO.insert(h);}catch(Exception ignored){}}
}