<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html>
<html>
<head><meta charset="UTF-8"><title>Join</title></head>
<body>
<h2>회원가입</h2>

<form method="post" action="/usr/member/doJoin">
  <div>아이디: <input name="loginId"></div>
  <div>비밀번호: <input type="password" name="loginPw"></div>

  <div>
    역할:
    <select name="role">
      <option value="GUARDIAN">보호자</option>
      <option value="THERAPIST">치료사</option>
    </select>
  </div>

  <button type="submit">가입</button>
</form>

<p><a href="/usr/member/login">로그인</a></p>
</body>
</html>
