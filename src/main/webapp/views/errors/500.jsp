<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 - Lỗi hệ thống</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/errors.css">
</head>
<body>
    <div class="error-wrapper">
        <div class="error-box">
            <div class="error-code">500</div>
            <h1>Hệ thống đang gặp sự cố</h1>
            <p>Đã có lỗi xảy ra ở phía máy chủ. Vui lòng thử lại sau ít phút, hoặc báo cho nhóm phát triển nếu lỗi này tiếp diễn.</p>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/login">Về trang đăng nhập</a>

            <%-- Chỉ hữu ích lúc phát triển - nên xoá hoặc ẩn trước khi nộp bài / lên production --%>
            <% if (exception != null) { %>
            <details class="error-detail" open>
                <summary>Chi tiết lỗi (chỉ hiện lúc phát triển)</summary>
                <pre><%
                    Throwable t = exception;
                    while (t != null) {
                        out.println(t.toString());
                        StackTraceElement[] trace = t.getStackTrace();
                        int limit = Math.min(trace.length, 8);
                        for (int i = 0; i < limit; i++) {
                            out.println("    at " + trace[i]);
                        }
                        t = t.getCause();
                        if (t != null) {
                            out.println("Caused by:");
                        }
                    }
                %></pre>
            </details>
            <% } %>
        </div>
    </div>
</body>
</html>
