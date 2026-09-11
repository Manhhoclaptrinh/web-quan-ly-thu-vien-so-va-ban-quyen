<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${not empty categories ? (not empty book ? 'Cập nhật sách' : 'Thêm sách') : 'Chi tiết sách'} - Thư viện số</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/views/_navbar.jsp" %>
<div class="container">
<c:choose>
<c:when test="${not empty categories}">
    <div class="card">
        <h1>${not empty book ? 'Cập nhật sách' : 'Thêm sách mới'}</h1>
        <c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
        <form action="${pageContext.request.contextPath}/books/save" method="post" enctype="multipart/form-data">
            <c:if test="${not empty book}"><input type="hidden" name="bookId" value="${book.bookId}"></c:if>
            <div class="form-group"><label>Tên sách</label><input type="text" name="title" class="form-control" required value="${book.title}"></div>
            <div class="form-group"><label>Tác giả</label><input type="text" name="author" class="form-control" value="${book.author}"></div>
            <div class="form-group"><label>ISBN</label><input type="text" name="isbn" class="form-control" value="${book.isbn}"></div>
            <div class="form-group"><label>Nhà xuất bản</label><input type="text" name="publisher" class="form-control" value="${book.publisher}"></div>
            <div class="form-group"><label>Năm xuất bản</label><input type="number" name="publishYear" class="form-control" min="0" max="2100" value="${book.publishYear}"></div>
            <div class="form-group"><label>Danh mục</label><select name="categoryId" class="form-control" required>
                <c:forEach var="cat" items="${categories}"><option value="${cat.categoryId}" ${cat.categoryId == book.categoryId ? 'selected' : ''}>${cat.categoryName}</option></c:forEach>
            </select></div>
            <div class="form-group"><label>Tổng số bản</label><input type="number" name="totalCopies" class="form-control" min="1" required value="${empty book ? 1 : book.totalCopies}"></div>
            <div class="form-group"><label>File sách</label><input type="file" name="bookFile" class="form-control" accept=".pdf,.doc,.docx,.txt" ${empty book ? 'required' : ''}>
                <small class="text-muted">PDF, DOC, DOCX, TXT. File mới chỉ cần chọn khi muốn thay thế.</small>
                <c:if test="${not empty book.filePath}"><br><small class="text-muted">File hiện tại: ${book.filePath}</small></c:if>
            </div>
            <div class="form-group"><label>Mô tả</label><textarea name="description" class="form-control" rows="4">${book.description}</textarea></div>
            <div class="form-group"><label>Mức truy cập</label><select name="accessLevel" class="form-control">
                <option value="PUBLIC" ${empty book.accessLevel or book.accessLevel == 'PUBLIC' ? 'selected' : ''}>PUBLIC - Công khai</option>
                <option value="RESTRICTED" ${book.accessLevel == 'RESTRICTED' ? 'selected' : ''}>RESTRICTED - Giới hạn</option>
                <option value="PRIVATE" ${book.accessLevel == 'PRIVATE' ? 'selected' : ''}>PRIVATE - Riêng tư</option>
            </select></div>
            <button type="submit" class="btn btn-primary">Lưu</button>
            <a href="${pageContext.request.contextPath}/books" class="btn btn-secondary">Hủy</a>
        </form>
    </div>
