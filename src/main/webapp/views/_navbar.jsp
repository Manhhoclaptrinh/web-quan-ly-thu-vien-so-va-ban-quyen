<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%
    pageContext.setAttribute("navUri", request.getRequestURI());
%>
<button type="button" class="sidebar-toggle-mobile" id="sidebarToggleMobile" aria-label="Mở menu">☰</button>
<div class="sidebar-overlay" id="sidebarOverlay"></div>

<aside class="sidebar" id="sidebar">
    <div class="sidebar-header">
        <a class="brand" href="${pageContext.request.contextPath}/home">
            <span class="brand-icon">📚</span><span class="brand-text">Library</span>
        </a>
        <button type="button" class="sidebar-collapse-btn" id="sidebarCollapseBtn" title="Thu gọn menu">«</button>
    </div>

    <div class="sidebar-user">
        <div class="avatar">${fn:toUpperCase(fn:substring(sessionScope.currentUser.fullName, 0, 1))}</div>
        <div class="sidebar-user-info">
            <strong>${sessionScope.currentUser.fullName}</strong>
            <span class="role-badge role-${sessionScope.currentUser.role}">${sessionScope.currentUser.role}</span>
        </div>
    </div>

    <nav class="sidebar-nav">
        <div class="nav-group">
            <span class="nav-group-label">Tổng quan</span>
            <a class="nav-link ${fn:endsWith(navUri,'/home') ? 'active' : ''}" href="${pageContext.request.contextPath}/home"><span class="nav-icon">🏠</span><span>Dashboard</span></a>
        </div>

        <div class="nav-group">
            <span class="nav-group-label">Nội dung</span>
            <a class="nav-link ${fn:contains(navUri,'/documents') ? 'active' : ''}" href="${pageContext.request.contextPath}/documents"><span class="nav-icon">📄</span><span>Tài liệu</span></a>
            <a class="nav-link ${fn:contains(navUri,'/videos') ? 'active' : ''}" href="${pageContext.request.contextPath}/videos"><span class="nav-icon">🎬</span><span>Video</span></a>
            <a class="nav-link ${fn:contains(navUri,'/books') ? 'active' : ''}" href="${pageContext.request.contextPath}/books"><span class="nav-icon">📚</span><span>Sách</span></a>
        </div>

        <c:if test="${sessionScope.currentUser.role == 'ADMIN' or sessionScope.currentUser.role == 'LIBRARIAN'}">
        <div class="nav-group">
            <span class="nav-group-label">Quản trị</span>
            <a class="nav-link ${fn:contains(navUri,'/categories') ? 'active' : ''}" href="${pageContext.request.contextPath}/categories"><span class="nav-icon">🗂️</span><span>Danh mục</span></a>
            <c:if test="${sessionScope.currentUser.role == 'ADMIN'}">
                <a class="nav-link ${fn:contains(navUri,'/users') ? 'active' : ''}" href="${pageContext.request.contextPath}/users"><span class="nav-icon">👥</span><span>Người dùng</span></a>
            </c:if>
            <a class="nav-link ${fn:contains(navUri,'/license') and not fn:contains(navUri,'/video-license') ? 'active' : ''}" href="${pageContext.request.contextPath}/license"><span class="nav-icon">©️</span><span>Bản quyền</span></a>
            <a class="nav-link ${fn:contains(navUri,'/video-license') ? 'active' : ''}" href="${pageContext.request.contextPath}/video-license"><span class="nav-icon">🎞️</span><span>Bản quyền video</span></a>
            <a class="nav-link ${fn:contains(navUri,'/permission') and not fn:contains(navUri,'/permission-request') and not fn:contains(navUri,'/video-permission') ? 'active' : ''}" href="${pageContext.request.contextPath}/permission"><span class="nav-icon">🔐</span><span>Phân quyền tài liệu</span></a>
            <a class="nav-link ${fn:contains(navUri,'/video-permission') and not fn:contains(navUri,'/video-permission-request') ? 'active' : ''}" href="${pageContext.request.contextPath}/video-permission"><span class="nav-icon">🔐</span><span>Phân quyền video</span></a>
            <c:if test="${sessionScope.currentUser.role == 'ADMIN'}">
                <a class="nav-link ${fn:contains(navUri,'/audit-logs') ? 'active' : ''}" href="${pageContext.request.contextPath}/audit-logs"><span class="nav-icon">📝</span><span>Audit</span></a>
            </c:if>
        </div>
        </c:if>

        <div class="nav-group">
            <span class="nav-group-label">Cá nhân</span>
            <a class="nav-link ${fn:contains(navUri,'/history') ? 'active' : ''}" href="${pageContext.request.contextPath}/history"><span class="nav-icon">🕘</span><span>Lịch sử</span></a>
            <a class="nav-link ${fn:contains(navUri,'/favorites') ? 'active' : ''}" href="${pageContext.request.contextPath}/favorites"><span class="nav-icon">⭐</span><span>Yêu thích</span></a>
            <a class="nav-link ${fn:contains(navUri,'/notifications') ? 'active' : ''}" href="${pageContext.request.contextPath}/notifications"><span class="nav-icon">🔔</span><span>Thông báo</span><c:if test="${unreadNotifications > 0}"><span class="nav-badge">${unreadNotifications}</span></c:if></a>
            <a class="nav-link ${fn:contains(navUri,'/profile') ? 'active' : ''}" href="${pageContext.request.contextPath}/profile"><span class="nav-icon">👤</span><span>Hồ sơ</span></a>
            <a class="nav-link ${fn:contains(navUri,'/permission-request') and not fn:contains(navUri,'/video-permission-request') ? 'active' : ''}" href="${pageContext.request.contextPath}/permission-request"><span class="nav-icon">📥</span><span>Yêu cầu quyền tài liệu</span></a>
            <a class="nav-link ${fn:contains(navUri,'/video-permission-request') ? 'active' : ''}" href="${pageContext.request.contextPath}/video-permission-request"><span class="nav-icon">📥</span><span>Yêu cầu quyền video</span></a>
        </div>
    </nav>

    <div class="sidebar-footer">
        <a class="nav-link logout-link" href="${pageContext.request.contextPath}/logout"><span class="nav-icon">🚪</span><span>Đăng xuất</span></a>
    </div>
</aside>

<script>
(function () {
    var body = document.body;
    var collapseBtn = document.getElementById('sidebarCollapseBtn');
    var mobileBtn = document.getElementById('sidebarToggleMobile');
    var overlay = document.getElementById('sidebarOverlay');

    if (collapseBtn) {
        collapseBtn.addEventListener('click', function () {
            body.classList.toggle('sidebar-collapsed');
        });
    }
    if (mobileBtn) {
        mobileBtn.addEventListener('click', function () {
            body.classList.toggle('sidebar-mobile-open');
        });
    }
    if (overlay) {
        overlay.addEventListener('click', function () {
            body.classList.remove('sidebar-mobile-open');
        });
    }
})();
</script>
