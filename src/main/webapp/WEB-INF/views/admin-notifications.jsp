<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Admin - Thông báo</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css"></head>
<body class="admin-body"><div class="admin-shell"><%@ include file="/WEB-INF/views/admin-nav.jsp" %><main class="admin-main">
<header class="admin-head"><div><div class="eyebrow">NOTIFICATION CENTER</div><h1>Thông báo quản trị</h1><p>Các cảnh báo cần ADMIN chú ý và xử lý.</p></div></header>
<div class="todo-panel panel">
<a href="${pageContext.request.contextPath}/admin/permissions"><b>📩 ${overview.permissionRequests}</b><span>Permission requests đang chờ duyệt</span><strong>→</strong></a>
<a href="${pageContext.request.contextPath}/admin/permissions"><b>📜 ${overview.expiringLicenses}</b><span>License sắp hết hạn trong 7 ngày</span><strong>→</strong></a>
<a href="${pageContext.request.contextPath}/admin/security"><b>🚨 ${overview.failedLogins}</b><span>Đăng nhập lỗi trong 24 giờ</span><strong>→</strong></a>
<a href="${pageContext.request.contextPath}/admin/users"><b>🔒 ${overview.lockedUsers}</b><span>Tài khoản đang bị khóa</span><strong>→</strong></a>
</div>
<section class="panel"><h2>Sự kiện bảo mật gần đây</h2><c:forEach var="e" items="${events}"><div class="notification-row"><span>🚨</span><div><b>${e.action} — ${e.username}</b><small>${e.description} · ${e.ip}</small></div><time>${e.createdAt}</time></div></c:forEach><c:if test="${empty events}"><div class="empty">Không có cảnh báo mới.</div></c:if></section>
</main></div></body></html>
