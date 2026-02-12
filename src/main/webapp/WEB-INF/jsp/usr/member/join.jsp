<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	Object msgObj = request.getAttribute("msg");
	String msg = msgObj == null ? null : String.valueOf(msgObj);
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원가입</title>
<style>
  .msg { margin:10px 0; padding:10px; background:#fff3cd; border:1px solid #ffeeba; }
</style>
</head>
<body>

<h2>회원가입</h2>

<% if (msg != null && !msg.trim().isEmpty()) { %>
  <div class="msg"><%= msg %></div>
<% } %>

<form method="post" action="/usr/member/doJoin">
  <div>
    <input name="loginId" placeholder="아이디" />
  </div>
  <div>
    <input type="password" name="loginPw" placeholder="비밀번호" />
  </div>
  <div>
    <select name="role">
      <option value="GUARDIAN">GUARDIAN</option>
      <option value="THERAPIST">THERAPIST</option>
    </select>
  </div>
  <button type="submit">가입</button>
</form>

<p><a href="/usr/member/login">로그인</a></p>

</body>
</html>
