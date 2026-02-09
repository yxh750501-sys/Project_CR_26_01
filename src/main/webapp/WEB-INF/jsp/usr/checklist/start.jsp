<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo.vo.Checklist" %>

<%!
	private String esc(String s) {
		if (s == null) return "";
		return s.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
%>

<%
	Object msgObj = request.getAttribute("msg");
	String msg = msgObj == null ? null : String.valueOf(msgObj);

	Object childObj = request.getAttribute("selectedChildId");
	String selectedChildId = childObj == null ? "" : String.valueOf(childObj);

	List<Checklist> checklists = (List<Checklist>) request.getAttribute("checklists");
	if (checklists == null) checklists = new ArrayList<>();
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>체크리스트 시작</title>
<style>
	.msg { margin: 10px 0; padding: 10px; background: #fff3cd; border: 1px solid #ffeeba; }
	table { border-collapse: collapse; width: 100%; }
	th, td { border: 1px solid #ddd; padding: 8px; vertical-align: top; }
	th { background: #f7f7f7; }
</style>
</head>
<body>

<h2>체크리스트 시작</h2>

<% if (msg != null && !msg.trim().isEmpty()) { %>
	<div class="msg"><%= esc(msg) %></div>
<% } %>

<p>
	대표 아이 ID: <b><%= esc(selectedChildId) %></b>
	&nbsp;|&nbsp;
	<a href="/usr/child/list">아이 프로필 관리</a>
</p>

<table>
	<thead>
		<tr>
			<th>코드</th>
			<th>제목</th>
			<th>설명</th>
			<th>시작</th>
		</tr>
	</thead>
	<tbody>
	<% if (checklists.isEmpty()) { %>
		<tr><td colspan="4">등록된 체크리스트가 없습니다.</td></tr>
	<% } else { %>
		<% for (Checklist c : checklists) { %>
			<tr>
				<td><%= esc(c.getCode()) %></td>
				<td><%= esc(c.getTitle()) %></td>
				<td><%= esc(c.getDescription()) %></td>
				<td>
					<form method="post" action="/usr/checklist/doStart">
						<input type="hidden" name="checklistId" value="<%= c.getId() %>"/>
						<button type="submit">시작</button>
					</form>
				</td>
			</tr>
		<% } %>
	<% } %>
	</tbody>
</table>

</body>
</html>
