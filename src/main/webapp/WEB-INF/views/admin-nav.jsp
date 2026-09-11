<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="adminPath" value="${pageContext.request.requestURI}"/>
<aside class="admin-side">
  <div class="admin-brand">ADMIN PANEL</div>
  <div class="admin-sub">System monitoring · Read-only</div>
  <div class="admin-user">
    <strong>${sessionScope.currentUser.fullName}</strong>
    <div class="admin-role">${sessionScope.currentUser.username} · ADMIN</div>
  </div>
  <nav class="admin-nav">
    <a class="${fn:endsWith(adminPath,'/admin') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin">📊 Tổng quan</a>
    <a class="${fn:contains(adminPath,'/admin/users') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/users">👥 Người dùng</a>
    <a class="${fn:contains(adminPath,'/admin/content') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/content">📚 Nội dung</a>
    <a class="${fn:contains(adminPath,'/admin/permissions') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/permissions">🔐 Phân quyền</a>
    <a class="${fn:contains(adminPath,'/admin/reports') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/reports">📈 Báo cáo</a>
    <a class="${fn:contains(adminPath,'/admin/audit-logs') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/audit-logs">📋 Audit Logs</a>
  </nav>
  <div class="admin-footer">
    <a class="admin-nav action" style="display:block;color:#d1d5db;border-color:#374151;background:transparent" href="${pageContext.request.contextPath}/logout">🚪 Đăng xuất</a>
  </div>
</aside>
