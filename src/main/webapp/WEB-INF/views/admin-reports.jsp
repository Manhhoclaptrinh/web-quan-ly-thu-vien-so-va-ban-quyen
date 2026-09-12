<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Admin - Báo cáo</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css?v=20260912-admin">
</head>
<body class="admin-body">
<div class="admin-shell">
  <%@ include file="/WEB-INF/views/admin-nav.jsp" %>
  <main class="admin-main">
    <header class="admin-head">
      <div>
        <div class="eyebrow">SYSTEM ANALYTICS</div>
        <h1>Báo cáo hệ thống</h1>
        <p>Theo dõi truy cập, hoạt động và tình trạng vận hành.</p>
      </div>
    </header>

    <div class="report-grid">
      <div class="report-card"><span>👁️</span><b>${overview.accessHistory}</b><small>Lượt truy cập</small></div>
      <div class="report-card"><span>📋</span><b>${overview.auditLogs}</b><small>Audit logs</small></div>
      <div class="report-card"><span>🚨</span><b>${overview.failedLogins}</b><small>Login lỗi / 24h</small></div>
    </div>

    <section class="panel">
      <div class="panel-title"><h2>Lượt truy cập theo ngày</h2><span class="muted">7 ngày gần nhất</span></div>
      <div class="big-bars">
        <c:forEach var="x" items="${accessChart}">
          <div class="big-bar-col">
            <b>${x.total}</b>
            <div class="big-bar" style="height:${x.total*12+12}px"></div>
            <small>${x.day}</small>
          </div>
        </c:forEach>
      </div>
    </section>

    <section class="panel">
      <h2>Phạm vi quản trị</h2>
      <div class="recommendations">
        <div>📊 <b>Giám sát</b><span>Theo dõi số liệu vận hành và lưu lượng truy cập.</span></div>
        <div>📋 <b>Audit</b><span>Kiểm tra lịch sử thao tác quản trị và sự kiện hệ thống.</span></div>
        <div>🛡️ <b>Bảo mật</b><span>Phát hiện và theo dõi các đăng nhập bất thường.</span></div>
      </div>
    </section>
  </main>
</div>
</body>
</html>
