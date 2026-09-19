<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kết quả thanh toán - Thư viện số</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/errors.css">
</head>
<body>
<div class="error-wrapper" style="background: ${success ? 'linear-gradient(135deg,#0c5a5a,#17c3c3)' : 'linear-gradient(135deg,#7f1d1d,#b3261e)'};">
    <div class="error-box">
        <div class="error-code" style="font-size:52px; color: ${success ? '#0f6f6f' : '#b3261e'};">
            ${success ? '✅' : '❌'}
        </div>
        <h1>${resultTitle}</h1>
        <p>${resultMessage}</p>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/wallet">Về Ví của tôi</a>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/membership" style="margin-left:8px;">Về Hội viên</a>
    </div>
</div>
</body>
</html>
