<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết video - Thư viện số</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/views/_navbar.jsp" %>

<div class="container">
    <c:choose>
        <%-- Chế độ THÊM/SỬA: có biến "categories" nghĩa là đang ở form thêm/sửa --%>
        <c:when test="${not empty categories}">
            <div class="card">
                <h1>${not empty video ? 'Cập nhật video' : 'Thêm video mới'}</h1>
                <c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
                <form action="${pageContext.request.contextPath}/videos/save" method="post" enctype="multipart/form-data">
                    <c:if test="${not empty video}">
                        <input type="hidden" name="videoId" value="${video.videoId}">
                    </c:if>

                    <div class="form-group">
                        <label>Tên video</label>
                        <input type="text" name="title" class="form-control" required value="${video.title}">
                    </div>
                    <div class="form-group">
                        <label>Tác giả</label>
                        <input type="text" name="author" class="form-control" value="${video.author}">
                    </div>
                    <div class="form-group">
                        <label>Danh mục</label>
                        <select name="categoryId" class="form-control" required>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat.categoryId}" ${cat.categoryId == video.categoryId ? 'selected' : ''}>${cat.categoryName}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>File video</label>
                        <input type="file" name="videoFile" class="form-control" accept=".mp4,.webm,.ogg,.mov,.mkv">
                        <small class="text-muted">Định dạng: MP4, WEBM, OGG, MOV, MKV. Dung lượng tối đa 1GB.</small>
                        <c:if test="${not empty video.filePath}"><br><small class="text-muted">File hiện tại: ${video.filePath}. Chọn file mới nếu muốn thay thế.</small></c:if>
                    </div>
                    <div class="form-group">
                        <label>Mô tả</label>
                        <textarea name="description" class="form-control" rows="4">${video.description}</textarea>
                    </div>
                    <div class="form-group">
                        <label>Mức truy cập</label>
                        <select name="accessLevel" class="form-control">
                            <option value="PUBLIC" ${video.accessLevel == 'PUBLIC' ? 'selected' : ''}>PUBLIC - Công khai</option>
                            <option value="RESTRICTED" ${video.accessLevel == 'RESTRICTED' ? 'selected' : ''}>RESTRICTED - Giới hạn</option>
                            <option value="PRIVATE" ${video.accessLevel == 'PRIVATE' ? 'selected' : ''}>PRIVATE - Riêng tư</option>
                        </select>
                    </div>

                    <button type="submit" class="btn btn-primary">Lưu</button>
                    <a href="${pageContext.request.contextPath}/videos" class="btn btn-secondary">Hủy</a>
                </form>
            </div>
        </c:when>

        <%-- Chế độ XEM CHI TIẾT --%>
        <c:otherwise>
            <div class="card">
                <h1>${video.title}</h1>
                <p class="text-muted">Tác giả: ${video.author}  •  Danh mục: ${video.categoryName}</p>

                <c:if test="${param.error == 'no-view-permission'}">
                    <div class="alert alert-error">Bạn chưa có quyền VIEW video này. Hãy gửi yêu cầu cấp quyền.</div>
                </c:if>

                <c:choose>
                    <c:when test="${canView}">
                        <video class="mt-3" controls preload="metadata" style="width:100%;max-height:480px;background:#000"
                               src="${pageContext.request.contextPath}/videos/stream?id=${video.videoId}">
                            Trình duyệt của bạn không hỗ trợ thẻ video HTML5.
                        </video>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state mt-3">🔒<strong>Chưa có quyền xem</strong><span>Bạn cần được cấp quyền VIEW để phát video này.</span></div>
                    </c:otherwise>
                </c:choose>

                <table class="mt-3">
                    <tr><th style="width:180px;">Mức truy cập</th><td>${video.accessLevel}</td></tr>
                    <tr><th>Trạng thái</th><td>${video.status}</td></tr>
                    <tr><th>Người tải lên</th><td>${video.uploaderName}</td></tr>
                    <tr><th>Ngày tải lên</th><td>${video.uploadDate}</td></tr>
                    <tr><th>Mô tả</th><td>${video.description}</td></tr>
                </table>

                <div class="card mt-3" style="background:#f8fafc">
                    <h2>© Thông tin bản quyền</h2>
                    <c:choose>
                        <c:when test="${empty licenses}"><p class="text-muted">Video chưa có bản quyền được khai báo.</p></c:when>
                        <c:otherwise>
                            <c:forEach var="lic" items="${licenses}">
                                <table><tr><th>Loại</th><td>${lic.licenseType}</td></tr><tr><th>Mã bản quyền</th><td>${lic.licenseCode}</td></tr><tr><th>Ngày cấp</th><td>${lic.issuedDate}</td></tr><tr><th>Ngày hết hạn</th><td>${empty lic.expiryDate ? 'Không giới hạn' : lic.expiryDate}</td></tr><tr><th>Trạng thái</th><td>${lic.status}</td></tr><tr><th>Điều khoản</th><td>${lic.terms}</td></tr></table>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
                <c:if test="${not empty licenseValid and not licenseValid}"><div class="alert alert-error">Bản quyền video đã hết hạn hoặc bị thu hồi. READER không thể truy cập video.</div></c:if>

                <div class="card mt-3" style="background:#f8fafc">
                    <div class="flex-between">
                        <div><h2>⭐ Yêu thích</h2><p class="text-muted">Lưu video để truy cập nhanh từ mục Yêu thích.</p></div>
                        <form action="${pageContext.request.contextPath}/favorites" method="post">
                            <input type="hidden" name="itemType" value="VIDEO">
                            <input type="hidden" name="videoId" value="${video.videoId}">
                            <c:choose>
                                <c:when test="${isFavorite}"><input type="hidden" name="action" value="remove"><button class="btn btn-secondary" type="submit">★ Bỏ yêu thích</button></c:when>
                                <c:otherwise><input type="hidden" name="action" value="add"><button class="btn btn-primary" type="submit">☆ Thêm yêu thích</button></c:otherwise>
                            </c:choose>
                        </form>
                    </div>
                </div>
                <section class="card mt-3" id="reviews">
                    <div class="flex-between"><div><h2>⭐ Đánh giá video</h2><p class="text-muted">Điểm trung bình: <strong>${averageRating}</strong>/5 · ${reviews.size()} đánh giá</p></div></div>
                    <form action="${pageContext.request.contextPath}/video-reviews" method="post" class="review-form mt-3">
                        <input type="hidden" name="videoId" value="${video.videoId}">
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
                    <c:choose>
                        <c:when test="${canDownload}">
                            <a href="${pageContext.request.contextPath}/videos/download?id=${video.videoId}" class="btn btn-primary">Tải xuống</a>
                        </c:when>
                        <c:otherwise>
                            <p class="text-muted">Bạn không có quyền tải xuống video này.</p>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${canRequestView or canRequestDownload}">
                        <a href="${pageContext.request.contextPath}/video-permission-request?videoId=${video.videoId}&permissionType=${canRequestView ? 'VIEW' : 'DOWNLOAD'}" class="btn btn-secondary">Xin cấp quyền</a>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/videos" class="btn btn-secondary">Quay lại</a>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>
</body>
</html>
