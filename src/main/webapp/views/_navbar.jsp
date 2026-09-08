<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div class="navbar">
    <div class="brand">📚 Library</div>
    <nav>
        <a href="${pageContext.request.contextPath}/home">Dashboard</a>
        <a href="${pageContext.request.contextPath}/documents">Tài liệu</a>
        <a href="${pageContext.request.contextPath}/videos">🎬 Video</a>
        <c:if test="${sessionScope.currentUser.role == 'ADMIN'}">
            <a href="${pageContext.request.contextPath}/users">Người dùng</a>
        </c:if>
        <c:if test="${sessionScope.currentUser.role == 'ADMIN' or sessionScope.currentUser.role == 'LIBRARIAN'}">
            <a href="${pageContext.request.contextPath}/categories">Danh mục</a>
        </c:if>
        <c:if test="${sessionScope.currentUser.role == 'ADMIN' or sessionScope.currentUser.role == 'LIBRARIAN'}">
            <a href="${pageContext.request.contextPath}/license">Bản quyền</a>
            <a href="${pageContext.request.contextPath}/video-license">Bản quyền video</a>
            <a href="${pageContext.request.contextPath}/permission">Phân quyền tài liệu</a>
            <a href="${pageContext.request.contextPath}/video-permission">Phân quyền video</a>
        </c:if>
        <a href="${pageContext.request.contextPath}/history">Lịch sử</a>
        <a href="${pageContext.request.contextPath}/favorites">⭐ Yêu thích</a>
        <a href="${pageContext.request.contextPath}/notifications">🔔 Thông báo<c:if test="${unreadNotifications > 0}"> (${unreadNotifications})</c:if></a>
        <a href="${pageContext.request.contextPath}/profile">Hồ sơ</a>
        <a href="${pageContext.request.contextPath}/permission-request">Yêu cầu quyền tài liệu</a>
        <a href="${pageContext.request.contextPath}/video-permission-request">Yêu cầu quyền video</a>
        <c:if test="${sessionScope.currentUser.role == 'ADMIN'}"><a href="${pageContext.request.contextPath}/audit-logs">📝 Audit</a></c:if>
        <span class="user-info">${sessionScope.currentUser.fullName} (${sessionScope.currentUser.role})</span>
        <a href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
    </nav>
</div>
