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
	Object msgObj = request.getAttribute("msg");
	String msg = msgObj == null ? null : String.valueOf(msgObj);

	ChecklistRun run = (ChecklistRun) request.getAttribute("run");
	Checklist checklist = (Checklist) request.getAttribute("checklist");
	List<ChecklistQuestion> questions = (List<ChecklistQuestion>) request.getAttribute("questions");
	if (questions == null) questions = new ArrayList<>();

	Map<Long, ChecklistAnswer> answerMap = (Map<Long, ChecklistAnswer>) request.getAttribute("answerMap");
	if (answerMap == null) answerMap = new HashMap<>();
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>체크리스트 진행</title>
<style>
	.msg { margin: 10px 0; padding: 10px; background: #fff3cd; border: 1px solid #ffeeba; }
	.card { border: 1px solid #ddd; padding: 12px; margin: 10px 0; }
	.small { color: #666; font-size: 12px; }
	.row { margin-top: 8px; }
</style>
</head>
<body>

<h2>체크리스트 진행</h2>

<% if (msg != null && !msg.trim().isEmpty()) { %>
	<div class="msg"><%= esc(msg) %></div>
<% } %>

<div class="card">
	<div><b><%= checklist == null ? "" : esc(checklist.getTitle()) %></b></div>
	<div class="small">runId: <%= run == null ? "" : run.getId() %> / status: <%= run == null ? "" : esc(run.getStatus()) %> / totalScore: <%= run == null ? "" : run.getTotalScore() %></div>
</div>

<form method="post" action="/usr/checklist/doPost">
	<input type="hidden" name="runId" value="<%= run == null ? "" : run.getId() %>"/>

	<% for (ChecklistQuestion q : questions) {
		ChecklistAnswer a = answerMap.get(q.getId());
		String av = a == null ? "" : (a.getAnswerValue() == null ? "" : a.getAnswerValue());
		String at = a == null ? "" : (a.getAnswerText() == null ? "" : a.getAnswerText());
	%>

	<div class="card">
		<div><b><%= esc(q.getCode()) %>. <%= esc(q.getQuestionText()) %></b></div>
		<% if (q.getHelpText() != null && !q.getHelpText().isEmpty()) { %>
			<div class="small"><%= esc(q.getHelpText()) %></div>
		<% } %>

		<div class="row">
			<% if ("YN".equalsIgnoreCase(q.getResponseType())) { %>
				<label>
					<input type="radio" name="answer_<%= q.getId() %>" value="Y" <%= "Y".equalsIgnoreCase(av) ? "checked" : "" %> />
					Y
				</label>
				<label style="margin-left:12px;">
					<input type="radio" name="answer_<%= q.getId() %>" value="N" <%= "N".equalsIgnoreCase(av) ? "checked" : "" %> />
					N
				</label>
			<% } else if ("SCALE5".equalsIgnoreCase(q.getResponseType())) { %>
				<select name="answer_<%= q.getId() %>">
					<option value="" <%= av.isEmpty() ? "selected" : "" %>>선택</option>
					<option value="1" <%= "1".equals(av) ? "selected" : "" %>>1</option>
					<option value="2" <%= "2".equals(av) ? "selected" : "" %>>2</option>
					<option value="3" <%= "3".equals(av) ? "selected" : "" %>>3</option>
					<option value="4" <%= "4".equals(av) ? "selected" : "" %>>4</option>
					<option value="5" <%= "5".equals(av) ? "selected" : "" %>>5</option>
				</select>
			<% } else { %>
				<input type="hidden" name="answer_<%= q.getId() %>" value=""/>
			<% } %>
		</div>

		<div class="row">
			<textarea name="text_<%= q.getId() %>" rows="3" style="width:100%;" placeholder="메모(선택)"><%= esc(at) %></textarea>
		</div>
	</div>

	<% } %>

	<div style="margin-top:12px;">
		<button type="submit" name="action" value="save">저장</button>
		<button type="submit" name="action" value="submit" onclick="return confirm('제출하면 수정이 어려울 수 있어요. 제출할까요?');">제출</button>
		<a href="/usr/checklist/start" style="margin-left:10px;">체크리스트 목록</a>
	</div>
</form>

</body>
</html>
