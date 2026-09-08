<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Thư viện số</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
</head>
<body>
<%@ include file="/views/_navbar.jsp" %>

<div class="container dashboard-page">
    <section class="hero card">
        <div>
            <span class="eyebrow">DIGITAL LIBRARY MANAGEMENT</span>
            <h1>Xin chào, ${sessionScope.currentUser.fullName} 👋</h1>
            <p>Quản lý tài liệu số, bản quyền và quyền truy cập trên một hệ thống tập trung.</p>
        </div>
        <div class="hero-actions">
            <a href="${pageContext.request.contextPath}/documents" class="btn btn-primary">📚 Kho tài liệu</a>
            <c:if test="${sessionScope.currentUser.role == 'ADMIN' or sessionScope.currentUser.role == 'LIBRARIAN'}">
                <a href="${pageContext.request.contextPath}/documents?action=new" class="btn btn-light">＋ Thêm tài liệu</a>
            </c:if>
        </div>
    </section>

    <section class="stats-grid">
        <div class="stat-card">
            <div class="stat-icon">📚</div>
            <div><div class="stat-label">Tổng tài liệu</div><div class="stat-value">${totalDocuments}</div><div class="stat-note">${availableDocuments} đang khả dụng</div></div>
        </div>
        <div class="stat-card">
            <div class="stat-icon">🎬</div>
            <div><div class="stat-label">Tổng video</div><div class="stat-value">${totalVideos}</div><div class="stat-note">${availableVideos} đang khả dụng</div></div>
        </div>
        <div class="stat-card">
            <div class="stat-icon">🗂️</div>
            <div><div class="stat-label">Danh mục</div><div class="stat-value">${totalCategories}</div><div class="stat-note">Phân loại tài liệu</div></div>
        </div>
        <div class="stat-card">
            <div class="stat-icon">👥</div>
            <div><div class="stat-label">Người dùng</div><div class="stat-value">${totalUsers}</div><div class="stat-note">${activeUsers} tài khoản hoạt động</div></div>
        </div>
        <div class="stat-card">
            <div class="stat-icon">🔐</div>
            <div><div class="stat-label">Quyền truy cập</div><div class="stat-value">${totalPermissions}</div><div class="stat-note">VIEW / DOWNLOAD / EDIT</div></div>
        </div>
    </section>

    <section class="stats-grid stats-grid-small">
        <div class="mini-stat"><span>©️ Bản quyền tài liệu hợp lệ</span><strong>${validLicenses}</strong></div>
        <div class="mini-stat"><span>©️ Bản quyền video hợp lệ</span><strong>${validVideoLicenses}</strong></div>
        <div class="mini-stat"><span>⬇️ Lượt tải xuống</span><strong>${totalDownloads}</strong></div>
        <div class="mini-stat"><span>👁️ Lượt xem</span><strong>${totalViews}</strong></div>
    </section>

    <div class="dashboard-columns">
        <section class="card">
            <div class="section-heading">
                <div><span class="eyebrow">LIBRARY</span><h2>Tài liệu mới cập nhật</h2></div>
                <a href="${pageContext.request.contextPath}/documents" class="btn btn-secondary btn-sm">Xem tất cả</a>
            </div>
            <c:choose>
                <c:when test="${empty recentDocuments}"><div class="empty-state">📭<strong>Chưa có tài liệu</strong><span>Hệ thống chưa có tài liệu nào.</span></div></c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead><tr><th>Tài liệu</th><th>Danh mục</th><th>Quyền</th><th>Trạng thái</th><th></th></tr></thead>
                            <tbody>
                            <c:forEach var="doc" items="${recentDocuments}">
                                <tr>
                                    <td><div class="doc-title">${doc.title}</div><small>${doc.author}</small></td>
                                    <td>${doc.categoryName}</td>
                                    <td><span class="badge badge-${doc.accessLevel == 'PUBLIC' ? 'public' : doc.accessLevel == 'RESTRICTED' ? 'restricted' : 'private'}">${doc.accessLevel}</span></td>
                                    <td><span class="status-dot ${doc.status == 'AVAILABLE' ? 'status-active' : 'status-locked'}"></span>${doc.status}</td>
                                    <td><a class="link-arrow" href="${pageContext.request.contextPath}/documents/detail?id=${doc.documentId}">Xem →</a></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section class="card activity-card">
            <div class="section-heading"><div><span class="eyebrow">ACTIVITY</span><h2>Hoạt động gần đây</h2></div></div>
            <c:choose>
                <c:when test="${empty recentActivities}"><div class="empty-state">🕘<strong>Chưa có hoạt động</strong><span>Lịch sử truy cập sẽ xuất hiện tại đây.</span></div></c:when>
                <c:otherwise>
                    <div class="activity-list">
                        <c:forEach var="item" items="${recentActivities}">
                            <div class="activity-item">
                                <div class="activity-icon">${item.actionType == 'DOWNLOAD' ? '⬇️' : item.actionType == 'VIEW' ? '👁️' : item.actionType == 'LOGIN' ? '🔑' : '🚪'}</div>
                                <div class="activity-content">
                                    <strong>${item.actionType}</strong>
                                    <span>${empty item.documentTitle ? 'Phiên đăng nhập' : item.documentTitle}</span>
                                    <small>${item.username} · ${item.accessTime}</small>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </div>

    <section class="card mt-3">
        <div class="section-heading">
            <div><span class="eyebrow">LIBRARY</span><h2>Video mới cập nhật</h2></div>
            <a href="${pageContext.request.contextPath}/videos" class="btn btn-secondary btn-sm">Xem tất cả</a>
        </div>
        <c:choose>
            <c:when test="${empty recentVideos}"><div class="empty-state">📭<strong>Chưa có video</strong><span>Hệ thống chưa có video nào.</span></div></c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table>
                        <thead><tr><th>Video</th><th>Danh mục</th><th>Quyền</th><th>Trạng thái</th><th></th></tr></thead>
                        <tbody>
                        <c:forEach var="v" items="${recentVideos}">
                            <tr>
                                <td><div class="doc-title">${v.title}</div><small>${v.author}</small></td>
                                <td>${v.categoryName}</td>
                                <td><span class="badge badge-${v.accessLevel == 'PUBLIC' ? 'public' : v.accessLevel == 'RESTRICTED' ? 'restricted' : 'private'}">${v.accessLevel}</span></td>
                                <td><span class="status-dot ${v.status == 'AVAILABLE' ? 'status-active' : 'status-locked'}"></span>${v.status}</td>
                                <td><a class="link-arrow" href="${pageContext.request.contextPath}/videos/detail?id=${v.videoId}">Xem →</a></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
