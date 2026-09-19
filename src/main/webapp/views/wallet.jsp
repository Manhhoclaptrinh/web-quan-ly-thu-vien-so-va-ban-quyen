<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Ví của tôi - Thư viện số</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/wallet-membership.css">
</head>
<body>
<%@ include file="/views/_navbar.jsp" %>

<div class="container">
    <div class="card">
        <h1>💰 Ví của tôi</h1>
        <p class="text-muted">Dùng ví để trả phí xem PDF (500đ/giờ) và tải tài liệu (50.000đ/lượt) theo nhu cầu, không cần đăng ký hội viên dài hạn.</p>

        <c:if test="${param.success == '1'}"><div class="alert alert-success">Nạp tiền thành công!</div></c:if>
        <c:if test="${param.error == 'invalid-amount'}"><div class="alert alert-error">Số tiền không hợp lệ.</div></c:if>

        <div class="wallet-balance-box">
            <span>Số dư hiện tại</span>
            <strong>${walletBalance}đ</strong>
        </div>

        <h2 class="mt-3">💳 Nạp tiền qua VNPay </h2>
        <p class="text-muted">Quét QR / thẻ ATM nội địa / thẻ quốc tế ngay trên trang VNPay.</p>
        <form action="${pageContext.request.contextPath}/payment/vnpay/create" method="post">
            <input type="hidden" name="orderType" value="WALLET_TOPUP">
            <div class="form-group">
                <div class="topup-presets">
                    <button type="submit" name="amount" value="50000" class="btn btn-primary">50.000đ</button>
                    <button type="submit" name="amount" value="100000" class="btn btn-primary">100.000đ</button>
                    <button type="submit" name="amount" value="200000" class="btn btn-primary">200.000đ</button>
                    <button type="submit" name="amount" value="500000" class="btn btn-primary">500.000đ</button>
                </div>
            </div>
        </form>
        <form action="${pageContext.request.contextPath}/payment/vnpay/create" method="post" class="mt-3 topup-custom-form">
            <input type="hidden" name="orderType" value="WALLET_TOPUP">
            <div class="form-group">
                <label>Hoặc nhập số tiền khác (đ)</label>
                <input type="number" name="amount" class="form-control" min="1000" step="1000" placeholder="Ví dụ: 150000" required>
            </div>
            <button type="submit" class="btn btn-primary">Thanh toán qua VNPay</button>
        </form>

        <hr class="mt-3" style="border:none;border-top:1px dashed #cbd5e1;">

        <h2 class="mt-3">🧪 Nạp giả lập </h2>
        <form action="${pageContext.request.contextPath}/wallet" method="post" class="mt-3">
            <div class="form-group">
                <div class="topup-presets">
                    <button type="submit" name="amount" value="50000" class="btn btn-secondary">50.000đ</button>
                    <button type="submit" name="amount" value="100000" class="btn btn-secondary">100.000đ</button>
                    <button type="submit" name="amount" value="200000" class="btn btn-secondary">200.000đ</button>
                    <button type="submit" name="amount" value="500000" class="btn btn-secondary">500.000đ</button>
                </div>
            </div>
        </form>
        <form action="${pageContext.request.contextPath}/wallet" method="post" class="mt-3 topup-custom-form">
            <div class="form-group">
                <label>Hoặc nhập số tiền khác (đ)</label>
                <input type="number" name="amount" class="form-control" min="1000" step="1000" placeholder="Ví dụ: 150000" required>
            </div>
            <button type="submit" class="btn btn-secondary">Nạp tiền </button>
        </form>
    </div>

    <div class="card mt-3">
        <h2>🕘 Lịch sử giao dịch của tôi</h2>
        <c:choose>
            <c:when test="${empty transactions}">
                <p class="text-muted">Chưa có giao dịch nào.</p>
            </c:when>
            <c:otherwise>
                <table class="mt-3">
                    <tr><th>Thời gian</th><th>Loại</th><th>Số tiền</th><th>Mô tả</th></tr>
                    <c:forEach var="tx" items="${transactions}">
                        <tr>
                            <td>${tx.createdAt}</td>
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
