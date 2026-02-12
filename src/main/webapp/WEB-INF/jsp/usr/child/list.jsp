<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo.vo.Child" %>

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

	boolean needSelect = Boolean.TRUE.equals(request.getAttribute("needSelect"));

	Object sidObj = request.getAttribute("selectedChildId");
	Long selectedChildId = null;
	if (sidObj instanceof Number) {
		selectedChildId = ((Number) sidObj).longValue();
	}

	Child selectedChild = (Child) request.getAttribute("selectedChild");

	List<Child> children = (List<Child>) request.getAttribute("children");
	if (children == null) children = new ArrayList<>();
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>아이 프로필 목록</title>
<style>
	table { border-collapse: collapse; width: 100%; }
	th, td { border: 1px solid #ddd; padding: 8px; vertical-align: top; }
	th { background: #f7f7f7; }
	.badge { padding: 2px 8px; border-radius: 999px; background: #e8f5e9; display: inline-block; }
	.msg { margin: 10px 0; padding: 10px; background: #fff3cd; border: 1px solid #ffeeba; }
	.actions form { display: inline; margin: 0; }
	.topbar { display:flex; gap:10px; align-items:center; margin: 10px 0; }
	.btnlink { display:inline-block; padding:6px 10px; border:1px solid #ddd; background:#fff; text-decoration:none; border-radius:6px; }
</style>
</head>
<body>

<h2>아이 프로필 관리</h2>

<% if (msg != null && !msg.trim().isEmpty()) { %>
	<div class="msg"><%= esc(msg) %></div>
<% } %>

<% if (needSelect) { %>
	<div class="msg">체크리스트/결과 기능을 사용하려면 대표 아이를 먼저 선택해 주세요.</div>
<% } %>

<% if (selectedChild != null) { %>
	<div class="msg">
		현재 대표 아이: <b><%= esc(selectedChild.getName()) %></b>
		<% if (selectedChild.getBirthDate() != null && !selectedChild.getBirthDate().isEmpty()) { %>
			(생년월일: <%= esc(selectedChild.getBirthDate()) %>)
		<% } %>
	</div>
<% } %>

<div class="topbar">
	<a class="btnlink" href="/usr/child/write">아이 프로필 등록</a>
	<a class="btnlink" href="/usr/checklist/start">체크리스트 시작</a>
	<a class="btnlink" href="/usr/member/me">마이페이지</a>
</div>

<table>
	<thead>
		<tr>
			<th>ID</th>
			<th>이름</th>
			<th>생년월일</th>
			<th>성별</th>
			<th>메모</th>
			<th>대표</th>
			<th>관리</th>
		</tr>
	</thead>
	<tbody>
	<%
		if (children.isEmpty()) {
	%>
		<tr>
			<td colspan="7">등록된 아이가 없습니다.</td>
		</tr>
	<%
		} else {
			for (Child child : children) {
				boolean isSelected = (selectedChildId != null && selectedChildId.longValue() == child.getId());
	%>
		<tr>
			<td><%= child.getId() %></td>
			<td><%= esc(child.getName()) %></td>
			<td><%= esc(child.getBirthDate()) %></td>
			<td><%= esc(child.getGender()) %></td>
			<td><%= esc(child.getNote()) %></td>
			<td>
				<% if (isSelected) { %>
					<span class="badge">대표</span>
				<% } else { %>
					<form method="post" action="/usr/child/doSelect">
						<input type="hidden" name="id" value="<%= child.getId() %>"/>
						<button type="submit">대표 선택</button>
					</form>
				<% } %>
			</td>
			<td class="actions">
				<a href="/usr/child/modify?id=<%= child.getId() %>">수정</a>
				<form method="post" action="/usr/child/doDelete" onsubmit="return confirm('삭제할까요?');">
					<input type="hidden" name="id" value="<%= child.getId() %>"/>
					<button type="submit">삭제</button>
				</form>
			</td>
		</tr>
	<%
			}
		}
	%>
	</tbody>
</table>

<p style="margin-top:12px;">
	체크리스트 진입 주소: <b>/usr/checklist/start</b>
</p>

</body>
</html>
