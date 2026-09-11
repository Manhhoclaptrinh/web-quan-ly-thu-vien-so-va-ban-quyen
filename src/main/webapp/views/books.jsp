<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sách - Thư viện số</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/views/_navbar.jsp" %>
<div class="container">
    <div class="card">
        <div class="flex-between">
            <div>
                <h1>📚 Danh sách sách</h1>
                <p class="text-muted">Quản lý và tra cứu kho sách điện tử.</p>
            </div>
            <c:if test="${not isAuditor and (sessionScope.currentUser.role == 'ADMIN' or sessionScope.currentUser.role == 'LIBRARIAN')}">
                <a href="${pageContext.request.contextPath}/books/add" class="btn btn-primary">+ Thêm sách</a>
            </c:if>
        </div>

        <form action="${pageContext.request.contextPath}/books" method="get" class="advanced-search mt-3">
            <input type="text" name="keyword" class="form-control" placeholder="Tên sách, tác giả, ISBN, mô tả..." value="${keyword}">
            <input type="text" name="author" class="form-control" placeholder="Tác giả" value="${author}">
            <select name="categoryId" class="form-control">
                <option value="">-- Tất cả danh mục --</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="${cat.categoryId}" ${cat.categoryId == selectedCategoryId ? 'selected' : ''}>${cat.categoryName}</option>
                </c:forEach>
            </select>
            <select name="accessLevel" class="form-control">
                <option value="">-- Quyền truy cập --</option>
                <option value="PUBLIC" ${selectedAccessLevel == 'PUBLIC' ? 'selected' : ''}>PUBLIC</option>
                <option value="RESTRICTED" ${selectedAccessLevel == 'RESTRICTED' ? 'selected' : ''}>RESTRICTED</option>
                <option value="PRIVATE" ${selectedAccessLevel == 'PRIVATE' ? 'selected' : ''}>PRIVATE</option>
            </select>
            <select name="status" class="form-control">
                <option value="">-- Trạng thái --</option>
                <option value="AVAILABLE" ${selectedStatus == 'AVAILABLE' ? 'selected' : ''}>AVAILABLE</option>
                <option value="DISABLED" ${selectedStatus == 'DISABLED' ? 'selected' : ''}>DISABLED</option>
            </select>
            <input type="hidden" name="pageSize" value="${pageSize}">
            <button type="submit" class="btn btn-secondary">Tìm kiếm</button>
            <a href="${pageContext.request.contextPath}/books" class="btn btn-secondary">Xóa lọc</a>
        </form>

        <p class="text-muted">Tìm thấy <strong>${totalBooks}</strong> sách.</p>

        <c:choose>
            <c:when test="${empty books}">
                <div class="empty-state">📭<strong>Không tìm thấy sách</strong><span>Thử thay đổi điều kiện tìm kiếm.</span></div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table>
                        <thead>
                        <tr>
                            <th>Tên sách</th><th>Tác giả</th><th>ISBN</th><th>Danh mục</th>
                            <th>Bản in</th><th>Quyền</th><th>Trạng thái</th><th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="b" items="${books}">
                            <tr>
                                <td><strong>${b.title}</strong><br><small>${b.publisher}</small></td>
                                <td>${b.author}</td>
                                <td>${b.isbn}</td>
                                <td>${b.categoryName}</td>
                                <td>${b.availableCopies}/${b.totalCopies}</td>
                                <td><span class="badge badge-${b.accessLevel == 'PUBLIC' ? 'public' : b.accessLevel == 'RESTRICTED' ? 'restricted' : 'private'}">${b.accessLevel}</span></td>
                                <td>${b.status}</td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/books/detail?id=${b.bookId}">Xem</a>
                                    <c:if test="${not isAuditor and (sessionScope.currentUser.role == 'ADMIN' or sessionScope.currentUser.role == 'LIBRARIAN')}">
                                        | <a href="${pageContext.request.contextPath}/books/edit?id=${b.bookId}">Sửa</a>
                                        | <a href="${pageContext.request.contextPath}/books/delete?id=${b.bookId}" onclick="return confirm('Xóa sách này?');" style="color:#dc2626">Xóa</a>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
                <c:if test="${totalPages > 1}">
                    <div class="pagination">
                        <c:if test="${currentPage > 1}">
                            <a href="${pageContext.request.contextPath}/books?keyword=${keyword}&author=${author}&categoryId=${selectedCategoryId}&accessLevel=${selectedAccessLevel}&status=${selectedStatus}&page=${currentPage-1}&pageSize=${pageSize}">← Trước</a>
                        </c:if>
                        <c:forEach var="p" begin="1" end="${totalPages}">
                            <a class="${p == currentPage ? 'active' : ''}" href="${pageContext.request.contextPath}/books?keyword=${keyword}&author=${author}&categoryId=${selectedCategoryId}&accessLevel=${selectedAccessLevel}&status=${selectedStatus}&page=${p}&pageSize=${pageSize}">${p}</a>
                        </c:forEach>
                        <c:if test="${currentPage < totalPages}">
                            <a href="${pageContext.request.contextPath}/books?keyword=${keyword}&author=${author}&categoryId=${selectedCategoryId}&accessLevel=${selectedAccessLevel}&status=${selectedStatus}&page=${currentPage+1}&pageSize=${pageSize}">Sau →</a>
                        </c:if>
                    </div>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>
</div>
</body>
</html>
