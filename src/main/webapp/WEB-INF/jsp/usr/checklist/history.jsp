<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo.vo.*" %>

<%
  List<ChecklistRun> runs = (List<ChecklistRun>)request.getAttribute("runs");
  if (runs == null) runs = new ArrayList<>();
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>체크리스트 히스토리</title>
<style>
  body { font-family: Arial, sans-serif; }
  .card { margin:14px 0; padding:14px; border:1px solid #ddd; border-radius:12px; }
  .title { font-weight:900; margin-bottom:6px; }
  table { width:100%; border-collapse:collapse; }
  th, td { padding:10px; border-bottom:1px solid #eee; text-align:left; }
  a { text-decoration:none; }
</style>
</head>
<body>

<h2>체크리스트 히스토리(대표 아이)</h2>

<div class="card">
  <div class="title">지난 제출 결과</div>

  <% if (runs.isEmpty()) { %>
    <div>제출된 결과가 없습니다.</div>
  <% } else { %>
    <table>
      <thead>
        <tr>
          <th>제출일</th>
          <th>총점</th>
          <th>상태</th>
          <th>보기</th>
        </tr>
      </thead>
      <tbody>
        <% for (ChecklistRun r : runs) { %>
          <tr>
            <td><%= r.getSubmittedDate() %></td>
            <td><%= r.getTotalScore() %></td>
            <td><%= r.getStatus() %></td>
            <td><a href="/usr/checklist/result?runId=<%= r.getId() %>">결과 보기</a></td>
          </tr>
        <% } %>
      </tbody>
    </table>
  <% } %>
</div>

<p>
  <a href="/usr/checklist/start">체크리스트 목록</a> |
  <a href="/usr/child/list">아이 프로필 관리</a>
</p>

</body>
</html>
