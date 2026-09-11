<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Admin - Audit Logs</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css"></head>
<body class="admin-body"><div class="admin-shell"><%@ include file="/WEB-INF/views/admin-nav.jsp" %><main class="admin-main">
<div class="admin-head"><div><h1>Audit Logs</h1><p>Nhật ký thao tác hệ thống để ADMIN giám sát.</p></div><span class="admin-badge">READ-ONLY</span></div>
<section class="panel"><table class="table"><thead><tr><th>ID</th><th>Thời gian</th><th>User</th><th>Action</th><th>Target</th><th>Mô tả</th><th>IP</th></tr></thead><tbody><c:forEach var="a" items="${auditLogs}"><tr><td>${a.auditId}</td><td>${a.createdAt}</td><td>${a.username}</td><td>${a.actionType}</td><td>${a.targetType} #${a.targetId}</td><td>${a.description}</td><td>${a.ipAddress}</td></tr></c:forEach></tbody></table></section>
</main></div></body></html>