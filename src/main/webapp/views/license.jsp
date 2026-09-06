<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý bản quyền - Thư viện số</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/views/_navbar.jsp" %>

<div class="container">
    <div class="card">
        <h1>${empty editLicense ? "Thêm bản quyền" : "Cập nhật bản quyền"}</h1>
        <form action="${pageContext.request.contextPath}/license/save" method="post"><c:if test="${not empty editLicense}"><input type="hidden" name="licenseId" value="${editLicense.licenseId}"></c:if>
            <div class="form-group">
                <label>Tài liệu</label>
                <select name="documentId" class="form-control" required>
                    <c:forEach var="doc" items="${documents}">
                        <option value="${doc.documentId}" ${not empty editLicense and editLicense.documentId == doc.documentId ? 'selected' : ''}>${doc.title}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group">
                <label>Loại bản quyền</label>
                <select name="licenseType" class="form-control">
                    <option value="FREE" ${editLicense.licenseType == 'FREE' ? 'selected' : ''}>FREE - Miễn phí</option>
                    <option value="SUBSCRIPTION" ${editLicense.licenseType == 'SUBSCRIPTION' ? 'selected' : ''}>SUBSCRIPTION - Thuê bao</option>
                    <option value="PAID" ${editLicense.licenseType == 'PAID' ? 'selected' : ''}>PAID - Trả phí</option>
                    <option value="INTERNAL" ${editLicense.licenseType == 'INTERNAL' ? 'selected' : ''}>INTERNAL - Nội bộ</option>
                </select>
            </div>
            <div class="form-group">
                <label>Mã bản quyền</label>
                <input type="text" name="licenseCode" class="form-control" placeholder="VD: LIC-0001" value="${editLicense.licenseCode}">
            </div>
            <div class="form-group">
                <label>Ngày cấp</label>
                <input type="date" name="issuedDate" class="form-control" value="${editLicense.issuedDate}">
            </div>
            <div class="form-group">
                <label>Ngày hết hạn</label>
                <input type="date" name="expiryDate" class="form-control" value="${editLicense.expiryDate}">
            </div>
            <div class="form-group">
                <label>Điều khoản sử dụng</label>
                <textarea name="terms" class="form-control" rows="3">${editLicense.terms}</textarea>
            </div>
            <div class="form-group">
                <label>Trạng thái</label>
                <select name="status" class="form-control">
                    <option value="VALID" ${empty editLicense.status or editLicense.status == 'VALID' ? 'selected' : ''}>VALID - Còn hiệu lực</option>
                    <option value="EXPIRED" ${editLicense.status == 'EXPIRED' ? 'selected' : ''}>EXPIRED - Hết hạn</option>
                    <option value="REVOKED" ${editLicense.status == 'REVOKED' ? 'selected' : ''}>REVOKED - Bị thu hồi</option>
                </select>
            </div>
            <button type="submit" class="btn btn-primary">Lưu bản quyền</button> <c:if test="${not empty editLicense}"><a href="${pageContext.request.contextPath}/license" class="btn btn-secondary">Hủy sửa</a></c:if>
        </form>
    </div>

    <div class="card">
        <h2>Danh sách bản quyền</h2>
        <c:choose>
            <c:when test="${empty licenses}">
                <p class="text-muted">Chưa có bản quyền nào được cấp.</p>
            </c:when>
            <c:otherwise>
                <table>
                    <thead>
                    <tr>
                        <th>Tài liệu</th>
                        <th>Loại</th>
                        <th>Mã</th>
                        <th>Ngày cấp</th>
                        <th>Hết hạn</th>
                        <th>Trạng thái</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="lic" items="${licenses}">
                        <tr>
                            <td>${lic.documentTitle}</td>
                            <td>${lic.licenseType}</td>
                            <td>${lic.licenseCode}</td>
                            <td>${lic.issuedDate}</td>
                            <td>${lic.expiryDate}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${lic.status == 'VALID'}">
                                        <span class="badge badge-valid">VALID</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-expired">${lic.status}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <a href="${pageContext.request.contextPath}/license/edit?id=${lic.licenseId}">Sửa</a> | <a href="${pageContext.request.contextPath}/license/delete?id=${lic.licenseId}"
                                   onclick="return confirm('Xóa bản quyền này?');" style="color:#dc2626;">Xóa</a>
                            </td>
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
