<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<aside class="admin-side">
  <div class="admin-brand">ADMIN PANEL</div>
  <div class="admin-sub">System Administration</div>

  <div class="admin-user">
    <strong>${sessionScope.currentUser.fullName}</strong>
    <div class="admin-role">${sessionScope.currentUser.username} · ADMIN</div>
  </div>

  <nav class="admin-nav">
    <a class="${fn:endsWith(pageContext.request.requestURI,'/admin') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/admin">🏠 Tổng quan</a>
    <a class="${fn:contains(pageContext.request.requestURI,'/admin/reports') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/admin/reports">📊 Báo cáo</a>
    <a class="${fn:contains(pageContext.request.requestURI,'/admin/audit-logs') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/admin/audit-logs">📋 Audit Logs</a>
    <a class="${fn:contains(pageContext.request.requestURI,'/admin/security') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/admin/security">🛡️ Bảo mật</a>
    <a class="${fn:contains(pageContext.request.requestURI,'/admin/notifications') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/admin/notifications">🔔 Thông báo</a>
    <a class="${fn:contains(pageContext.request.requestURI,'/admin/settings') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/admin/settings">⚙️ Cài đặt</a>
  </nav>

  <div class="admin-footer">
    <a class="logout" href="${pageContext.request.contextPath}/logout">🚪 Đăng xuất</a>
  </div>
</aside>
