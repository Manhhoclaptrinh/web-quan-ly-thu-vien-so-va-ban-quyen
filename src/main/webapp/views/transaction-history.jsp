<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Lịch sử giao dịch - Thư viện số</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/wallet-membership.css">
</head>
<body>
<%@ include file="/views/_navbar.jsp" %>

<div class="container">
    <div class="card">
        <h1>💳 Lịch sử giao dịch</h1>
        <p class="text-muted">Toàn bộ giao dịch nạp ví, đăng ký hội viên, phí xem PDF và phí tải xuống của người dùng.</p>

        <form action="${pageContext.request.contextPath}/transaction-history" method="get" class="search-form">
            <input type="text" name="username" class="form-control" placeholder="Tìm theo tên tài khoản..." value="${keyword}">
            <button type="submit" class="btn btn-primary">Tìm</button>
            <c:if test="${not empty keyword}">
                <a href="${pageContext.request.contextPath}/transaction-history" class="btn btn-secondary">Xóa lọc</a>
            </c:if>
        </form>

        <c:choose>
            <c:when test="${empty transactions}">
                <p class="text-muted mt-3">Không có giao dịch nào.</p>
            </c:when>
            <c:otherwise>
                <table class="mt-3">
                    <tr><th>Thời gian</th><th>Tài khoản</th><th>Loại</th><th>Số tiền</th><th>Mô tả</th></tr>
                    <c:forEach var="tx" items="${transactions}">
                        <tr>
                            <td>${tx.createdAt}</td>
                            <td>${tx.username}</td>
                            <td>${tx.type}</td>
                            <td class="${tx.amount >= 0 ? 'amount-positive' : 'amount-negative'}">${tx.amount >= 0 ? '+' : ''}${tx.amount}đ</td>
                            <td>${tx.description}</td>
                        </tr>
                    </c:forEach>
                </table>
            </c:otherwise>
        </c:choose>
    </div>
</div>
</body>
</html>
