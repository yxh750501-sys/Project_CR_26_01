<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html>
<html>
<head><meta charset="UTF-8"><title>My Page</title></head>
<body>
<h2>마이페이지</h2>

<%
  Object uid = session.getAttribute("loginedUserId");
  Object role = session.getAttribute("loginedUserRole");
%>

<div>로그인 userId: <b><%= uid %></b></div>
<div>role: <b><%= role %></b></div>

<form method="post" action="/usr/member/doLogout">
  <button type="submit">로그아웃</button>
</form>
</body>
</html>
