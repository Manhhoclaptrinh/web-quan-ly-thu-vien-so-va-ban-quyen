<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo thống kê - Thủ thư</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/views/_navbar.jsp" %>

<div class="container librarian-report-page">
    <section class="report-hero card">
        <div>
            <span class="eyebrow">LIBRARIAN ANALYTICS</span>
            <h1>Báo cáo & thống kê thư viện</h1>
            <p>Tổng hợp kho nội dung, hoạt động sử dụng, yêu cầu cấp quyền, đánh giá, bản quyền và giao dịch.</p>
        </div>
        <div class="report-role">THỦ THƯ · READ ONLY REPORT</div>
    </section>

    <section class="card report-filter">
        <div class="report-filter-head">
            <div>
                <span class="eyebrow">REPORT PERIOD</span>
                <h2>Khoảng thời gian báo cáo</h2>
            </div>
            <span class="report-period-info">${startDate} → ${endDate}</span>
        </div>
        <div class="report-filter-actions">
            <a class="${selectedRange == 'today' ? 'active' : ''}" href="${pageContext.request.contextPath}/librarian/reports?range=today">Hôm nay</a>
            <a class="${selectedRange == '7days' ? 'active' : ''}" href="${pageContext.request.contextPath}/librarian/reports?range=7days">7 ngày</a>
            <a class="${selectedRange == '30days' ? 'active' : ''}" href="${pageContext.request.contextPath}/librarian/reports?range=30days">30 ngày</a>
            <a class="${selectedRange == 'year' ? 'active' : ''}" href="${pageContext.request.contextPath}/librarian/reports?range=year">Năm nay</a>
            <a class="${selectedRange == '5years' ? 'active' : ''}" href="${pageContext.request.contextPath}/librarian/reports?range=5years">5 năm</a>
        </div>
        <form class="report-custom-form" method="get" action="${pageContext.request.contextPath}/librarian/reports">
            <input type="hidden" name="range" value="custom">
            <label>Từ <input type="date" name="start" value="${startDate}" required></label>
            <label>Đến <input type="date" name="end" value="${endDate}" required></label>
            <button type="submit" class="btn btn-primary">Áp dụng</button>
        </form>
    </section>

    <c:set var="o" value="${overview}"/>
    <section class="report-kpi-grid">
        <div class="report-kpi"><span>📄</span><div><small>Tài liệu</small><strong>${o.documents}</strong><em>${o.availableDocuments} khả dụng</em></div></div>
        <div class="report-kpi"><span>📚</span><div><small>Sách</small><strong>${o.books}</strong><em>${o.availableBooks} khả dụng</em></div></div>
        <div class="report-kpi"><span>🎬</span><div><small>Video</small><strong>${o.videos}</strong><em>${o.availableVideos} khả dụng</em></div></div>
        <div class="report-kpi"><span>👥</span><div><small>Người dùng hoạt động</small><strong>${o.activeUsers}</strong><em>${o.categories} danh mục</em></div></div>
        <div class="report-kpi"><span>👁️</span><div><small>Lượt xem</small><strong>${o.views}</strong><em>Toàn hệ thống</em></div></div>
        <div class="report-kpi"><span>⬇️</span><div><small>Lượt tải</small><strong>${o.downloads}</strong><em>Toàn hệ thống</em></div></div>
        <div class="report-kpi"><span>⭐</span><div><small>Lượt yêu thích</small><strong>${o.favorites}</strong><em>${o.reviews} lượt đánh giá</em></div></div>
        <div class="report-kpi report-kpi-alert"><span>📥</span><div><small>Yêu cầu chờ xử lý</small><strong>${o.pendingRequests}</strong><em>Quyền tài liệu + video</em></div></div>
    </section>

    <section class="card report-panel">
        <div class="section-heading">
            <div><span class="eyebrow">TREND</span><h2>Xu hướng hoạt động 5 năm</h2></div>
            <span class="report-note">Lượt xem + lượt tải</span>
        </div>
        <c:set var="maxYear" value="1"/>
        <c:forEach var="x" items="${yearlyReport}"><c:if test="${x.total > maxYear}"><c:set var="maxYear" value="${x.total}"/></c:if></c:forEach>
        <div class="year-chart">
            <c:choose>
                <c:when test="${empty yearlyReport}"><div class="empty-state">📭<strong>Chưa có dữ liệu 5 năm</strong><span>Hệ thống sẽ tự cập nhật khi có lịch sử truy cập.</span></div></c:when>
                <c:otherwise>
                    <c:forEach var="x" items="${yearlyReport}">
                        <div class="year-bar-item">
                            <strong>${x.total}</strong>
                            <div class="year-bar-track"><div class="year-bar-fill" style="height:${x.total == 0 ? 5 : (x.total * 190 / maxYear + 8)}px"></div></div>
                            <b>${x.year}</b>
                            <small>👁 ${x.views} · ⬇ ${x.downloads}</small>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <div class="report-grid-2">
        <section class="card report-panel">
            <div class="section-heading"><div><span class="eyebrow">MONTHLY</span><h2>Hoạt động theo tháng · ${reportYear}</h2></div></div>
            <div class="monthly-report-grid">
                <c:forEach var="x" items="${monthlyReport}">
                    <div class="month-stat"><span>Tháng ${x.month}</span><strong>${x.total}</strong><small>👁 ${x.views} · ⬇ ${x.downloads}</small></div>
                </c:forEach>
            </div>
        </section>

        <section class="card report-panel">
            <div class="section-heading"><div><span class="eyebrow">ACTIVITY</span><h2>Hoạt động trong khoảng chọn</h2></div></div>
            <c:set var="maxAccess" value="1"/>
            <c:forEach var="x" items="${activityReport}"><c:if test="${x.total > maxAccess}"><c:set var="maxAccess" value="${x.total}"/></c:if></c:forEach>
            <div class="report-bars report-bars-flex">
                <c:forEach var="x" items="${activityReport}">
                    <div class="report-bar-item">
                        <div class="report-bar-value">${x.total}</div>
                        <div class="report-bar-track"><div class="report-bar-fill" style="height:${x.total == 0 ? 5 : (x.total * 150 / maxAccess + 8)}px"></div></div>
                        <strong>${x.day}</strong>
                        <small>👁 ${x.views} · ⬇ ${x.downloads}</small>
                    </div>
                </c:forEach>
            </div>
            <c:if test="${empty activityReport}"><div class="empty-state">Không có dữ liệu trong khoảng đã chọn.</div></c:if>
        </section>
    </div>

    <div class="report-grid-2">
        <section class="card report-panel">
            <div class="section-heading"><div><span class="eyebrow">CONTENT</span><h2>Cơ cấu kho nội dung</h2></div></div>
            <div class="report-type-list">
                <c:forEach var="x" items="${contentByType}"><div class="report-type-row"><div><strong>${x.type}</strong><span>${x.available} khả dụng · ${x.disabled} vô hiệu</span></div><b>${x.total}</b></div></c:forEach>
            </div>
        </section>
        <section class="card report-panel">
            <div class="section-heading"><div><span class="eyebrow">ACCESS REQUESTS</span><h2>Tình trạng yêu cầu cấp quyền</h2></div></div>
            <div class="status-stat-grid"><div><span>⏳ Chờ xử lý</span><strong>${requestStats.pending}</strong></div><div><span>✅ Đã duyệt</span><strong>${requestStats.approved}</strong></div><div><span>❌ Từ chối</span><strong>${requestStats.rejected}</strong></div></div>
            <div class="report-info-box">Số liệu gộp cả yêu cầu quyền tài liệu và quyền video.</div>
        </section>
    </div>

    <section class="card report-panel">
        <div class="section-heading"><div><span class="eyebrow">POPULAR CONTENT</span><h2>Top nội dung trong khoảng chọn</h2></div><span class="report-note">Xem + tải</span></div>
        <c:choose>
            <c:when test="${empty topContent}"><div class="empty-state">📭<strong>Chưa có dữ liệu</strong><span>Chưa ghi nhận lượt xem hoặc tải trong khoảng đã chọn.</span></div></c:when>
            <c:otherwise><div class="table-wrap"><table><thead><tr><th>#</th><th>Nội dung</th><th>Loại</th><th>Lượt truy cập</th></tr></thead><tbody><c:forEach var="x" items="${topContent}" varStatus="st"><tr><td><strong>${st.count}</strong></td><td>${x.title}</td><td><span class="report-tag">${x.type}</span></td><td><strong>${x.total}</strong></td></tr></c:forEach></tbody></table></div></c:otherwise>
        </c:choose>
    </section>

    <div class="report-grid-2">
        <section class="card report-panel">
            <div class="section-heading"><div><span class="eyebrow">CATEGORIES</span><h2>Thống kê theo danh mục</h2></div></div>
            <div class="table-wrap"><table><thead><tr><th>Danh mục</th><th>Tài liệu</th><th>Sách</th><th>Video</th><th>Tổng</th></tr></thead><tbody><c:forEach var="x" items="${categoryStats}"><tr><td><strong>${x.name}</strong></td><td>${x.documents}</td><td>${x.books}</td><td>${x.videos}</td><td><strong>${x.total}</strong></td></tr></c:forEach></tbody></table></div>
        </section>
        <section class="card report-panel">
            <div class="section-heading"><div><span class="eyebrow">QUALITY</span><h2>Đánh giá nội dung</h2></div></div>
            <div class="quality-summary"><div><strong>${reviewStats.total}</strong><span>Tổng lượt đánh giá</span></div><div><strong>${reviewStats.average == 0 ? '0.0' : reviewStats.average}</strong><span>Điểm trung bình / 5</span></div></div>
        </section>
    </div>

    <div class="report-grid-2">
        <section class="card report-panel">
            <div class="section-heading"><div><span class="eyebrow">LICENSE</span><h2>Tình trạng bản quyền</h2></div></div>
            <div class="license-summary"><div><span>📄 Tài liệu còn hạn</span><strong>${licenseStats.documents}</strong></div><div><span>📚 Sách còn hạn</span><strong>${licenseStats.books}</strong></div><div><span>🎬 Video còn hạn</span><strong>${licenseStats.videos}</strong></div><div><span>⛔ Đã hết hạn</span><strong>${licenseStats.expired}</strong></div><div class="license-warning"><span>⚠️ Hết hạn trong 30 ngày</span><strong>${licenseStats.expiring30}</strong></div></div>
        </section>
        <section class="card report-panel">
            <div class="section-heading"><div><span class="eyebrow">TRANSACTIONS</span><h2>Giao dịch trong khoảng chọn</h2></div></div>
            <c:set var="t" value="${transactionStats}"/>
            <div class="transaction-grid"><div><span>Tổng giao dịch</span><strong>${t.transactions}</strong></div><div><span>Nạp ví</span><strong>${t.topups}</strong></div><div><span>Giao dịch tải</span><strong>${t.downloads}</strong></div><div><span>Giao dịch xem PDF</span><strong>${t.views}</strong></div><div><span>Gói hội viên</span><strong>${t.memberships}</strong></div><div><span>Doanh thu phí</span><strong><c:out value="${t.revenue}"/> ₫</strong></div></div>
        </section>
    </div>

    <p class="report-footer-note">Báo cáo được tổng hợp trực tiếp từ dữ liệu nghiệp vụ. Màn hình chỉ đọc và không thay đổi dữ liệu thư viện.</p>
</div>
</body>
</html>
