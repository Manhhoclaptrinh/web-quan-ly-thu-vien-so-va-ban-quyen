<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Admin - Audit Logs</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css"></head>
<body class="admin-body"><div class="admin-shell"><%@ include file="/WEB-INF/views/admin-nav.jsp" %><main class="admin-main">
<header class="admin-head"><div><div class="eyebrow">SECURITY AUDIT</div><h1>Audit Logs</h1><p>Nhật ký thao tác để truy vết hoạt động quản trị và hệ thống.</p></div></header>
<section class="panel"><div class="toolbar"><input id="auditFilter" placeholder="🔎 Tìm user, action, mô tả, IP..."></div><table class="table" id="auditTable"><thead><tr><th>ID</th><th>Thời gian</th><th>User</th><th>Action</th><th>Target</th><th>Mô tả</th><th>IP</th></tr></thead><tbody><c:forEach var="a" items="${auditLogs}"><tr><td>#${a.auditId}</td><td>${a.createdAt}</td><td>${a.username}</td><td><span class="badge">${a.actionType}</span></td><td>${a.targetType} ${a.targetId}</td><td>${a.description}</td><td>${a.ipAddress}</td></tr></c:forEach></tbody></table></section>
<script>document.getElementById('auditFilter').addEventListener('input',function(){const q=this.value.toLowerCase();document.querySelectorAll('#auditTable tbody tr').forEach(x=>x.style.display=x.innerText.toLowerCase().includes(q)?'':'none')});</script>
</main></div></body></html>
