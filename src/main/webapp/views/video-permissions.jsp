<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Phân quyền video - Thư viện số</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/views/_navbar.jsp" %>

<div class="container">
    <div class="card">
        <c:if test="${isViewer}"><h1>🔐 Danh sách phân quyền</h1><p class="text-muted">Viewer / Read-only Admin chỉ được xem quyền đã cấp.</p></c:if>
        <c:if test="${not isViewer}"><h1>Cấp quyền truy cập video</h1>
        <form action="${pageContext.request.contextPath}/video-permission/save" method="post">
            <div class="form-group">
                <label>Người dùng</label>
                <select name="userId" class="form-control" required>
                    <c:forEach var="u" items="${users}">
                        <option value="${u.userId}">${u.fullName} (${u.username})</option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group">
                <label>Video</label>
                <select name="videoId" class="form-control" required>
                    <c:forEach var="v" items="${videos}">
                        <option value="${v.videoId}">${v.title}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group">
                <label>Loại quyền</label>
                <select name="permissionType" class="form-control">
                    <option value="VIEW">VIEW - Xem</option>
                    <option value="DOWNLOAD">DOWNLOAD - Tải xuống</option>
                    <option value="EDIT">EDIT - Chỉnh sửa</option>
                </select>
            </div>
            <div class="form-group">
                <label>Hết hạn (để trống nếu không giới hạn)</label>
                <input type="datetime-local" name="expiryDate" class="form-control">
            </div>
            <button type="submit" class="btn btn-primary">Cấp quyền</button>
        </form></c:if>
    </div>

    <div class="card">
        <h2>Danh sách quyền video đã cấp</h2>
        <c:choose>
            <c:when test="${empty permissions}">
                <p class="text-muted">Chưa có quyền video nào được cấp.</p>
            </c:when>
            <c:otherwise>
                <table>
                    <thead>
                    <tr>
                        <th>Người dùng</th>
                        <th>Video</th>
                        <th>Loại quyền</th>
                        <th>Cấp bởi</th>
                        <th>Ngày cấp</th>
                        <th>Hết hạn</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="p" items="${permissions}">
                        <tr>
                            <td>${p.username}</td>
                            <td>${p.videoTitle}</td>
                            <td>${p.permissionType}</td>
                            <td>${p.grantedByName}</td>
                            <td>${p.grantedDate}</td>
                            <td>${empty p.expiryDate ? 'Không giới hạn' : p.expiryDate}</td>
                            <td><c:choose><c:when test="${isViewer}"><span class="text-muted">Chỉ xem</span></c:when><c:otherwise>
                                <a href="${pageContext.request.contextPath}/video-permission/delete?id=${p.permissionId}"
                                   onclick="return confirm('Thu hồi quyền video này?');" style="color:#dc2626;">Thu hồi</a>
                            </c:otherwise></c:choose></td>
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
