<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Quản lý người dùng</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/views/_navbar.jsp" %><div class="container">
<div class="card"><div class="flex-between"><div><h1>Quản lý người dùng</h1><p class="text-muted">ADMIN quản lý tài khoản, vai trò và trạng thái.</p></div><c:if test="${sessionScope.currentUser.role == 'ADMIN'}"><a class="btn btn-primary" href="${pageContext.request.contextPath}/users/add">+ Thêm người dùng</a></c:if></div></div>
<div class="card"><table><thead><tr><th>ID</th><th>Username</th><th>Họ tên</th><th>Email</th><th>Vai trò</th><th>Trạng thái</th><th>Ngày tạo</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach var="u" items="${users}"><tr><td>${u.userId}</td><td><strong>${u.username}</strong></td><td>${u.fullName}</td><td>${u.email}</td><td><span class="badge badge-public">${u.role}</span></td><td><c:choose><c:when test="${u.status == 'ACTIVE'}"><span class="badge badge-valid">ACTIVE</span></c:when><c:otherwise><span class="badge badge-expired">LOCKED</span></c:otherwise></c:choose></td><td>${u.createdAt}</td><td>
<c:if test="${sessionScope.currentUser.role == 'ADMIN'}">
<a href="${pageContext.request.contextPath}/users/edit?id=${u.userId}">Sửa</a>
<c:choose><c:when test="${u.userId == sessionScope.currentUser.userId}"><span class="text-muted"> | Tài khoản hiện tại</span></c:when><c:otherwise>
 | <form action="${pageContext.request.contextPath}/users/toggle-status" method="post" style="display:inline"><input type="hidden" name="id" value="${u.userId}"><button type="submit" class="link-button" onclick="return confirm('${u.status == 'ACTIVE' ? 'Khóa' : 'Mở khóa'} tài khoản này?');">${u.status == 'ACTIVE' ? 'Khóa' : 'Mở khóa'}</button></form>
 | <a href="${pageContext.request.contextPath}/users/delete?id=${u.userId}" onclick="return confirm('Xóa tài khoản này?');" style="color:#dc2626">Xóa</a>
</c:otherwise></c:choose>
</c:if>
<c:if test="${sessionScope.currentUser.role == 'AUDITOR'}"><span class="text-muted">Chỉ xem</span></c:if>
</td></tr></c:forEach>
</tbody></table></div></div></body></html>
