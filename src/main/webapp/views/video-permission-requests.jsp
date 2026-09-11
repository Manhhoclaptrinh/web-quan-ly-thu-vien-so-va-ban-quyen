<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Yêu cầu cấp quyền video</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/views/_navbar.jsp" %><div class="container">
<div class="card"><h1>Yêu cầu cấp quyền truy cập video</h1>
<c:if test="${param.success == 'created'}"><div class="alert alert-success">Đã gửi yêu cầu. Vui lòng chờ ADMIN/LIBRARIAN duyệt.</div></c:if>
<c:if test="${param.error == 'pending'}"><div class="alert alert-error">Bạn đã có một yêu cầu đang chờ xử lý cho quyền này.</div></c:if>
<c:if test="${param.error == 'already-granted'}"><div class="alert alert-success">Bạn đã được cấp quyền này cho video.</div></c:if>
<c:if test="${param.error == 'processed'}"><div class="alert alert-error">Yêu cầu này đã được xử lý trước đó.</div></c:if>
<c:if test="${param.error == 'no-license'}"><div class="alert alert-error">Video chưa có bản quyền đang hiệu lực nên chưa thể duyệt.</div></c:if>
<c:choose><c:when test="${sessionScope.currentUser.role == 'ADMIN' or sessionScope.currentUser.role == 'LIBRARIAN'}"><p class="text-muted">Duyệt hoặc từ chối yêu cầu của READER.</p></c:when><c:when test="${isViewer}"><p class="text-muted">Viewer / Read-only Admin chỉ xem toàn bộ yêu cầu, không được tạo, duyệt hoặc từ chối.</p></c:when><c:otherwise><p class="text-muted">Gửi yêu cầu xem/tải video và theo dõi trạng thái xử lý.</p>
<form action="${pageContext.request.contextPath}/video-permission-request/create" method="post" class="mt-3">
<div class="form-group"><label>Video</label><select name="videoId" class="form-control" required><c:forEach var="v" items="${videos}"><c:if test="${v.status == 'AVAILABLE'}"><option value="${v.videoId}" ${param.videoId == v.videoId ? 'selected' : ''}>${v.title} (${v.accessLevel})</option></c:if></c:forEach></select></div>
<div class="form-group"><label>Quyền muốn xin</label><select name="permissionType" class="form-control"><option value="VIEW" ${empty param.permissionType or param.permissionType == 'VIEW' ? 'selected' : ''}>VIEW - Xem</option><option value="DOWNLOAD" ${param.permissionType == 'DOWNLOAD' ? 'selected' : ''}>DOWNLOAD - Tải xuống</option></select></div>
<div class="form-group"><label>Lý do</label><textarea name="reason" class="form-control" rows="3" maxlength="500" placeholder="Ví dụ: Phục vụ học tập môn Công nghệ Java"></textarea></div>
<button class="btn btn-primary" type="submit">Gửi yêu cầu</button></form></c:otherwise></c:choose></div>
<div class="card"><c:choose><c:when test="${empty requests}"><p class="text-muted">Chưa có yêu cầu nào.</p></c:when><c:otherwise><table><thead><tr><th>Người dùng</th><th>Video</th><th>Quyền</th><th>Lý do</th><th>Trạng thái</th><th>Ngày gửi</th><th>Xử lý</th></tr></thead><tbody>
<c:forEach var="r" items="${requests}"><tr><td>${r.userFullName}<br><span class="text-muted">${r.username}</span></td><td>${r.videoTitle}</td><td>${r.permissionType}</td><td>${r.reason}</td><td><c:choose><c:when test="${r.status == 'PENDING'}"><span class="badge badge-restricted">PENDING</span></c:when><c:when test="${r.status == 'APPROVED'}"><span class="badge badge-valid">APPROVED</span></c:when><c:otherwise><span class="badge badge-expired">REJECTED</span></c:otherwise></c:choose></td><td>${r.requestedDate}</td><td>
<c:if test="${(sessionScope.currentUser.role == 'ADMIN' or sessionScope.currentUser.role == 'LIBRARIAN') and r.status == 'PENDING'}">
<form action="${pageContext.request.contextPath}/video-permission-request/approve" method="post" style="margin-bottom:6px"><input type="hidden" name="id" value="${r.requestId}"><input type="datetime-local" name="expiryDate" class="form-control" style="max-width:210px" title="Bỏ trống nếu không giới hạn"><button class="btn btn-primary" type="submit">Duyệt</button></form>
<form action="${pageContext.request.contextPath}/video-permission-request/reject" method="post"><input type="hidden" name="id" value="${r.requestId}"><button class="btn btn-secondary" type="submit" onclick="return confirm('Từ chối yêu cầu này?');">Từ chối</button></form>
</c:if><c:if test="${r.status != 'PENDING'}"><span class="text-muted">${r.processedByName}</span></c:if>
</td></tr></c:forEach></tbody></table></c:otherwise></c:choose></div></div></body></html>
