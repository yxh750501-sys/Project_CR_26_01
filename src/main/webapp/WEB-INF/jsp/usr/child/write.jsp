<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html>
<html>
<head><meta charset="UTF-8"><title>Child Write</title></head>
<body>
<h2>아이 프로필 등록</h2>

<form method="post" action="/usr/child/doWrite">
  <div>이름: <input name="name" required></div>
  <div>생년월일: <input type="date" name="birthDate"></div>
  <div>
    성별:
    <select name="gender">
      <option value="U">미정</option>
      <option value="M">남</option>
      <option value="F">여</option>
    </select>
  </div>
  <div>메모:<br>
    <textarea name="note" rows="5" cols="50"></textarea>
  </div>
  <button type="submit">저장</button>
</form>

<p><a href="/usr/child/list">목록</a></p>
</body>
</html>