</c:when>
<c:otherwise>
    <div class="card">
        <div class="flex-between"><div><h1>📚 ${book.title}</h1><p class="text-muted">${book.author} · ${book.publisher}</p></div><a href="${pageContext.request.contextPath}/books" class="btn btn-secondary">← Danh sách</a></div>
        <c:if test="${param.error == 'no-view-permission'}"><div class="alert alert-error">Bạn chưa có quyền VIEW sách này.</div></c:if>

        <table class="mt-3">
            <tr><th style="width:180px">ISBN</th><td>${book.isbn}</td></tr>
            <tr><th>Nhà xuất bản</th><td>${book.publisher}</td></tr>
            <tr><th>Năm xuất bản</th><td>${book.publishYear}</td></tr>
            <tr><th>Danh mục</th><td>${book.categoryName}</td></tr>
            <tr><th>Số bản</th><td>${book.availableCopies}/${book.totalCopies} còn lại</td></tr>
            <tr><th>Mức truy cập</th><td>${book.accessLevel}</td></tr>
            <tr><th>Trạng thái</th><td>${book.status}</td></tr>
            <tr><th>Người tải lên</th><td>${book.uploaderName}</td></tr>
            <tr><th>Mô tả</th><td>${book.description}</td></tr>
        </table>

        <c:if test="${canView}">
            <c:choose>
                <c:when test="${fn:endsWith(book.filePath, '.pdf')}">
                    <div class="card mt-3" style="padding:0;overflow:hidden">
                        <iframe src="${pageContext.request.contextPath}/books/preview?id=${book.bookId}" title="Xem trước sách" style="width:100%;height:700px;border:0"></iframe>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert-success mt-3">Bạn có quyền xem thông tin sách. File này không hỗ trợ preview trực tiếp trên trình duyệt.</div>
                </c:otherwise>
            </c:choose>
        </c:if>
        <c:if test="${not canView}">
            <div class="empty-state mt-3">🔒<strong>Chưa có quyền xem file</strong><span>Bạn có thể gửi yêu cầu VIEW nếu tài liệu yêu cầu cấp quyền.</span></div>
        </c:if>

        <div class="flex-between mt-3">
            <div>
                <c:if test="${canDownload and not isAuditor}"><a href="${pageContext.request.contextPath}/books/download?id=${book.bookId}" class="btn btn-primary">⬇ Tải sách</a></c:if>
                <c:if test="${canRequestView and not isAuditor}"><a href="${pageContext.request.contextPath}/book-permission-request?bookId=${book.bookId}&permissionType=VIEW" class="btn btn-secondary">🔐 Xin quyền VIEW</a></c:if>
            </div>
            <c:if test="${not isAuditor and canView and canRequestDownload}">
                <a href="${pageContext.request.contextPath}/book-permission-request?bookId=${book.bookId}&permissionType=DOWNLOAD" class="btn btn-secondary">Xin quyền DOWNLOAD</a>
            </c:if>
        </div>

        <div class="card mt-3" style="background:#f8fafc">
            <h2>© Thông tin bản quyền</h2>
            <c:choose><c:when test="${empty licenses}"><p class="text-muted">Sách chưa có bản quyền được khai báo.</p></c:when>
            <c:otherwise><c:forEach var="lic" items="${licenses}">
                <table><tr><th>Loại</th><td>${lic.licenseType}</td></tr><tr><th>Mã</th><td>${lic.licenseCode}</td></tr><tr><th>Ngày cấp</th><td>${lic.issuedDate}</td></tr><tr><th>Ngày hết hạn</th><td>${empty lic.expiryDate ? 'Không giới hạn' : lic.expiryDate}</td></tr><tr><th>Trạng thái</th><td>${lic.status}</td></tr><tr><th>Điều khoản</th><td>${lic.terms}</td></tr></table>
            </c:forEach></c:otherwise></c:choose>
        </div>

        <c:if test="${not isAuditor}">
            <div class="card mt-3" style="background:#f8fafc">
                <div class="flex-between"><div><h2>⭐ Yêu thích</h2><p class="text-muted">Lưu sách để truy cập nhanh.</p></div>
                    <form action="${pageContext.request.contextPath}/favorites" method="post">
                        <input type="hidden" name="itemType" value="BOOK"><input type="hidden" name="bookId" value="${book.bookId}">
                        <c:choose><c:when test="${isFavorite}"><input type="hidden" name="action" value="remove"><button class="btn btn-secondary">★ Bỏ yêu thích</button></c:when>
                        <c:otherwise><input type="hidden" name="action" value="add"><button class="btn btn-primary">☆ Thêm yêu thích</button></c:otherwise></c:choose>
                    </form>
                </div>
            </div>
        </c:if>

        <section class="card mt-3" id="reviews">
            <div class="flex-between"><div><h2>⭐ Đánh giá sách</h2><p class="text-muted">Điểm trung bình: <strong>${averageRating}</strong>/5 · ${reviews.size()} đánh giá</p></div></div>
            <c:if test="${not isAuditor}">
                <form action="${pageContext.request.contextPath}/book-reviews" method="post" class="review-form mt-3">
                    <input type="hidden" name="bookId" value="${book.bookId}">
                    <div class="form-group"><label>Điểm đánh giá</label><select name="rating" class="form-control" required><option value="5">5 - Rất tốt</option><option value="4">4 - Tốt</option><option value="3">3 - Khá</option><option value="2">2 - Trung bình</option><option value="1">1 - Chưa tốt</option></select></div>
                    <div class="form-group"><label>Nhận xét</label><textarea name="comment" class="form-control" rows="3">${myReview.comment}</textarea></div>
                    <button class="btn btn-primary" type="submit">${empty myReview ? 'Gửi đánh giá' : 'Cập nhật đánh giá'}</button>
                </form>
            </c:if>
            <div class="review-list mt-3">
                <c:forEach var="review" items="${reviews}">
                    <div class="review-item"><div class="flex-between"><strong>${review.userFullName}</strong><span>⭐ ${review.rating}/5</span></div><p>${review.comment}</p><small class="text-muted">${review.createdAt}</small></div>
                </c:forEach>
                <c:if test="${empty reviews}"><p class="text-muted">Chưa có đánh giá.</p></c:if>
            </div>
        </section>
    </div>
</c:otherwise>
</c:choose>
</div>
</body>
</html>
