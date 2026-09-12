<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Admin - Cài đặt</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css?v=20260912-admin"></head>
<body class="admin-body"><div class="admin-shell"><%@ include file="/WEB-INF/views/admin-nav.jsp" %><main class="admin-main">
<header class="admin-head"><div><div class="eyebrow">SYSTEM CONFIGURATION</div><h1>Cài đặt hệ thống</h1><p>Thiết lập cấu hình vận hành và bảo mật hệ thống.</p></div></header>
<form method="post" action="${pageContext.request.contextPath}/admin" class="settings-form"><input type="hidden" name="action" value="settings-save">
<section class="panel"><h2>🏛️ Thông tin thư viện</h2><div class="form-grid"><label>Tên thư viện<input name="libraryName" value="${settings.libraryName}" placeholder="CNJ25 Digital Library"></label><label>Mô tả<input name="libraryDescription" value="${settings.libraryDescription}"></label><label>Dung lượng upload tối đa (MB)<input type="number" name="maxUploadMb" min="1" value="${empty settings.maxUploadMb?50:settings.maxUploadMb}"></label></div></section>

<section class="panel"><h2>🛡️ Bảo mật</h2><div class="form-grid"><label>Session timeout (phút)<input type="number" name="sessionTimeout" min="5" value="${empty settings.sessionTimeout?30:settings.sessionTimeout}"></label></div></section>
<button class="btn primary">💾 Lưu cài đặt</button></form>
<c:if test="${param.success=='1'}"><div class="toast">✓ Đã lưu cài đặt hệ thống.</div></c:if>
</main></div></body></html>
