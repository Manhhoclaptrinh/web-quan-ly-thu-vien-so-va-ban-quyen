<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Admin - Báo cáo</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css"></head>
<body class="admin-body"><div class="admin-shell"><%@ include file="/WEB-INF/views/admin-nav.jsp" %><main class="admin-main">
<div class="admin-head"><div><h1>Báo cáo</h1><p>Báo cáo tổng hợp người dùng, nội dung, truy cập và hoạt động.</p></div><span class="admin-badge">SYSTEM REPORTS</span></div>
<div class="grid2"><section class="panel"><h2>Báo cáo người dùng</h2><p>Tổng người dùng: <strong>${overview.users}</strong></p><p>Đang hoạt động: <strong>${overview.activeUsers}</strong></p></section>
<section class="panel"><h2>Báo cáo nội dung</h2><p>Documents: <strong>${overview.documents}</strong></p><p>Videos: <strong>${overview.videos}</strong></p><p>Books: <strong>${overview.books}</strong></p></section>
<section class="panel"><h2>Báo cáo truy cập</h2><p>Tổng lượt truy cập/lịch sử: <strong>${overview.accessHistory}</strong></p></section>
<section class="panel"><h2>Báo cáo hoạt động</h2><p>Audit logs: <strong>${overview.auditLogs}</strong></p><p>Permissions: <strong>${overview.permissions}</strong></p><p>Permission requests: <strong>${overview.permissionRequests}</strong></p><p>Licenses: <strong>${overview.licenses}</strong></p></section></div>
</main></div></body></html>