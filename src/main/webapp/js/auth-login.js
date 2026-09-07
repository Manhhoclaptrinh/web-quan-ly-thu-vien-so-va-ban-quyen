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

    // Form Register chỉ mang tính giao diện: tài khoản do quản trị viên tạo (xem UserServlet)
    if (registerForm) {
        registerForm.addEventListener('submit', function (e) {
            e.preventDefault();
            alert('Tài khoản trong hệ thống do quản trị viên cấp.\nVui lòng liên hệ quản trị viên để được tạo tài khoản.');
        });
    }

    // Nếu server trả về lỗi đăng nhập (${error}), luôn mở sẵn tab Login
    var hasLoginError = document.querySelector('.login-container .alert-error');
    if (hasLoginError) {
        showLogin();
    }
});
