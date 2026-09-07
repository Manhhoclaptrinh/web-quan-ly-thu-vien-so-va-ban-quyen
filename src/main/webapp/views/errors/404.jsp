<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>404 - Không tìm thấy trang</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/errors.css">
</head>
<body>
    <div class="error-wrapper">
        <div class="error-box">
            <div class="error-code">404</div>
            <h1>Không tìm thấy trang bạn yêu cầu</h1>
            <p>Đường dẫn có thể đã bị đổi tên, xoá, hoặc bạn đã nhập sai địa chỉ.</p>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/home">Về trang chủ</a>
        </div>
    </div>
</body>
</html>
