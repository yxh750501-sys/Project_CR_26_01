<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo.vo.*" %>

<%
  ChecklistRun run = (ChecklistRun)request.getAttribute("run");
  List<ChecklistQuestion> questions = (List<ChecklistQuestion>)request.getAttribute("questions");
  Map<Long, ChecklistAnswer> answersMap = (Map<Long, ChecklistAnswer>)request.getAttribute("answersMap");

  if (questions == null) questions = new ArrayList<>();
  if (answersMap == null) answersMap = new HashMap<>();
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>체크리스트 진행</title>
<style>
  body { font-family: Arial, sans-serif; }
  .card { margin:16px 0; padding:14px; border:1px solid #ddd; border-radius:12px; }
  .qtitle { font-weight:700; }
  .help { color:#333; margin-top:8px; line-height:1.45; white-space:pre-line; }
  .meta { color:#777; font-size:12px; margin-top:6px; }
  select { padding:9px; min-width:260px; border-radius:10px; border:1px solid #bbb; }
  textarea { padding:9px; width:100%; max-width:920px; border-radius:10px; border:1px solid #bbb; }
  button { padding:10px 16px; border-radius:10px; border:1px solid #bbb; background:#fff; cursor:pointer; }
  button:hover { background:#f5f5f5; }
</style>
</head>
<body>

<h2>체크리스트</h2>

<div class="card">
  <div>Run ID: <b><%= (run == null ? "-" : run.getId()) %></b></div>
  <div class="meta">선택지는 라벨로만 선택합니다: 불가능 / 거의 불가능 / 거의 가능 / 가능 / 모름</div>
</div>

<form method="post" action="/usr/checklist/doSubmit">
  <input type="hidden" name="runId" value="<%= (run == null ? "" : run.getId()) %>">

  <% for (ChecklistQuestion q : questions) {
       ChecklistAnswer a = answersMap.get(q.getId());
       String av = (a == null || a.getAnswerValue()==null) ? "" : a.getAnswerValue();
       String at = (a == null || a.getAnswerText()==null) ? "" : a.getAnswerText();
       String rt = (q.getResponseType()==null) ? "" : q.getResponseType().trim().toUpperCase();
       boolean isText = "TEXT".equals(rt);
  %>

    <div class="card">
      <div class="qtitle"><%= q.getCode() %>. <%= q.getQuestionText() %></div>

      <% if (q.getHelpText() != null && !q.getHelpText().trim().isEmpty()) { %>
        <div class="help"><%= q.getHelpText() %></div>
      <% } %>

      <div class="meta">응답형: <%= rt %></div>

      <% if (isText) { %>
        <div style="margin-top:10px;">
          <textarea name="t_<%= q.getId() %>" rows="4" placeholder="구체 상황을 그대로 작성(언제/어디서/무엇 때문에/어떻게)"><%= at %></textarea>
        </div>
      <% } else { %>
        <div style="margin-top:10px;">
          <select name="v_<%= q.getId() %>">
            <option value="">선택</option>

            <!-- 값(value)은 기존 로직 깨지지 않게 1~5 유지, 화면 텍스트만 라벨 -->
            <option value="1" <%= "1".equals(av) ? "selected" : "" %>>불가능</option>
            <option value="2" <%= "2".equals(av) ? "selected" : "" %>>거의 불가능</option>
            <option value="3" <%= "3".equals(av) ? "selected" : "" %>>모름</option>
            <option value="4" <%= "4".equals(av) ? "selected" : "" %>>거의 가능</option>
            <option value="5" <%= "5".equals(av) ? "selected" : "" %>>가능</option>
          </select>
        </div>

        <div style="margin-top:10px;">
          <textarea name="t_<%= q.getId() %>" rows="2" placeholder="메모(선택): 관찰된 실제 예시를 1~2줄로 적어주세요."><%= at %></textarea>
        </div>
      <% } %>
    </div>

  <% } %>

  <button type="submit">제출</button>
</form>

<p style="margin-top:16px;"><a href="/usr/checklist/start">체크리스트 목록으로</a></p>

</body>
</html>
