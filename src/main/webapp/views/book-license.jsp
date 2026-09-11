<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Bản quyền sách</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/views/_navbar.jsp" %><div class="container">
<div class="card">
<div class="flex-between"><div><h1>©️ Bản quyền sách</h1><p class="text-muted">${isViewer ? 'Viewer / Read-only Admin chỉ được xem giấy phép và thời hạn bản quyền.' : 'Theo dõi giấy phép và thời hạn bản quyền của sách.'}</p></div>
<a href="${pageContext.request.contextPath}/book-license" class="btn btn-secondary">Làm mới</a></div>
<c:if test="${not isAuditor}">
<h2 class="mt-3">${empty editLicense ? "Thêm bản quyền sách" : "Cập nhật bản quyền sách"}</h2>
<form action="${pageContext.request.contextPath}/book-license/save" method="post">
<input type="hidden" name="licenseId" value="${editLicense.licenseId}">
<div class="form-group"><label>Sách</label><select name="bookId" class="form-control" required><c:forEach var="b" items="${books}"><option value="${b.bookId}" ${b.bookId == editLicense.bookId ? 'selected' : ''}>${b.title}</option></c:forEach></select></div>
<div class="form-group"><label>Loại</label><select name="licenseType" class="form-control"><option value="FREE" ${empty editLicense or editLicense.licenseType == 'FREE' ? 'selected' : ''}>FREE</option><option value="SUBSCRIPTION" ${editLicense.licenseType == 'SUBSCRIPTION' ? 'selected' : ''}>SUBSCRIPTION</option><option value="PAID" ${editLicense.licenseType == 'PAID' ? 'selected' : ''}>PAID</option><option value="INTERNAL" ${editLicense.licenseType == 'INTERNAL' ? 'selected' : ''}>INTERNAL</option></select></div>
<div class="form-group"><label>Mã bản quyền</label><input name="licenseCode" class="form-control" value="${editLicense.licenseCode}"></div>
<div class="form-group"><label>Ngày cấp</label><input type="date" name="issuedDate" class="form-control" value="${editLicense.issuedDate}"></div>
<div class="form-group"><label>Ngày hết hạn</label><input type="date" name="expiryDate" class="form-control" value="${editLicense.expiryDate}"></div>
<div class="form-group"><label>Điều khoản</label><textarea name="terms" class="form-control" rows="3">${editLicense.terms}</textarea></div>
<div class="form-group"><label>Trạng thái</label><select name="status" class="form-control"><option value="VALID">VALID</option><option value="EXPIRED">EXPIRED</option><option value="REVOKED">REVOKED</option></select></div>
<button class="btn btn-primary">Lưu</button> <a class="btn btn-secondary" href="${pageContext.request.contextPath}/book-license">Hủy</a>
</form></c:if>
</div>
<div class="card"><h2>Danh sách bản quyền sách</h2><c:choose><c:when test="${empty licenses}"><p class="text-muted">Chưa có bản quyền sách nào.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Sách</th><th>Loại</th><th>Mã</th><th>Ngày cấp</th><th>Hết hạn</th><th>Trạng thái</th><th></th></tr></thead><tbody>
<c:forEach var="l" items="${licenses}"><tr><td>${l.bookTitle}</td><td>${l.licenseType}</td><td>${l.licenseCode}</td><td>${l.issuedDate}</td><td>${empty l.expiryDate ? 'Không giới hạn' : l.expiryDate}</td><td>${l.status}</td><td><c:if test="${not isAuditor}"><a href="${pageContext.request.contextPath}/book-license/edit?id=${l.licenseId}">Sửa</a> | <a href="${pageContext.request.contextPath}/book-license/delete?id=${l.licenseId}" onclick="return confirm('Xóa bản quyền này?');" style="color:#dc2626">Xóa</a></c:if></td></tr></c:forEach>
</tbody></table></div></c:otherwise></c:choose></div></div></body></html>
