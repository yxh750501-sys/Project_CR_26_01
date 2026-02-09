<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo.vo.*" %>

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
	ChecklistRun run = (ChecklistRun) request.getAttribute("run");
	Checklist checklist = (Checklist) request.getAttribute("checklist");
	List<RunRecommendationItem> items = (List<RunRecommendationItem>) request.getAttribute("items");
	if (items == null) items = new ArrayList<>();
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>체크리스트 결과</title>
<style>
	.card { border: 1px solid #ddd; padding: 12px; margin: 10px 0; }
	.small { color: #666; font-size: 12px; }
	.badge { padding: 2px 8px; border-radius: 999px; background: #e3f2fd; display: inline-block; }
	ul { margin: 6px 0 0 18px; }
</style>
</head>
<body>

<h2>체크리스트 결과</h2>

<div class="card">
	<div><b><%= checklist == null ? "" : esc(checklist.getTitle()) %></b></div>
	<div class="small">
		status: <%= run == null ? "" : esc(run.getStatus()) %> /
		totalScore: <%= run == null ? "" : run.getTotalScore() %> /
		submittedDate: <%= run == null ? "" : esc(run.getSubmittedDate()) %>
	</div>
</div>

<div class="card">
	<div><b>추천</b></div>

	<% if (items.isEmpty()) { %>
		<div style="margin-top:8px;">현재 응답 기준으로 추천이 없습니다. (규칙을 추가하면 추천이 더 잘 나와요)</div>
	<% } else { %>
		<% for (RunRecommendationItem item : items) { %>
			<div class="card">
				<div>
					<span class="badge"><%= esc(item.getRecommendationCategory()) %></span>
					<b style="margin-left:6px;"><%= esc(item.getRecommendationTitle()) %></b>
				</div>
				<div style="margin-top:6px;"><%= esc(item.getRecommendationDescription()) %></div>

				<% if (item.getReasonText() != null && !item.getReasonText().isEmpty()) { %>
					<div class="small" style="margin-top:8px;">이유: <%= esc(item.getReasonText()) %></div>
				<% } %>

				<% if (item.getEvidences() != null && !item.getEvidences().isEmpty()) { %>
					<div class="small" style="margin-top:8px;">근거</div>
					<ul>
						<% for (RunRecommendationEvidence e : item.getEvidences()) { %>
							<li class="small"><%= esc(e.getEvidenceText()) %></li>
						<% } %>
					</ul>
				<% } %>
			</div>
		<% } %>
	<% } %>
</div>

<p>
	<a href="/usr/checklist/start">체크리스트 목록</a>
</p>

</body>
</html>
