<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo.vo.*" %>

<%
  List<Checklist> checklists = (List<Checklist>)request.getAttribute("checklists");
  if (checklists == null) checklists = new ArrayList<>();

  Object selectedChildIdObj = request.getAttribute("selectedChildId");
  if (selectedChildIdObj == null) selectedChildIdObj = session.getAttribute("selectedChildId");
  String selectedChildId = (selectedChildIdObj == null) ? "" : String.valueOf(selectedChildIdObj);
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>체크리스트 시작</title>
<style>
  body { font-family: Arial, sans-serif; }
  .card { margin:14px 0; padding:14px; border:1px solid #ddd; border-radius:12px; }
  .title { font-weight:800; margin-bottom:8px; }
  .small { color:#333; font-size:13px; line-height:1.55; white-space:pre-line; }
  .warn { color:#b00020; font-size:13px; white-space:pre-line; }
  .box { margin-top:10px; padding:12px; border-radius:12px; background:#f6f8ff; border:1px solid #dbe3ff; }
  .boxTitle { font-weight:800; color:#1f2a5a; margin-bottom:6px; }
  .bullet { margin:0; padding-left:18px; }
  .bullet li { margin:4px 0; }
  button { padding:9px 14px; border-radius:10px; border:1px solid #bbb; background:#fff; cursor:pointer; }
  button:hover { background:#f5f5f5; }
</style>
</head>
<body>

<h2>체크리스트 시작</h2>

<div class="card">
  <div class="title">AAC 보드가 뭔가요?</div>
  <div class="small">
AAC 보드는 “그림/사진/단어 카드”를 이용해서 아이가 말 대신 선택으로 의사표현할 수 있게 돕는 도구예요.
보호자/치료사가 ‘보기(선택지)’를 주고 아이는 ‘가리키기/응시/잡기’로 선택합니다.
  </div>

  <div class="box">
    <div class="boxTitle">아주 쉬운 사용 예시</div>
    <ul class="bullet">
      <li>준비: 간식 사진 2장(사과/과자)처럼 “선택지 2개”만 먼저 준비</li>
      <li>진행: “뭐 먹을래?” 하고 2장을 동시에 보여줌</li>
      <li>반응: 아이가 5초 안에 한쪽을 응시/가리키면 그 선택지를 “바로 제공”</li>
      <li>핵심: 선택 → 즉시 제공(일관성). 이게 쌓이면 AAC가 빨리 안정돼요.</li>
    </ul>
  </div>
</div>

<div class="card">
  <div class="title">답변 기준(애매함 제거)</div>

  <div class="box">
    <div class="boxTitle">라벨 기준(최근 7일 / 10번 기회)</div>
    <ul class="bullet">
      <li><b>불가능</b>: 0~1회</li>
      <li><b>어느정도 불가능</b>: 2~4회</li>
      <li><b>어느정도 가능</b>: 5~7회</li>
      <li><b>가능함</b>: 8~10회</li>
      <li><b>모름</b>: 관찰 부족/확신 없음(상황을 충분히 만들지 못함)</li>
    </ul>
  </div>

  <div class="small" style="margin-top:10px;">
예시(이름 부르기):
- 조용한 환경에서 이름 부르기 10번 기회 만들기
- “3초 내 반응”이 6번 → 어느정도 가능
- 1번 → 불가능
- 10번을 못 만들었으면 → 모름
  </div>
</div>

<div class="card">
  <div class="title">대표 아이</div>
  <% if (selectedChildId == null || selectedChildId.trim().isEmpty()) { %>
    <div class="warn">대표 아이가 아직 선택되지 않았습니다.
아이프로필 관리에서 대표설정 후 체크리스트를 시작하세요.</div>
    <p style="margin-top:10px;"><a href="/usr/child/list">아이 프로필 관리로</a></p>
  <% } else { %>
    <div class="small">현재 대표 아이 ID: <b><%= selectedChildId %></b></div>
  <% } %>
</div>

<h3>체크리스트 목록</h3>

<% if (checklists.isEmpty()) { %>
  <div class="card">체크리스트가 없습니다. (DB의 checklists / checklist_questions 확인)</div>
<% } else { %>
  <% for (Checklist c : checklists) { %>
    <div class="card">
      <div class="title"><%= c.getTitle() %></div>
      <div class="small"><%= (c.getDescription()==null ? "" : c.getDescription()) %></div>

      <form method="post" action="/usr/checklist/doStart" style="margin-top:10px;">
        <input type="hidden" name="checklistId" value="<%= c.getId() %>">
        <button type="submit">시작</button>
      </form>
    </div>
  <% } %>
<% } %>

<p><a href="/usr/child/list">아이 프로필 관리로</a></p>

</body>
</html>
