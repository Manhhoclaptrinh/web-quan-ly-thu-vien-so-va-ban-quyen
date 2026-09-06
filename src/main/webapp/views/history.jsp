<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Lịch sử truy cập - Thư viện số</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/views/_navbar.jsp" %>

<div class="container">
    <div class="card">
        <h1>Lịch sử truy cập</h1>
        <p class="text-muted">
            <c:choose>
                <c:when test="${sessionScope.currentUser.role == 'ADMIN' or sessionScope.currentUser.role == 'LIBRARIAN'}">
                    Toàn bộ lịch sử truy cập hệ thống.
                </c:when>
                <c:otherwise>
                    Lịch sử truy cập của bạn.
                </c:otherwise>
            </c:choose>
        </p>
        <form action="${pageContext.request.contextPath}/history" method="get" class="advanced-search mt-3">
<c:if test="${sessionScope.currentUser.role == 'ADMIN' or sessionScope.currentUser.role == 'LIBRARIAN'}"><input type="number" name="userId" class="form-control" placeholder="User ID" value="${selectedUserId}"></c:if>
<input type="number" name="documentId" class="form-control" placeholder="Document ID" value="${selectedDocumentId}">
<select name="actionType" class="form-control"><option value="">-- Tất cả hành động --</option><option value="LOGIN" ${selectedAction=='LOGIN'?'selected':''}>LOGIN</option><option value="LOGOUT" ${selectedAction=='LOGOUT'?'selected':''}>LOGOUT</option><option value="VIEW" ${selectedAction=='VIEW'?'selected':''}>VIEW</option><option value="DOWNLOAD" ${selectedAction=='DOWNLOAD'?'selected':''}>DOWNLOAD</option></select>
<label class="date-field">Từ ngày <input type="date" name="fromDate" value="${fromDate}"></label><label class="date-field">Đến ngày <input type="date" name="toDate" value="${toDate}"></label>
<button type="submit" class="btn btn-secondary">Lọc</button><a href="${pageContext.request.contextPath}/history" class="btn btn-secondary">Xóa lọc</a></form>

        <c:choose>
            <c:when test="${empty historyList}">
                <p class="text-muted mt-3">Chưa có lịch sử truy cập nào.</p>
            </c:when>
            <c:otherwise>
                <table class="mt-3">
                    <thead>
                    <tr>
                        <th>Người dùng</th>
                        <th>Tài liệu</th>
                        <th>Hành động</th>
                        <th>Thời gian</th>
                        <th>Địa chỉ IP</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="h" items="${historyList}">
                        <tr>
                            <td>${h.username}</td>
                            <td>${empty h.documentTitle ? '-' : h.documentTitle}</td>
                            <td>${h.actionType}</td>
                            <td>${h.accessTime}</td>
                            <td>${h.ipAddress}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </div>
</div>
</body>
</html>
