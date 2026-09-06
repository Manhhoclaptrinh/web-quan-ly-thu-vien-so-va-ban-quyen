<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Hồ sơ cá nhân</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/views/_navbar.jsp" %><div class="container"><div class="card" style="max-width:760px;margin:auto">
<h1>Hồ sơ cá nhân</h1><c:if test="${param.success == 'updated'}"><div class="alert alert-success">Cập nhật hồ sơ thành công.</div></c:if><c:if test="${param.success == 'password'}"><div class="alert alert-success">Đổi mật khẩu thành công.</div></c:if>
<table><tr><th>Username</th><td>${profile.username}</td></tr><tr><th>Họ và tên</th><td>${profile.fullName}</td></tr><tr><th>Email</th><td>${profile.email}</td></tr><tr><th>Vai trò</th><td>${profile.role}</td></tr><tr><th>Trạng thái</th><td>${profile.status}</td></tr><tr><th>Ngày tạo</th><td>${profile.createdAt}</td></tr></table>
<div class="mt-3"><a class="btn btn-primary" href="${pageContext.request.contextPath}/profile/edit">Chỉnh sửa hồ sơ</a> <a class="btn btn-secondary" href="${pageContext.request.contextPath}/profile/change-password">Đổi mật khẩu</a></div>
</div></div></body></html>
