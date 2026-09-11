<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Admin Dashboard</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css"></head>
<body class="admin-body"><div class="admin-shell"><%@ include file="/WEB-INF/views/admin-nav.jsp" %><main class="admin-main">
<div class="admin-head"><div><h1>Tổng quan hệ thống</h1><p>Dashboard giám sát website dành riêng cho ADMIN.</p></div><span class="admin-badge">ADMIN · READ-ONLY</span></div>
<div class="kpis">
<div class="kpi"><div class="kpi-label">Tổng số người dùng</div><div class="kpi-value">${overview.users}</div></div>
<div class="kpi"><div class="kpi-label">Tài liệu</div><div class="kpi-value">${overview.documents}</div></div>
<div class="kpi"><div class="kpi-label">Video</div><div class="kpi-value">${overview.videos}</div></div>
<div class="kpi"><div class="kpi-label">Sách</div><div class="kpi-value">${overview.books}</div></div>
<div class="kpi"><div class="kpi-label">Lượt truy cập</div><div class="kpi-value">${overview.accessHistory}</div></div>
<div class="kpi"><div class="kpi-label">Audit Logs</div><div class="kpi-value">${overview.auditLogs}</div></div>
<div class="kpi"><div class="kpi-label">Permissions</div><div class="kpi-value">${overview.permissions}</div></div>
<div class="kpi"><div class="kpi-label">Permission Requests</div><div class="kpi-value">${overview.permissionRequests}</div></div>
<div class="kpi"><div class="kpi-label">Licenses</div><div class="kpi-value">${overview.licenses}</div></div>
</div>
<div class="grid2"><section class="panel"><h2>Giám sát nhanh</h2><div class="actions"><a class="action" href="${pageContext.request.contextPath}/admin/users">Xem người dùng</a><a class="action" href="${pageContext.request.contextPath}/admin/content">Xem nội dung</a><a class="action" href="${pageContext.request.contextPath}/admin/permissions">Xem phân quyền</a><a class="action" href="${pageContext.request.contextPath}/admin/reports">Xem báo cáo</a><a class="action" href="${pageContext.request.contextPath}/admin/audit-logs">Xem audit logs</a></div></section>
<section class="panel"><h2>Chính sách quyền</h2><div class="readonly">ADMIN chỉ xem thống kê/danh sách. Không có thêm, sửa, xóa, upload, cấp/thu hồi quyền, duyệt request hoặc quản lý nghiệp vụ.</div></section></div>
</main></div></body></html>