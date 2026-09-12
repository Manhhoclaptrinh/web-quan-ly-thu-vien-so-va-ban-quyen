<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Admin Dashboard</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css?v=20260912-admin">
</head>
<body class="admin-body">
<div class="admin-shell">
  <%@ include file="/WEB-INF/views/admin-nav.jsp" %>

  <main class="admin-main">
    <header class="admin-head">
      <div>
        <div class="eyebrow">SYSTEM OVERVIEW</div>
        <h1>Tổng quan hệ thống</h1>
        <p>Trung tâm giám sát và quản trị kỹ thuật của thư viện số.</p>
      </div>
      <div class="admin-head-right">
        <span class="admin-badge">● ADMIN</span>
        <span class="date-badge">Hệ thống đang hoạt động</span>
      </div>
    </header>

    <c:set var="kpi" value="${overview}"/>
    <div class="kpis">
      <div class="kpi"><span class="kpi-icon">👁️</span><div><div class="kpi-label">Lượt truy cập</div><div class="kpi-value">${kpi.accessHistory}</div><small>Tổng lượt ghi nhận</small></div></div>
      <div class="kpi"><span class="kpi-icon">📋</span><div><div class="kpi-label">Audit Logs</div><div class="kpi-value">${kpi.auditLogs}</div><small>Lịch sử hoạt động hệ thống</small></div></div>
      <div class="kpi"><span class="kpi-icon">🚨</span><div><div class="kpi-label">Login lỗi / 24h</div><div class="kpi-value">${kpi.failedLogins}</div><small>Sự kiện cần theo dõi</small></div></div>
    </div>

    <div class="grid2">
      <section class="panel">
        <div class="panel-title">
          <h2>Lượt truy cập 7 ngày</h2>
          <a href="${pageContext.request.contextPath}/admin/reports">Chi tiết →</a>
        </div>
        <c:set var="chartMax" value="1"/>
        <c:forEach var="x" items="${accessChart}">
          <c:if test="${x.total gt chartMax}"><c:set var="chartMax" value="${x.total}"/></c:if>
        </c:forEach>
        <div class="bar-chart">
          <c:forEach var="x" items="${accessChart}">
            <div class="bar-col">
              <div class="bar" style="height:${x.total == 0 ? 6 : (x.total * 160 / chartMax + 10)}px" title="${x.total} lượt"></div>
              <small>${x.day}</small>
              <b>${x.total}</b>
            </div>
          </c:forEach>
        </div>
      </section>

      <section class="panel">
        <div class="panel-title">
          <h2>Trung tâm điều hành</h2>
          <a href="${pageContext.request.contextPath}/admin/security">Bảo mật →</a>
        </div>
        <div class="todo">
          <a href="${pageContext.request.contextPath}/admin/security">
            <b>🚨 ${kpi.failedLogins}</b>
            <span>Sự kiện đăng nhập lỗi trong 24 giờ</span>
          </a>
          <a href="${pageContext.request.contextPath}/admin/audit-logs">
            <b>📋 ${kpi.auditLogs}</b>
            <span>Bản ghi audit đang được lưu</span>
          </a>
          <a href="${pageContext.request.contextPath}/admin/settings">
            <b>⚙️ Cấu hình</b>
            <span>Thiết lập hệ thống và bảo mật phiên</span>
          </a>
        </div>
      </section>
    </div>

    <section class="panel">
      <div class="panel-title"><h2>Truy cập nhanh</h2></div>
      <div class="actions">
        <a class="action primary" href="${pageContext.request.contextPath}/admin/reports">📊 Báo cáo hệ thống</a>
        <a class="action" href="${pageContext.request.contextPath}/admin/audit-logs">📋 Xem audit logs</a>
        <a class="action" href="${pageContext.request.contextPath}/admin/security">🛡️ Trung tâm bảo mật</a>
        <a class="action" href="${pageContext.request.contextPath}/admin/notifications">🔔 Thông báo</a>
        <a class="action" href="${pageContext.request.contextPath}/admin/settings">⚙️ Cài đặt hệ thống</a>
      </div>
    </section>
  </main>
</div>
</body>
</html>
