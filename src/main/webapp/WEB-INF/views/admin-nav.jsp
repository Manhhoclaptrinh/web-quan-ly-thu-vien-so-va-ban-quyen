<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="adminPath" value="${pageContext.request.requestURI}"/>
<aside class="admin-side">
  <div class="admin-brand">ADMIN PANEL</div>
  <div class="admin-sub">System Administration</div>
  <div class="admin-user"><strong>${sessionScope.currentUser.fullName}</strong><div class="admin-role">${sessionScope.currentUser.username} · ADMIN</div></div>
  <div class="admin-search"><input id="globalSearch" placeholder="Tìm người dùng, nội dung..." autocomplete="off"><div id="searchResults"></div></div>
  <nav class="admin-nav">
    <a class="${fn:endsWith(adminPath,'/admin') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin">🏠 Tổng quan</a>
    <a class="${fn:contains(adminPath,'/admin/users') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/users">👥 Người dùng</a>
    <a class="${fn:contains(adminPath,'/admin/content') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/content">📚 Nội dung</a>
    <a class="${fn:contains(adminPath,'/admin/permissions') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/permissions">🔐 Phân quyền</a>
    <a class="${fn:contains(adminPath,'/admin/reports') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/reports">📊 Báo cáo</a>
    <a class="${fn:contains(adminPath,'/admin/audit-logs') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/audit-logs">📋 Audit Logs</a>
    <a class="${fn:contains(adminPath,'/admin/security') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/security">🛡️ Bảo mật</a>
    <a class="${fn:contains(adminPath,'/admin/notifications') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/notifications">🔔 Thông báo <span class="nav-alert">${unreadNotifications}</span></a>
    <a class="${fn:contains(adminPath,'/admin/settings') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/settings">⚙️ Cài đặt</a>
  </nav>
  <div class="admin-footer"><a class="logout" href="${pageContext.request.contextPath}/logout">🚪 Đăng xuất</a></div>
</aside>
<script>
(function(){
 const input=document.getElementById('globalSearch'), box=document.getElementById('searchResults'); if(!input)return;
 let timer;
 input.addEventListener('input',function(){clearTimeout(timer);const q=this.value.trim();if(q.length<2){box.innerHTML='';box.style.display='none';return;}
 timer=setTimeout(()=>fetch('${pageContext.request.contextPath}/admin/search?q='+encodeURIComponent(q)).then(r=>r.json()).then(a=>{
 box.innerHTML=a.map(x=>'<div class="search-item"><span>'+x.type+'</span><b>'+x.name+'</b></div>').join('')||'<div class="search-empty">Không tìm thấy</div>';box.style.display='block';
 }).catch(()=>{}),180);});
 document.addEventListener('click',e=>{if(!e.target.closest('.admin-search'))box.style.display='none';});
})();
</script>
