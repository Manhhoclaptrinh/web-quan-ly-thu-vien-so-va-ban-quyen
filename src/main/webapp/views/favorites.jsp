<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Mục yêu thích</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/views/_navbar.jsp" %><div class="container"><div class="card">
<h1>⭐ Mục yêu thích</h1><p class="text-muted">Danh sách tài liệu, video và sách bạn đã đánh dấu để xem lại nhanh.</p>
<c:choose><c:when test="${empty favorites}"><div class="empty-state"><strong>Chưa có mục yêu thích</strong><span>Hãy mở tài liệu, video hoặc sách và chọn “Thêm yêu thích”.</span></div></c:when>
<c:otherwise><div class="table-wrap"><table><thead><tr><th>Loại</th><th>Tên</th><th>Tác giả</th><th>Ngày thêm</th><th></th></tr></thead><tbody>
<c:forEach var="f" items="${favorites}"><tr><td>${f.isBook ? '📚 Sách' : f.isVideo ? '🎬 Video' : '📄 Tài liệu'}</td><td><strong>${f.documentTitle}</strong></td><td>${f.author}</td><td>${f.createdAt}</td><td><c:choose><c:when test="${f.isBook}"><a href="${pageContext.request.contextPath}/books/detail?id=${f.bookId}">Xem →</a></c:when><c:when test="${f.isVideo}"><a href="${pageContext.request.contextPath}/videos/detail?id=${f.videoId}">Xem →</a></c:when><c:otherwise><a href="${pageContext.request.contextPath}/documents/detail?id=${f.documentId}">Xem →</a></c:otherwise></c:choose></td></tr></c:forEach>
</tbody></table></div></c:otherwise></c:choose>
</div></div></body></html>