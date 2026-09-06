<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>${empty user ? 'Thêm người dùng' : 'Cập nhật người dùng'}</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/views/_navbar.jsp" %><div class="container"><div class="card" style="max-width:700px;margin:auto"><h1>${empty user ? 'Thêm người dùng' : 'Cập nhật người dùng'}</h1>
<c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
<form action="${pageContext.request.contextPath}/users/save" method="post"><c:if test="${not empty user}"><input type="hidden" name="userId" value="${user.userId}"></c:if>
<div class="form-group"><label>Tên đăng nhập *</label><input class="form-control" name="username" required value="${user.username}" ${not empty user ? 'readonly' : ''}></div>
<div class="form-group"><label>Mật khẩu ${empty user ? '*' : '(để trống nếu không đổi)'}</label><input class="form-control" type="password" name="password" ${empty user ? 'required' : ''} minlength="4"></div>
<div class="form-group"><label>Họ và tên *</label><input class="form-control" name="fullName" required value="${user.fullName}"></div>
<div class="form-group"><label>Email</label><input class="form-control" type="email" name="email" value="${user.email}"></div>
<div class="form-group"><label>Vai trò</label><select class="form-control" name="role"><option value="ADMIN" ${user.role == 'ADMIN' ? 'selected' : ''}>ADMIN</option><option value="LIBRARIAN" ${user.role == 'LIBRARIAN' ? 'selected' : ''}>LIBRARIAN</option><option value="READER" ${empty user.role or user.role == 'READER' ? 'selected' : ''}>READER</option></select></div>
<div class="form-group"><label>Trạng thái</label><select class="form-control" name="status"><option value="ACTIVE" ${empty user.status or user.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE - Hoạt động</option><option value="LOCKED" ${user.status == 'LOCKED' ? 'selected' : ''}>LOCKED - Khóa</option></select></div>
<button class="btn btn-primary" type="submit">Lưu</button> <a class="btn btn-secondary" href="${pageContext.request.contextPath}/users">Hủy</a></form></div></div></body></html>
