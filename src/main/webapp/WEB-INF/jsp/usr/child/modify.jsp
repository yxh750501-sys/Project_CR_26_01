<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.example.demo.vo.Child" %>
<!doctype html>
<html>
<head><meta charset="UTF-8"><title>Child Modify</title></head>
<body>
<%
  Child child = (Child) request.getAttribute("child");
%>

<h2>아이 프로필 수정</h2>

<form method="post" action="/usr/child/doModify">
  <input type="hidden" name="id" value="<%= child.getId() %>"/>

  <div>이름: <input name="name" value="<%= child.getName() %>" required></div>

  <div>생년월일:
    <input type="date" name="birthDate" value="<%= child.getBirthDate() == null ? "" : child.getBirthDate() %>">
  </div>

  <div>
    성별:
    <select name="gender">
      <option value="U" <%= "U".equals(child.getGender()) ? "selected" : "" %>>미정</option>
      <option value="M" <%= "M".equals(child.getGender()) ? "selected" : "" %>>남</option>
      <option value="F" <%= "F".equals(child.getGender()) ? "selected" : "" %>>여</option>
    </select>
  </div>

  <div>메모:<br>
    <textarea name="note" rows="5" cols="50"><%= child.getNote() == null ? "" : child.getNote() %></textarea>
  </div>

  <button type="submit">수정 저장</button>
</form>

<p><a href="/usr/child/list">목록</a></p>
</body>
</html>
