<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html>
<html>
<head><meta charset="UTF-8"><title>Login</title></head>
<body>
<h2>로그인</h2>

<%
  String error = request.getParameter("error");
  if ("1".equals(error)) {
%>
  <p style="color:red;">아이디 또는 비밀번호가 올바르지 않습니다.</p>
<%
  }
%>

<form method="post" action="/usr/member/doLogin">
  <div>아이디: <input name="loginId"></div>
  <div>비밀번호: <input type="password" name="loginPw"></div>
  <button type="submit">로그인</button>
</form>

<p><a href="/usr/member/join">회원가입</a></p>
</body>
</html>
