<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Hội viên - Thư viện số</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/wallet-membership.css">
</head>
<body>
<%@ include file="/views/_navbar.jsp" %>

<div class="container">
    <div class="card">
        <h1>🏷️ Hội viên thư viện số</h1>
        <p class="text-muted">Hội viên được xem PDF và tải tài liệu <strong>thoải mái</strong>, không bị tính phí theo lượt.</p>

        <c:if test="${param.success == '1'}"><div class="alert alert-success">Đăng ký / gia hạn hội viên thành công!</div></c:if>
        <c:if test="${param.error == 'invalid-plan'}"><div class="alert alert-error">Gói hội viên không hợp lệ.</div></c:if>

        <c:choose>
            <c:when test="${not empty activeMembership}">
                <div class="alert alert-success">
                    Bạn đang là hội viên gói <strong>${activeMembership.planType == 'MONTHLY' ? 'Tháng' : 'Năm'}</strong>,
                    hết hạn vào <strong>${activeMembership.endDate}</strong>.
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-error">Bạn chưa phải là hội viên. Đăng ký ngay để xem/tải tài liệu thoải mái.</div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="membership-plans mt-3">
        <div class="card plan-card">
            <h2>Gói Tháng</h2>
            <div class="plan-price">${priceMonthly}đ<span>/tháng</span></div>
            <ul class="plan-features">
                <li>✔ Xem PDF không giới hạn</li>
                <li>✔ Tải tài liệu không giới hạn</li>
                <li>✔ Phù hợp dùng thử / ngắn hạn</li>
            </ul>
            <form action="${pageContext.request.contextPath}/membership" method="post">
                <input type="hidden" name="planType" value="MONTHLY">
                <button type="submit" class="btn btn-primary btn-block">
                    ${not empty activeMembership ? 'Gia hạn thêm 1 tháng' : 'Đăng ký gói Tháng'}
                </button>
            </form>
        </div>

        <div class="card plan-card plan-card-featured">
            <div class="plan-badge">Tiết kiệm hơn</div>
            <h2>Gói Năm</h2>
            <div class="plan-price">${priceYearly}đ<span>/năm</span></div>
            <ul class="plan-features">
                <li>✔ Xem PDF không giới hạn</li>
                <li>✔ Tải tài liệu không giới hạn</li>
                <li>✔ Chỉ ~41.700đ/tháng, rẻ hơn đăng ký tháng</li>
            </ul>
            <form action="${pageContext.request.contextPath}/membership" method="post">
                <input type="hidden" name="planType" value="YEARLY">
                <button type="submit" class="btn btn-primary btn-block">
                    ${not empty activeMembership ? 'Gia hạn thêm 1 năm' : 'Đăng ký gói Năm'}
                </button>
            </form>
        </div>
    </div>

    <div class="card mt-3">
        <p class="text-muted">Không muốn cam kết dài hạn? Bạn có thể dùng <a href="${pageContext.request.contextPath}/wallet">Ví điện tử</a> để trả phí theo lượt xem/tải thay vì đăng ký hội viên.</p>
    </div>
</div>
</body>
</html>
