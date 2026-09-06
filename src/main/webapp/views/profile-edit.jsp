<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Chỉnh sửa hồ sơ</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/views/_navbar.jsp" %><div class="container"><div class="card" style="max-width:650px;margin:auto"><h1>Chỉnh sửa hồ sơ</h1>
<c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
<form action="${pageContext.request.contextPath}/profile/save" method="post"><div class="form-group"><label>Username</label><input class="form-control" value="${profile.username}" readonly></div><div class="form-group"><label>Họ và tên *</label><input class="form-control" name="fullName" value="${profile.fullName}" required></div><div class="form-group"><label>Email</label><input class="form-control" type="email" name="email" value="${profile.email}"></div><button class="btn btn-primary">Lưu thay đổi</button> <a class="btn btn-secondary" href="${pageContext.request.contextPath}/profile">Hủy</a></form>
</div></div></body></html>
