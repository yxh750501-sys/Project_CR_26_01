<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	Object idObj = request.getAttribute("loginedUserId");
	String uid = idObj == null ? "-" : String.valueOf(idObj);
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>마이페이지</title>
</head>
<body>

<h2>마이페이지</h2>
<p>로그인 사용자 ID: <b><%= uid %></b></p>

<form method="post" action="/usr/member/doLogout">
  <button type="submit">로그아웃</button>
</form>

<p><a href="/usr/child/list">아이 프로필 관리</a></p>

</body>
</html>
