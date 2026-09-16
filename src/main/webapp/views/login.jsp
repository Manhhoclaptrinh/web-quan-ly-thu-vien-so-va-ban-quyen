<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - Thư viện số CNJ25</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth-login.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
</head>
<body>

<div class="container" id="container">

    <%-- ============ FORM ĐĂNG KÝ (tự do, không cần quản trị viên duyệt) ============ --%>
    <div class="form-container register-container">
        <form action="${pageContext.request.contextPath}/register" method="post">
            <h1>Registration</h1>

            <c:if test="${not empty registerError}">
                <div class="alert-error">${registerError}</div>
            </c:if>

            <div class="input-group">
                <input type="text" name="username" placeholder="Username" value="${oldUsername}" required>
                <i class="fa-solid fa-user"></i>
            </div>
            <div class="input-group">
                <input type="text" name="fullName" placeholder="Họ và tên" value="${oldFullName}" required>
                <i class="fa-solid fa-id-card"></i>
            </div>
            <div class="input-group">
                <input type="email" name="email" placeholder="Email" value="${oldEmail}" required>
                <i class="fa-solid fa-envelope"></i>
            </div>
            <div class="input-group">
                <input type="password" name="password" placeholder="Password (tối thiểu 6 ký tự)" required minlength="6">
                <i class="fa-solid fa-lock"></i>
            </div>
            <button type="submit" class="main-btn">Register</button>
            <p class="note-text">Đăng ký tự do, sử dụng ngay sau khi tạo tài khoản.</p>
            <p class="mobile-toggle" id="toLoginMobile">Đã có tài khoản? Đăng nhập</p>
        </form>
    </div>

    <%-- ============ FORM ĐĂNG NHẬP (kết nối LoginServlet có sẵn) ============ --%>
    <div class="form-container login-container">
        <form action="${pageContext.request.contextPath}/login" method="post">
            <h1>Login</h1>

            <c:if test="${not empty error}">
                <div class="alert-error">${error}</div>
            </c:if>
            <c:if test="${param.registered == '1'}">
                <div class="alert-success">Tạo tài khoản thành công! Hãy đăng nhập.</div>
            </c:if>

            <div class="input-group">
                <input type="text" name="username" placeholder="Username" required autofocus>
                <i class="fa-solid fa-user"></i>
            </div>
            <div class="input-group">
                <input type="password" name="password" placeholder="Password" required>
                <i class="fa-solid fa-lock"></i>
            </div>
            <button type="submit" class="main-btn">Login</button>
            <p class="note-text">Tài khoản mẫu: admin / librarian / reader1 &mdash; mật khẩu: 123456</p>
            <p class="mobile-toggle" id="toRegisterMobile">Chưa có tài khoản? Đăng ký</p>
        </form>
    </div>

    <%-- ============ LỚP PHỦ TRƯỢT (hiệu ứng chuyển đổi Login / Register) ============ --%>
    <div class="overlay-container">
        <div class="overlay">
            <div class="overlay-panel overlay-left">
                <h1>Welcome Back!</h1>
                <p>Already have an Account</p>
                <button class="ghost-btn" id="loginBtn" type="button">Login</button>
            </div>
            <div class="overlay-panel overlay-right">
                <h1>Hello, Welcome</h1>
                <p>Don't have an Account</p>
                <button class="ghost-btn" id="registerBtn" type="button">Register</button>
            </div>
        </div>
    </div>

</div>

<script src="${pageContext.request.contextPath}/js/auth-login.js"></script>
</body>
</html>
