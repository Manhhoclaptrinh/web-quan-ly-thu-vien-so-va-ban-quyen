<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết tài liệu - Thư viện số</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/views/_navbar.jsp" %>

<div class="container">
    <c:choose>
        <%-- Chế độ THÊM/SỬA: có biến "categories" nghĩa là đang ở form thêm/sửa --%>
        <c:when test="${not empty categories}">
            <div class="card">
                <h1>${not empty document ? 'Cập nhật tài liệu' : 'Thêm tài liệu mới'}</h1>
                <c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
                <form action="${pageContext.request.contextPath}/documents/save" method="post" enctype="multipart/form-data">
                    <c:if test="${not empty document}">
                        <input type="hidden" name="documentId" value="${document.documentId}">
                    </c:if>

                    <div class="form-group">
                        <label>Tên tài liệu</label>
                        <input type="text" name="title" class="form-control" required value="${document.title}">
                    </div>
                    <div class="form-group">
                        <label>Tác giả</label>
                        <input type="text" name="author" class="form-control" value="${document.author}">
                    </div>
                    <div class="form-group">
                        <label>Danh mục</label>
                        <select name="categoryId" class="form-control" required>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat.categoryId}" ${cat.categoryId == document.categoryId ? 'selected' : ''}>${cat.categoryName}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>File tài liệu</label>
                        <input type="file" name="documentFile" class="form-control" accept=".pdf,.doc,.docx,.txt">
                        <c:if test="${not empty document.filePath}"><small class="text-muted">File hiện tại: ${document.filePath}. Chọn file mới nếu muốn thay thế.</small></c:if>
                    </div>
                    <div class="form-group">
                        <label>Mô tả</label>
                        <textarea name="description" class="form-control" rows="4">${document.description}</textarea>
                    </div>
                    <div class="form-group">
                        <label>Mức truy cập</label>
                        <select name="accessLevel" class="form-control">
                            <option value="PUBLIC" ${document.accessLevel == 'PUBLIC' ? 'selected' : ''}>PUBLIC - Công khai</option>
                            <option value="RESTRICTED" ${document.accessLevel == 'RESTRICTED' ? 'selected' : ''}>RESTRICTED - Giới hạn</option>
                            <option value="PRIVATE" ${document.accessLevel == 'PRIVATE' ? 'selected' : ''}>PRIVATE - Riêng tư</option>
                        </select>
                    </div>

                    <button type="submit" class="btn btn-primary">Lưu</button>
                    <a href="${pageContext.request.contextPath}/documents" class="btn btn-secondary">Hủy</a>
                </form>
            </div>
        </c:when>

        <%-- Chế độ XEM CHI TIẾT --%>
        <c:otherwise>
            <div class="card">
                <h1>${document.title}</h1>
                <p class="text-muted">Tác giả: ${document.author}  •  Danh mục: ${document.categoryName}</p>

                <table class="mt-3">
                    <tr><th style="width:180px;">Mức truy cập</th><td>${document.accessLevel}</td></tr>
                    <tr><th>Trạng thái</th><td>${document.status}</td></tr>
                    <tr><th>Người tải lên</th><td>${document.uploaderName}</td></tr>
                    <tr><th>Ngày tải lên</th><td>${document.uploadDate}</td></tr>
                    <tr><th>Mô tả</th><td>${document.description}</td></tr>
                </table>

                <div class="card mt-3" style="background:#f8fafc">
                    <h2>© Thông tin bản quyền</h2>
                    <c:choose>
                        <c:when test="${empty licenses}"><p class="text-muted">Tài liệu chưa có bản quyền được khai báo.</p></c:when>
                        <c:otherwise>
                            <c:forEach var="lic" items="${licenses}">
                                <table><tr><th>Loại</th><td>${lic.licenseType}</td></tr><tr><th>Mã bản quyền</th><td>${lic.licenseCode}</td></tr><tr><th>Ngày cấp</th><td>${lic.issuedDate}</td></tr><tr><th>Ngày hết hạn</th><td>${empty lic.expiryDate ? 'Không giới hạn' : lic.expiryDate}</td></tr><tr><th>Trạng thái</th><td>${lic.status}</td></tr><tr><th>Điều khoản</th><td>${lic.terms}</td></tr></table>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
                <c:if test="${not empty licenseValid and not licenseValid}"><div class="alert alert-error">Bản quyền tài liệu đã hết hạn hoặc bị thu hồi. READER không thể truy cập tài liệu.</div></c:if>

                <div class="card mt-3" style="background:#f8fafc">
                    <div class="flex-between">
                        <div><h2>⭐ Yêu thích</h2><p class="text-muted">Lưu tài liệu để truy cập nhanh từ mục Yêu thích.</p></div>
                        <form action="${pageContext.request.contextPath}/favorites" method="post">
                            <input type="hidden" name="documentId" value="${document.documentId}">
                            <c:choose>
                                <c:when test="${isFavorite}"><input type="hidden" name="action" value="remove"><button class="btn btn-secondary" type="submit">★ Bỏ yêu thích</button></c:when>
                                <c:otherwise><input type="hidden" name="action" value="add"><button class="btn btn-primary" type="submit">☆ Thêm yêu thích</button></c:otherwise>
                            </c:choose>
                        </form>
                    </div>
                </div>
                <section class="card mt-3" id="reviews">
                    <div class="flex-between"><div><h2>⭐ Đánh giá tài liệu</h2><p class="text-muted">Điểm trung bình: <strong>${averageRating}</strong>/5 · ${reviews.size()} đánh giá</p></div></div>
                    <form action="${pageContext.request.contextPath}/reviews" method="post" class="review-form mt-3">
                        <input type="hidden" name="documentId" value="${document.documentId}">
                        <div class="form-group"><label>Điểm đánh giá</label><select name="rating" class="form-control" required>
                            <option value="5">5 - Rất tốt</option><option value="4">4 - Tốt</option><option value="3">3 - Khá</option><option value="2">2 - Trung bình</option><option value="1">1 - Chưa tốt</option>
                        </select></div>
                        <div class="form-group"><label>Nhận xét</label><textarea name="comment" class="form-control" rows="3" placeholder="Nhận xét của bạn...">${myReview.comment}</textarea></div>
                        <button class="btn btn-primary" type="submit">${empty myReview ? 'Gửi đánh giá' : 'Cập nhật đánh giá'}</button>
                    </form>
                    <div class="review-list mt-3"><c:forEach var="review" items="${reviews}">
                        <div class="review-item"><div class="flex-between"><strong>${review.userFullName}</strong><span>⭐ ${review.rating}/5</span></div><p>${review.comment}</p><small class="text-muted">${review.createdAt}</small></div>
                    </c:forEach></div>
                </section>

                <div class="mt-3">
                    <c:if test="${param.error == 'no-view-permission'}">
                        <div class="alert alert-error">Bạn chưa có quyền VIEW tài liệu này. Hãy gửi yêu cầu cấp quyền.</div>
                    </c:if>
                    <c:if test="${canView and fn:toLowerCase(document.filePath).endsWith('.pdf')}">
                        <a href="${pageContext.request.contextPath}/documents/preview?id=${document.documentId}" target="_blank" rel="noopener" class="btn btn-secondary">Xem PDF</a>
                    </c:if>
                    <c:choose>
                        <c:when test="${canDownload}">
                            <a href="${pageContext.request.contextPath}/documents/download?id=${document.documentId}" class="btn btn-primary">Tải xuống</a>
                        </c:when>
                        <c:otherwise>
                            <p class="text-muted">Bạn không có quyền tải xuống tài liệu này.</p>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${canRequestView or canRequestDownload}">
                        <a href="${pageContext.request.contextPath}/permission-request?documentId=${document.documentId}&permissionType=${canRequestView ? 'VIEW' : 'DOWNLOAD'}" class="btn btn-secondary">Xin cấp quyền</a>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/documents" class="btn btn-secondary">Quay lại</a>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>
</body>
</html>
