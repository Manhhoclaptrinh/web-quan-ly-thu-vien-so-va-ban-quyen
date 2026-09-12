<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Admin - Thông báo</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css?v=20260912-admin">
</head>
<body class="admin-body">
<div class="admin-shell">
  <%@ include file="/WEB-INF/views/admin-nav.jsp" %>
  <main class="admin-main">
    <header class="admin-head">
      <div>
        <div class="eyebrow">SYSTEM NOTIFICATIONS</div>
        <h1>Thông báo quản trị</h1>
        <p>Các cảnh báo kỹ thuật và bảo mật cần ADMIN theo dõi.</p>
      </div>
    </header>

    <div class="todo-panel panel">
      <a href="${pageContext.request.contextPath}/admin/security">
        <b>🚨 ${overview.failedLogins}</b>
        <span>Đăng nhập lỗi trong 24 giờ</span>
        <strong>→</strong>
      </a>
      <a href="${pageContext.request.contextPath}/admin/audit-logs">
        <b>📋 ${overview.auditLogs}</b>
        <span>Bản ghi audit hiện có</span>
        <strong>→</strong>
      </a>
      <a href="${pageContext.request.contextPath}/admin/settings">
        <b>⚙️</b>
        <span>Kiểm tra cấu hình hệ thống</span>
        <strong>→</strong>
      </a>
    </div>

    <section class="panel">
      <h2>Sự kiện bảo mật gần đây</h2>
      <c:forEach var="e" items="${events}">
        <div class="notification-row">
          <span>🚨</span>
          <div><b>${e.action} — ${e.username}</b><small>${e.description} · ${e.ip}</small></div>
          <time>${e.createdAt}</time>
        </div>
      </c:forEach>
      <c:if test="${empty events}"><div class="empty">Không có cảnh báo mới.</div></c:if>
    </section>
  </main>
</div>
</body>
</html>
