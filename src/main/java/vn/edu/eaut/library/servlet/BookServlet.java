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
import vn.edu.eaut.library.dao.BookDAO;
import vn.edu.eaut.library.dao.BookLicenseDAO;
import vn.edu.eaut.library.dao.BookPermissionDAO;
import vn.edu.eaut.library.dao.BookReviewDAO;
import vn.edu.eaut.library.dao.CategoryDAO;
import vn.edu.eaut.library.dao.FavoriteDAO;
import vn.edu.eaut.library.dao.HistoryDAO;
import vn.edu.eaut.library.model.AccessHistory;
import vn.edu.eaut.library.model.Book;
import vn.edu.eaut.library.model.User;

@WebServlet("/books/*")
@MultipartConfig(maxFileSize = 20 * 1024 * 1024, maxRequestSize = 25 * 1024 * 1024)
public class BookServlet extends HttpServlet {
    private final BookDAO bookDAO = new BookDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BookPermissionDAO permissionDAO = new BookPermissionDAO();
    private final HistoryDAO historyDAO = new HistoryDAO();
    private final BookLicenseDAO licenseDAO = new BookLicenseDAO();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();
    private final BookReviewDAO reviewDAO = new BookReviewDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        User user = (User) req.getSession().getAttribute("currentUser");
        if (path == null || "/".equals(path)) listBooks(req, resp);
        else if ("/detail".equals(path)) viewDetail(req, resp, user);
        else if ("/add".equals(path)) { if (!staff(user, resp)) return; req.setAttribute("categories", categoryDAO.findAll()); req.getRequestDispatcher("/views/book-detail.jsp").forward(req, resp); }
        else if ("/edit".equals(path)) { if (!staff(user, resp)) return; int id=parseId(req,resp); if(id<0)return; req.setAttribute("book",bookDAO.findById(id)); req.setAttribute("categories",categoryDAO.findAll()); req.getRequestDispatcher("/views/book-detail.jsp").forward(req,resp); }
        else if ("/delete".equals(path)) { if (!staff(user,resp))return; int id=parseId(req,resp); if(id<0)return; bookDAO.delete(id); auditLogDAO.insert(user.getUserId(),"DELETE_BOOK","BOOK",id,"Xóa sách #"+id,req.getRemoteAddr()); resp.sendRedirect(req.getContextPath()+"/books"); }
        else resp.sendError(404);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user=(User)req.getSession().getAttribute("currentUser"); if(!staff(user,resp))return;
        if("/save".equals(req.getPathInfo())) save(req,resp,user); else resp.sendError(404);
    }

    private void listBooks(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        String keyword=req.getParameter("keyword"), cp=req.getParameter("categoryId");
        String access=req.getParameter("accessLevel"), status=req.getParameter("status"), author=req.getParameter("author");
        Integer categoryId=null; try{if(cp!=null&&!cp.isEmpty())categoryId=Integer.parseInt(cp);}catch(NumberFormatException ignored){}
        int page=1; try{page=Math.max(1,Integer.parseInt(req.getParameter("page")));}catch(Exception ignored){}
        int pageSize=10; try{pageSize=Math.max(1,Math.min(50,Integer.parseInt(req.getParameter("pageSize"))));}catch(Exception ignored){}
        int total=bookDAO.countSearch(keyword,categoryId,access,status,author);
        int totalPages=Math.max(1,(int)Math.ceil(total/(double)pageSize)); if(page>totalPages)page=totalPages;
        req.setAttribute("books",bookDAO.search(keyword,categoryId,access,status,author,page,pageSize));
        req.setAttribute("categories",categoryDAO.findAll()); req.setAttribute("keyword",keyword); req.setAttribute("author",author);
        req.setAttribute("selectedCategoryId",categoryId); req.setAttribute("selectedAccessLevel",access); req.setAttribute("selectedStatus",status);
        req.setAttribute("currentPage",page); req.setAttribute("pageSize",pageSize); req.setAttribute("totalBooks",total); req.setAttribute("totalPages",totalPages);
        req.getRequestDispatcher("/views/books.jsp").forward(req,resp);
    }
    private void viewDetail(HttpServletRequest req,HttpServletResponse resp,User user)throws ServletException,IOException{
        int id=parseId(req,resp); if(id<0)return; Book book=bookDAO.findById(id);
        if(book==null){resp.sendError(404,"Không tìm thấy sách.");return;}
        boolean canView = canAccess(user, book, "VIEW");
        // READER vẫn được xem trang chi tiết để biết sách và gửi yêu cầu cấp quyền.
        if (!canView && (book.getAccessLevel() == null || "PUBLIC".equalsIgnoreCase(book.getAccessLevel()))) {
            resp.sendError(403,"Bạn không có quyền xem sách này.");
            return;
        }
        if (canView) {
            AccessHistory h=new AccessHistory();
            h.setUserId(user.getUserId());h.setTargetType("BOOK");h.setBookId(id);h.setActionType("VIEW");h.setIpAddress(req.getRemoteAddr());safeLog(h);
        }
        req.setAttribute("book",book);
        req.setAttribute("licenses", licenseDAO.findByBookId(id));
        req.setAttribute("licenseValid", licenseDAO.hasValidLicense(id));
        req.setAttribute("canDownload",canAccess(user,book,"DOWNLOAD"));
        req.setAttribute("canView", canView);
        req.setAttribute("canRequestView", !canView);
        req.setAttribute("canRequestDownload", canView && !canAccess(user,book,"DOWNLOAD"));
        req.setAttribute("isFavorite", favoriteDAO.existsBook(user.getUserId(), id));
        req.setAttribute("reviews", reviewDAO.findByBookId(id));
        req.setAttribute("averageRating", reviewDAO.average(id));
        req.setAttribute("myReview", reviewDAO.findByUser(user.getUserId(), id));
        req.getRequestDispatcher("/views/book-detail.jsp").forward(req,resp);
    }
    private void save(HttpServletRequest req,HttpServletResponse resp,User user)throws IOException,ServletException{
        req.setCharacterEncoding("UTF-8"); String id=req.getParameter("bookId");
        String title=req.getParameter("title"),author=req.getParameter("author"),desc=req.getParameter("description"),access=req.getParameter("accessLevel");
        String isbn=req.getParameter("isbn"),publisher=req.getParameter("publisher");
        int cat; try{cat=Integer.parseInt(req.getParameter("categoryId"));}catch(Exception e){resp.sendError(400,"Danh mục không hợp lệ.");return;}
        Integer publishYear=null; try{String py=req.getParameter("publishYear"); if(py!=null&&!py.trim().isEmpty())publishYear=Integer.parseInt(py.trim());}catch(NumberFormatException ignored){}
        int totalCopies=1; try{String tc=req.getParameter("totalCopies"); if(tc!=null&&!tc.trim().isEmpty())totalCopies=Math.max(1,Integer.parseInt(tc.trim()));}catch(NumberFormatException ignored){}

        Book book=new Book();book.setTitle(title);book.setAuthor(author);book.setCategoryId(cat);book.setDescription(desc);book.setAccessLevel(access);book.setStatus("AVAILABLE");
        book.setIsbn(isbn);book.setPublisher(publisher);book.setPublishYear(publishYear);book.setTotalCopies(totalCopies);
        if(id==null||id.trim().isEmpty()){
            book.setAvailableCopies(totalCopies);
            book.setUploadedBy(user.getUserId()); String stored=saveUploadedFile(req);
            if(stored==null){req.setAttribute("error","Sách mới bắt buộc phải chọn file.");req.setAttribute("categories",categoryDAO.findAll());req.getRequestDispatcher("/views/book-detail.jsp").forward(req,resp);return;}
            book.setFilePath(stored);bookDAO.insert(book); auditLogDAO.insert(user.getUserId(),"CREATE_BOOK","BOOK",null,"Thêm sách: "+title,req.getRemoteAddr());
        }else{
            int bid=Integer.parseInt(id);Book old=bookDAO.findById(bid);if(old==null){resp.sendError(404);return;}
            // Giữ nguyên tỉ lệ số bản còn lại khi cập nhật tổng số bản.
            int diff=totalCopies-old.getTotalCopies();
            book.setAvailableCopies(Math.max(0, old.getAvailableCopies()+diff));
            String stored=saveUploadedFile(req);book.setBookId(bid);book.setFilePath(stored!=null?stored:old.getFilePath());bookDAO.updateWithFile(book); auditLogDAO.insert(user.getUserId(),"UPDATE_BOOK","BOOK",bid,"Cập nhật sách: "+title,req.getRemoteAddr());
        }
        resp.sendRedirect(req.getContextPath()+"/books");
    }
    private String saveUploadedFile(HttpServletRequest req)throws IOException,ServletException{
        Part part=req.getPart("bookFile"); if(part==null||part.getSize()==0)return null;
        String original=Paths.get(part.getSubmittedFileName()).getFileName().toString();
        String lower=original.toLowerCase(); if(!(lower.endsWith(".pdf")||lower.endsWith(".doc")||lower.endsWith(".docx")||lower.endsWith(".txt")))throw new ServletException("Chỉ cho phép PDF, DOC, DOCX, TXT.");
        String ext=lower.substring(lower.lastIndexOf('.')); String fileName=UUID.randomUUID()+ext;
        String real=getServletContext().getRealPath("/WEB-INF/uploads/books"); if(real==null)throw new IOException("Không xác định được thư mục upload sách.");
        Path root=Paths.get(real);Files.createDirectories(root);try(InputStream in=part.getInputStream()){Files.copy(in,root.resolve(fileName),StandardCopyOption.REPLACE_EXISTING);}
        return "/WEB-INF/uploads/books/"+fileName;
    }
    private boolean canAccess(User u,Book b,String type){
        if(u==null || b==null || !"AVAILABLE".equalsIgnoreCase(b.getStatus())) return false;
        if(u.isAdmin()||u.isLibrarian()) return true;
        if(u.isAuditor()) return "VIEW".equalsIgnoreCase(type);
        if(!licenseDAO.hasValidLicense(b.getBookId())) return false;
        if("PUBLIC".equalsIgnoreCase(b.getAccessLevel())&&"VIEW".equalsIgnoreCase(type)) return true;
        return permissionDAO.hasPermission(u.getUserId(),b.getBookId(),type);
    }
    private boolean staff(User u,HttpServletResponse r)throws IOException{if(u==null||!(u.isAdmin()||u.isLibrarian())){r.sendError(403,"Chức năng chỉ dành cho ADMIN/LIBRARIAN.");return false;}return true;}
    private int parseId(HttpServletRequest req,HttpServletResponse resp)throws IOException{try{return Integer.parseInt(req.getParameter("id"));}catch(Exception e){resp.sendError(400,"ID không hợp lệ.");return -1;}}
    private void safeLog(AccessHistory h){try{historyDAO.insert(h);}catch(Exception ignored){}}
}