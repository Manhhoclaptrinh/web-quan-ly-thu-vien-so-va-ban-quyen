<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Admin - Nội dung</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css"></head>
<body class="admin-body"><div class="admin-shell"><%@ include file="/WEB-INF/views/admin-nav.jsp" %><main class="admin-main">
<header class="admin-head"><div><div class="eyebrow">CONTENT EDITOR</div><h1>${empty item ? 'Thêm' : 'Chỉnh sửa'} ${type}</h1><p>Admin có quyền tạo, cập nhật và thay đổi trạng thái nội dung.</p></div><a class="btn" href="${pageContext.request.contextPath}/admin/content">← Quay lại</a></header>
<section class="panel"><form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/admin"><input type="hidden" name="action" value="content-save"><input type="hidden" name="type" value="${type}"><input type="hidden" name="id" value="${type=='DOCUMENT'?item.documentId:type=='VIDEO'?item.videoId:item.bookId}">
<div class="form-grid">
<label>Tiêu đề<input name="title" value="${item.title}" required></label><label>Tác giả<input name="author" value="${item.author}"></label>
<label>Danh mục<select name="categoryId" required><c:forEach var="c" items="${categories}"><option value="${c.categoryId}" ${item.categoryId==c.categoryId?'selected':''}>${c.categoryName}</option></c:forEach></select></label>
<label>Quyền truy cập<select name="accessLevel"><option ${item.accessLevel=='PUBLIC'?'selected':''}>PUBLIC</option><option ${item.accessLevel=='RESTRICTED'?'selected':''}>RESTRICTED</option><option ${item.accessLevel=='PRIVATE'?'selected':''}>PRIVATE</option></select></label>
<label>Trạng thái<select name="status"><option ${empty item.status||item.status=='AVAILABLE'?'selected':''}>AVAILABLE</option><option ${item.status=='DISABLED'?'selected':''}>DISABLED</option></select></label>
<c:if test="${type=='BOOK'}"><label>ISBN<input name="isbn" value="${item.isbn}"></label><label>Nhà xuất bản<input name="publisher" value="${item.publisher}"></label><label>Năm xuất bản<input type="number" name="publishYear" value="${item.publishYear}"></label><label>Tổng số bản<input type="number" name="totalCopies" min="1" value="${empty item.totalCopies?1:item.totalCopies}"></label></c:if>
<label class="full">Mô tả<textarea name="description" rows="5">${item.description}</textarea></label>
<label class="full">File ${empty item ? '(bắt buộc)' : '(để trống nếu giữ file cũ)'}<input type="file" name="contentFile" ${empty item?'required':''}><small>Document/Book: PDF, DOC, DOCX, TXT · Video: MP4, WEBM, MOV · tối đa 50MB.</small></label>
</div><button class="btn primary">💾 Lưu nội dung</button></form></section>
</main></div></body></html>
