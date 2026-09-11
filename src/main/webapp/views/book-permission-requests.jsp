<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Yêu cầu quyền sách</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/views/_navbar.jsp" %><div class="container">
<div class="card"><h1>📥 Yêu cầu cấp quyền sách</h1>
<c:if test="${param.success == 'created'}"><div class="alert alert-success">Đã gửi yêu cầu.</div></c:if>
<c:if test="${param.error == 'pending'}"><div class="alert alert-error">Bạn đã có yêu cầu đang chờ xử lý.</div></c:if>
<c:if test="${param.error == 'already-granted'}"><div class="alert alert-success">Bạn đã được cấp quyền này.</div></c:if>
<c:if test="${param.error == 'processed'}"><div class="alert alert-error">Yêu cầu đã được xử lý.</div></c:if>
<c:if test="${param.error == 'no-license'}"><div class="alert alert-error">Sách chưa có bản quyền hợp lệ.</div></c:if>
<c:choose>
<c:when test="${sessionScope.currentUser.role == 'ADMIN' or sessionScope.currentUser.role == 'LIBRARIAN'}"><p class="text-muted">Duyệt hoặc từ chối yêu cầu của READER.</p></c:when>
<c:when test="${isViewer}"><p class="text-muted">Viewer / Read-only Admin chỉ xem danh sách yêu cầu, không được tạo hoặc xử lý yêu cầu.</p></c:when>
<c:otherwise>
<p class="text-muted">Gửi yêu cầu xem/tải sách và theo dõi trạng thái.</p>
<form action="${pageContext.request.contextPath}/book-permission-request/create" method="post" class="mt-3">
<div class="form-group"><label>Sách</label><select name="bookId" class="form-control" required><c:forEach var="b" items="${books}"><c:if test="${b.status == 'AVAILABLE'}"><option value="${b.bookId}" ${param.bookId == b.bookId ? 'selected' : ''}>${b.title} (${b.accessLevel})</option></c:if></c:forEach></select></div>
<div class="form-group"><label>Quyền</label><select name="permissionType" class="form-control"><option value="VIEW">VIEW - Xem</option><option value="DOWNLOAD">DOWNLOAD - Tải</option></select></div>
<div class="form-group"><label>Lý do</label><textarea name="reason" class="form-control" rows="3" maxlength="500"></textarea></div>
<button class="btn btn-primary">Gửi yêu cầu</button></form>
</c:otherwise></c:choose></div>
<div class="card"><h2>Danh sách yêu cầu</h2><c:choose><c:when test="${empty requests}"><p class="text-muted">Chưa có yêu cầu nào.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Người dùng</th><th>Sách</th><th>Quyền</th><th>Lý do</th><th>Trạng thái</th><th>Ngày gửi</th><th>Xử lý</th></tr></thead><tbody>
<c:forEach var="r" items="${requests}"><tr><td>${r.userFullName}<br><span class="text-muted">${r.username}</span></td><td>${r.bookTitle}</td><td>${r.permissionType}</td><td>${r.reason}</td><td>${r.status}</td><td>${r.requestedDate}</td><td>
<c:if test="${(sessionScope.currentUser.role == 'ADMIN' or sessionScope.currentUser.role == 'LIBRARIAN') and r.status == 'PENDING'}">
<form action="${pageContext.request.contextPath}/book-permission-request/approve" method="post" style="margin-bottom:6px"><input type="hidden" name="id" value="${r.requestId}"><input type="datetime-local" name="expiryDate" class="form-control" style="max-width:210px"><button class="btn btn-primary">Duyệt</button></form>
<form action="${pageContext.request.contextPath}/book-permission-request/reject" method="post"><input type="hidden" name="id" value="${r.requestId}"><button class="btn btn-secondary" onclick="return confirm('Từ chối yêu cầu này?');">Từ chối</button></form>
</c:if>
<c:if test="${r.status != 'PENDING'}"><span class="text-muted">${r.processedByName}</span></c:if>
</td></tr></c:forEach>
</tbody></table></div></c:otherwise></c:choose></div></div></body></html>
