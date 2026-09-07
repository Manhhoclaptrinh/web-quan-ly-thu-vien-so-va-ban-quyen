<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>403 - Không có quyền truy cập</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/errors.css">
</head>
<body>
    <div class="error-wrapper">
        <div class="error-box">
            <div class="error-code">403</div>
            <h1>Bạn không có quyền truy cập trang này</h1>
            <p>Tài khoản của bạn không đủ quyền để xem nội dung này. Nếu bạn cho rằng đây là nhầm lẫn, hãy liên hệ quản trị viên hệ thống.</p>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/home">Về trang chủ</a>
        </div>
    </div>
</body>
</html>
