<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Audit Log</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/views/_navbar.jsp" %><div class="container"><div class="card">
<h1>📝 Audit Log</h1><p class="text-muted">Theo dõi các thao tác quản trị quan trọng. Tối đa 200 bản ghi gần nhất.</p>
<c:choose><c:when test="${empty auditLogs}"><p class="text-muted mt-3">Chưa có audit log.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Thời gian</th><th>Người thực hiện</th><th>Hành động</th><th>Đối tượng</th><th>Mô tả</th><th>IP</th></tr></thead><tbody>
<c:forEach var="a" items="${auditLogs}"><tr><td>${a.createdAt}</td><td>${a.username}</td><td><span class="badge badge-audit">${a.actionType}</span></td><td>${a.targetType}${not empty a.targetId ? ' #' : ''}${a.targetId}</td><td>${a.description}</td><td>${a.ipAddress}</td></tr></c:forEach>
</tbody></table></div></c:otherwise></c:choose></div></div></body></html>