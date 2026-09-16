<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Lịch sử đăng ký hội viên - Thư viện số</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/wallet-membership.css">
</head>
<body>
<%@ include file="/views/_navbar.jsp" %>

<div class="container">
    <div class="card">
        <h1>🏷️ Lịch sử đăng ký hội viên</h1>
        <p class="text-muted">Toàn bộ lượt đăng ký / gia hạn gói hội viên Tháng và Năm của người dùng.</p>

        <form action="${pageContext.request.contextPath}/membership-history" method="get" class="search-form">
            <input type="text" name="username" class="form-control" placeholder="Tìm theo tên tài khoản..." value="${keyword}">
            <button type="submit" class="btn btn-primary">Tìm</button>
            <c:if test="${not empty keyword}">
                <a href="${pageContext.request.contextPath}/membership-history" class="btn btn-secondary">Xóa lọc</a>
            </c:if>
        </form>

        <c:choose>
            <c:when test="${empty memberships}">
                <p class="text-muted mt-3">Không có lượt đăng ký hội viên nào.</p>
            </c:when>
            <c:otherwise>
                <table class="mt-3">
                    <tr><th>Ngày đăng ký</th><th>Tài khoản</th><th>Gói</th><th>Giá</th><th>Bắt đầu</th><th>Hết hạn</th></tr>
                    <c:forEach var="m" items="${memberships}">
                        <tr>
                            <td>${m.createdAt}</td>
                            <td>${m.username}</td>
                            <td>${m.planType == 'MONTHLY' ? 'Tháng' : 'Năm'}</td>
                            <td>${m.price}đ</td>
                            <td>${m.startDate}</td>
                            <td>${m.endDate}</td>
                        </tr>
                    </c:forEach>
                </table>
            </c:otherwise>
        </c:choose>
    </div>
</div>
</body>
</html>