<div class="dashboard-columns mt-3"><section class="card"><div class="section-heading"><div><span class="eyebrow">ANALYTICS</span><h2>Tài liệu theo danh mục</h2></div></div><c:forEach var="entry" items="${documentsByCategory}"><div class="bar-row"><span>${entry.key}</span><div class="bar-track"><div class="bar-fill" style="width:${totalDocuments > 0 ? (entry.value * 100 / totalDocuments) : 0}%"></div></div><strong>${entry.value}</strong></div></c:forEach></section><section class="card"><div class="section-heading"><div><span class="eyebrow">ACTIVITY</span><h2>Hoạt động hệ thống</h2></div></div><c:forEach var="entry" items="${activitySummary}"><div class="bar-row"><span>${entry.key}</span><div class="bar-track"><div class="bar-fill" style="width:${totalViews + totalDownloads > 0 ? (entry.value * 100 / (totalViews + totalDownloads)) : 0}%"></div></div><strong>${entry.value}</strong></div></c:forEach></section></div>
<section class="card mt-3"><div class="section-heading"><div><span class="eyebrow">POPULAR</span><h2>Tài liệu được truy cập nhiều nhất</h2></div></div><table><thead><tr><th>#</th><th>Tài liệu</th><th>Lượt xem/tải</th></tr></thead><tbody><c:forEach var="item" items="${topDocuments}" varStatus="st"><tr><td>${st.count}</td><td>${item.title}</td><td><strong>${item.total}</strong></td></tr></c:forEach></tbody></table></section>
</div>
</body>
</html>
