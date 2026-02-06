<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo.vo.Child" %>
<!doctype html>
<html>
<head><meta charset="UTF-8"><title>Child List</title></head>
<body>
<h2>아이 프로필 목록</h2>

<p>
  <a href="/usr/child/write">+ 아이 프로필 추가</a>
  |
  <a href="/usr/member/me">마이페이지</a>
</p>

<%
  List<Child> children = (List<Child>) request.getAttribute("children");
  if (children == null || children.size() == 0) {
%>
  <p>등록된 아이 프로필이 없습니다.</p>
<%
  } else {
%>
  <table border="1" cellpadding="8" cellspacing="0">
    <tr>
      <th>ID</th>
      <th>이름</th>
      <th>생년월일</th>
      <th>성별</th>
      <th>관리</th>
    </tr>
<%
    for (Child c : children) {
%>
    <tr>
      <td><%= c.getId() %></td>
      <td><%= c.getName() %></td>
      <td><%= c.getBirthDate() %></td>
      <td><%= c.getGender() %></td>
      <td>
        <a href="/usr/child/modify?id=<%= c.getId() %>">수정</a>
        <form method="post" action="/usr/child/doDelete" style="display:inline;">
          <input type="hidden" name="id" value="<%= c.getId() %>"/>
          <button type="submit" onclick="return confirm('삭제할까요?');">삭제</button>
        </form>
      </td>
    </tr>
<%
    }
%>
  </table>
<%
  }
%>

</body>
</html>
