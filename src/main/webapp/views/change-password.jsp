<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Đổi mật khẩu</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/views/_navbar.jsp" %><div class="container"><div class="card" style="max-width:650px;margin:auto"><h1>Đổi mật khẩu</h1>
<c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
<form action="${pageContext.request.contextPath}/profile/change-password" method="post"><div class="form-group"><label>Mật khẩu hiện tại *</label><input class="form-control" type="password" name="currentPassword" required></div><div class="form-group"><label>Mật khẩu mới *</label><input class="form-control" type="password" name="newPassword" minlength="4" required></div><div class="form-group"><label>Xác nhận mật khẩu mới *</label><input class="form-control" type="password" name="confirmPassword" minlength="4" required></div><button class="btn btn-primary">Đổi mật khẩu</button> <a class="btn btn-secondary" href="${pageContext.request.contextPath}/profile">Hủy</a></form>
</div></div></body></html>
