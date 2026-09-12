<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Admin Dashboard</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css?v=20260912"></head>
<body class="admin-body"><div class="admin-shell"><%@ include file="/WEB-INF/views/admin-nav.jsp" %><main class="admin-main">
<header class="admin-head"><div><div class="eyebrow">SYSTEM OVERVIEW</div><h1>Tổng quan hệ thống</h1><p>Trung tâm điều hành thư viện số và bản quyền truy cập.</p></div><div class="admin-head-right"><span class="admin-badge">● ADMIN</span><span class="date-badge">Hệ thống đang hoạt động</span></div></header>
<div class="kpis">
<c:set var="kpi" value="${overview}"/>
<div class="kpi"><span class="kpi-icon">👥</span><div><div class="kpi-label">Người dùng</div><div class="kpi-value">${kpi.users}</div><small>${kpi.activeUsers} đang hoạt động</small></div></div>
<div class="kpi"><span class="kpi-icon">📄</span><div><div class="kpi-label">Tài liệu</div><div class="kpi-value">${kpi.documents}</div></div></div>
<div class="kpi"><span class="kpi-icon">🎬</span><div><div class="kpi-label">Video</div><div class="kpi-value">${kpi.videos}</div></div></div>
<div class="kpi"><span class="kpi-icon">📚</span><div><div class="kpi-label">Sách</div><div class="kpi-value">${kpi.books}</div></div></div>
<div class="kpi"><span class="kpi-icon">👁️</span><div><div class="kpi-label">Lượt truy cập</div><div class="kpi-value">${kpi.accessHistory}</div></div></div>
<div class="kpi"><span class="kpi-icon">🔐</span><div><div class="kpi-label">Permissions</div><div class="kpi-value">${kpi.permissions}</div></div></div>
<div class="kpi warn"><span class="kpi-icon">📩</span><div><div class="kpi-label">Request chờ duyệt</div><div class="kpi-value">${kpi.permissionRequests}</div></div></div>
<div class="kpi warn"><span class="kpi-icon">📜</span><div><div class="kpi-label">License sắp hết hạn</div><div class="kpi-value">${kpi.expiringLicenses}</div><small>trong 7 ngày</small></div></div>
<div class="kpi danger"><span class="kpi-icon">🚨</span><div><div class="kpi-label">Login lỗi / 24h</div><div class="kpi-value">${kpi.failedLogins}</div></div></div>
</div>
<div class="grid2">
<section class="panel"><div class="panel-title"><h2>Lượt truy cập 7 ngày</h2><a href="${pageContext.request.contextPath}/admin/reports">Chi tiết →</a></div>
<c:set var="chartMax" value="1"/><c:forEach var="x" items="${accessChart}"><c:if test="${x.total gt chartMax}"><c:set var="chartMax" value="${x.total}"/></c:if></c:forEach>
<div class="bar-chart"><c:forEach var="x" items="${accessChart}"><div class="bar-col"><div class="bar" style="height:${x.total == 0 ? 6 : (x.total * 160 / chartMax + 10)}px" title="${x.total} lượt"></div><small>${x.day}</small><b>${x.total}</b></div></c:forEach></div></section>
<section class="panel"><div class="panel-title"><h2>Cần xử lý</h2><a href="${pageContext.request.contextPath}/admin/permissions">Mở trung tâm quyền →</a></div>
<div class="todo"><a href="${pageContext.request.contextPath}/admin/permissions"><b>📩 ${kpi.permissionRequests}</b><span>Permission requests đang chờ</span></a>
<a href="${pageContext.request.contextPath}/admin/permissions"><b>📜 ${kpi.expiringLicenses}</b><span>License hết hạn trong 7 ngày</span></a>
<a href="${pageContext.request.contextPath}/admin/security"><b>🚨 ${kpi.failedLogins}</b><span>Sự kiện đăng nhập lỗi trong 24h</span></a></div></section>
</div>
<section class="panel"><div class="panel-title"><h2>Giám sát nhanh</h2></div><div class="actions">
<a class="action primary" href="${pageContext.request.contextPath}/admin/content/form?type=DOCUMENT">＋ Thêm tài liệu</a>
<a class="action primary" href="${pageContext.request.contextPath}/admin/content/form?type=BOOK">＋ Thêm sách</a>
<a class="action primary" href="${pageContext.request.contextPath}/admin/content/form?type=VIDEO">＋ Thêm video</a>
<a class="action" href="${pageContext.request.contextPath}/admin/users">Quản lý người dùng</a><a class="action" href="${pageContext.request.contextPath}/admin/permissions">Quản lý quyền</a><a class="action" href="${pageContext.request.contextPath}/admin/audit-logs">Xem audit logs</a></div></section>
</main></div></body></html>
