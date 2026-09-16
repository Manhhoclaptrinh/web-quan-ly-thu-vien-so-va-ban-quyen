// Hiệu ứng trượt chuyển đổi giữa form Login và Register (trang đăng nhập)
document.addEventListener('DOMContentLoaded', function () {
    var container = document.getElementById('container');
    var registerBtn = document.getElementById('registerBtn');
    var loginBtn = document.getElementById('loginBtn');
    var toRegisterMobile = document.getElementById('toRegisterMobile');
    var toLoginMobile = document.getElementById('toLoginMobile');
    var registerForm = document.getElementById('registerForm');

    function showRegister() {
        container.classList.add('active');
    }

    function showLogin() {
        container.classList.remove('active');
    }

    if (registerBtn) registerBtn.addEventListener('click', showRegister);
    if (loginBtn) loginBtn.addEventListener('click', showLogin);
    if (toRegisterMobile) toRegisterMobile.addEventListener('click', showRegister);
    if (toLoginMobile) toLoginMobile.addEventListener('click', showLogin);

    // Nếu server trả về lỗi đăng ký, mở sẵn tab Register để người dùng thấy lỗi và sửa lại
    var hasRegisterError = document.querySelector('.register-container .alert-error');
    if (hasRegisterError) {
        showRegister();
    }

    // Nếu server trả về lỗi đăng nhập (${error}), luôn mở sẵn tab Login
    var hasLoginError = document.querySelector('.login-container .alert-error');
    if (hasLoginError) {
        showLogin();
    }
});
