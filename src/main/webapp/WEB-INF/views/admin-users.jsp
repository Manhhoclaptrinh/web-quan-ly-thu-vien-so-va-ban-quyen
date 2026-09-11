<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Admin - Người dùng</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css"></head>
<body class="admin-body"><div class="admin-shell"><%@ include file="/WEB-INF/views/admin-nav.jsp" %><main class="admin-main">
<div class="admin-head"><div><h1>Người dùng</h1><p>Chỉ xem username, thông tin, role và trạng thái.</p></div><span class="admin-badge">MONITORING ONLY</span></div>
<section class="panel"><table class="table"><thead><tr><th>ID</th><th>Username</th><th>Họ tên</th><th>Email</th><th>Role</th><th>Trạng thái</th><th>Ngày tạo</th></tr></thead><tbody><c:forEach var="u" items="${users}"><tr><td>${u.userId}</td><td>${u.username}</td><td>${u.fullName}</td><td>${u.email}</td><td><span class="tag">${u.role}</span></td><td><span class="tag">${u.status}</span></td><td>${u.createdAt}</td></tr></c:forEach></tbody></table></section>
</main></div></body></html>