<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Tài liệu yêu thích</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/views/_navbar.jsp" %><div class="container"><div class="card">
<h1>⭐ Tài liệu yêu thích</h1><p class="text-muted">Danh sách tài liệu bạn đã đánh dấu để xem lại nhanh.</p>
<c:choose><c:when test="${empty favorites}"><div class="empty-state"><strong>Chưa có tài liệu yêu thích</strong><span>Hãy mở một tài liệu và chọn “Thêm yêu thích”.</span></div></c:when>
<c:otherwise><div class="table-wrap"><table><thead><tr><th>Tài liệu</th><th>Tác giả</th><th>Ngày thêm</th><th></th></tr></thead><tbody>
<c:forEach var="f" items="${favorites}"><tr><td><strong>${f.documentTitle}</strong></td><td>${f.author}</td><td>${f.createdAt}</td><td><a href="${pageContext.request.contextPath}/documents/detail?id=${f.documentId}">Xem →</a></td></tr></c:forEach>
</tbody></table></div></c:otherwise></c:choose>
</div></div></body></html>