<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Phân quyền sách</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/views/_navbar.jsp" %><div class="container">
<c:if test="${not isAuditor}"><div class="card"><h1>🔐 Cấp quyền sách</h1><form action="${pageContext.request.contextPath}/book-permission/save" method="post">
<div class="form-group"><label>Người dùng</label><select name="userId" class="form-control" required><c:forEach var="u" items="${users}"><option value="${u.userId}">${u.fullName} (${u.username})</option></c:forEach></select></div>
<div class="form-group"><label>Sách</label><select name="bookId" class="form-control" required><c:forEach var="b" items="${books}"><option value="${b.bookId}">${b.title}</option></c:forEach></select></div>
<div class="form-group"><label>Loại quyền</label><select name="permissionType" class="form-control"><option value="VIEW">VIEW - Xem</option><option value="DOWNLOAD">DOWNLOAD - Tải</option><option value="EDIT">EDIT - Chỉnh sửa</option></select></div>
<div class="form-group"><label>Hết hạn</label><input type="datetime-local" name="expiryDate" class="form-control"></div>
<button class="btn btn-primary">Cấp quyền</button></form></div></c:if>
<div class="card"><h2>Danh sách quyền sách</h2><c:choose><c:when test="${empty permissions}"><p class="text-muted">Chưa có quyền sách nào.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Người dùng</th><th>Sách</th><th>Quyền</th><th>Cấp bởi</th><th>Ngày cấp</th><th>Hết hạn</th><th></th></tr></thead><tbody>
<c:forEach var="p" items="${permissions}"><tr><td>${p.username}</td><td>${p.bookTitle}</td><td>${p.permissionType}</td><td>${p.grantedByName}</td><td>${p.grantedDate}</td><td>${empty p.expiryDate ? 'Không giới hạn' : p.expiryDate}</td><td><c:if test="${not isAuditor}"><a href="${pageContext.request.contextPath}/book-permission/delete?id=${p.permissionId}" onclick="return confirm('Thu hồi quyền sách này?');" style="color:#dc2626">Thu hồi</a></c:if></td></tr></c:forEach>
</tbody></table></div></c:otherwise></c:choose></div></div></body></html>
