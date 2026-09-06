<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Thông báo</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/views/_navbar.jsp" %><div class="container"><div class="card">
<div class="flex-between"><div><h1>🔔 Thông báo</h1><p class="text-muted">Các cập nhật về quyền truy cập và hệ thống.</p></div><a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/notifications/read-all">Đánh dấu đã đọc tất cả</a></div>
<c:choose><c:when test="${empty notifications}"><div class="empty-state"><strong>Không có thông báo</strong></div></c:when><c:otherwise>
<div class="notification-list"><c:forEach var="n" items="${notifications}"><div class="notification-item ${n.read ? '' : 'unread'}"><div><strong>${n.title}</strong><p>${n.message}</p><small>${n.createdAt} · ${n.type}</small></div><c:if test="${!n.read}"><a class="btn btn-sm btn-secondary" href="${pageContext.request.contextPath}/notifications/read?id=${n.notificationId}">Đã đọc</a></c:if></div></c:forEach></div>
</c:otherwise></c:choose></div></div></body></html>