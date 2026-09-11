package vn.edu.eaut.library.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.eaut.library.dao.CategoryDAO;
import vn.edu.eaut.library.model.Category;

import java.io.IOException;

@WebServlet("/categories/*")
public class CategoryServlet extends HttpServlet {
    private final CategoryDAO dao = new CategoryDAO();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) {
            req.setAttribute("categories", dao.findAll());
            req.getRequestDispatcher("/views/categories.jsp").forward(req, resp);
        } else if ("/add".equals(path)) {
            req.getRequestDispatcher("/views/category-form.jsp").forward(req, resp);
        } else if ("/edit".equals(path)) {
            int id = parseId(req, resp); if (id < 0) return;
            req.setAttribute("category", dao.findById(id));
            req.getRequestDispatcher("/views/category-form.jsp").forward(req, resp);
        } else if ("/delete".equals(path)) {
            int id = parseId(req, resp); if (id < 0) return;
            dao.delete(id);
            resp.sendRedirect(req.getContextPath() + "/categories");
        } else resp.sendError(404);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!"/save".equals(req.getPathInfo())) { resp.sendError(404); return; }
        String id = req.getParameter("categoryId");
        Category c = new Category();
        c.setCategoryName(req.getParameter("categoryName"));
        c.setDescription(req.getParameter("description"));
        try {
            if (id == null || id.trim().isEmpty()) dao.insert(c);
            else { c.setCategoryId(Integer.parseInt(id)); dao.update(c); }
            resp.sendRedirect(req.getContextPath() + "/categories");
        } catch (RuntimeException e) {
            req.setAttribute("error", "Không thể lưu danh mục. Tên danh mục không được để trống.");
            req.setAttribute("category", c);
            req.getRequestDispatcher("/views/category-form.jsp").forward(req, resp);
        }
    }
    private int parseId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try { return Integer.parseInt(req.getParameter("id")); }
        catch (Exception e) { resp.sendError(400, "ID không hợp lệ."); return -1; }
    }
}
