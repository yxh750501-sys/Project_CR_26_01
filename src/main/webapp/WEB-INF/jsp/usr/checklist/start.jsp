<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>체크리스트 작성</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 20px; }
    .box { border: 1px solid #ddd; padding: 14px; border-radius: 10px; margin-bottom: 14px; }
    h2 { margin: 0 0 10px 0; }
    .muted { color: #777; font-size: 12px; }
    .q { padding: 12px 0; border-bottom: 1px solid #eee; }
    .q:last-child { border-bottom: none; }
    .help { color:#666; font-size:12px; margin-top:6px; }
    .opts label { margin-right: 12px; }
    .btn { padding: 10px 14px; border: 1px solid #333; background:#fff; border-radius: 10px; cursor:pointer; }
    .btnRow { display:flex; gap:10px; margin-top:14px; }
  </style>
</head>
<body>

<c:if test="${param.saved eq '1'}">
  <script>alert('임시저장 완료');</script>
</c:if>

<div class="box">
  <h2><c:out value="${checklist.title}" /></h2>
  <div class="muted">
    runId: <c:out value="${runId}" /> /
    checklistId: <c:out value="${checklist.id}" /> /
    childId: <c:out value="${childId}" />
  </div>
  <c:if test="${not empty checklist.description}">
    <div class="help"><c:out value="${checklist.description}" /></div>
  </c:if>
</div>

<form class="box" action="/usr/checklist/doSubmit" method="post">
  <input type="hidden" name="runId" value="${runId}" />
  <input type="hidden" name="checklistId" value="${checklist.id}" />

  <c:forEach items="${questions}" var="q">
    <!-- ★ 핵심: questionId를 문자열로 만든 뒤 answersMap(StringKey)에서 꺼낸다 -->
    <fmt:formatNumber value="${q.id}" groupingUsed="false" var="qidStr" />
    <c:set var="ans" value="${answersMap[qidStr]}" />

    <div class="q">
      <div><b><c:out value="${q.sortOrder}" />.</b> <c:out value="${q.questionText}" /></div>

      <c:if test="${not empty q.helpText}">
        <div class="help"><c:out value="${q.helpText}" /></div>
      </c:if>

      <c:choose>
        <c:when test="${q.responseType eq 'YN'}">
          <div class="opts">
            <label>
              <input type="radio" name="q_${q.id}" value="Y"
                <c:if test="${ans.answerValue eq 'Y'}">checked</c:if>
              > 예
            </label>
            <label>
              <input type="radio" name="q_${q.id}" value="N"
                <c:if test="${ans.answerValue eq 'N'}">checked</c:if>
              > 아니오
            </label>
          </div>
        </c:when>

        <c:when test="${q.responseType eq 'SCALE5'}">
          <div class="opts">
            <label><input type="radio" name="q_${q.id}" value="1" <c:if test="${ans.answerValue eq '1'}">checked</c:if> > 1</label>
            <label><input type="radio" name="q_${q.id}" value="2" <c:if test="${ans.answerValue eq '2'}">checked</c:if> > 2</label>
            <label><input type="radio" name="q_${q.id}" value="3" <c:if test="${ans.answerValue eq '3'}">checked</c:if> > 3</label>
            <label><input type="radio" name="q_${q.id}" value="4" <c:if test="${ans.answerValue eq '4'}">checked</c:if> > 4</label>
            <label><input type="radio" name="q_${q.id}" value="5" <c:if test="${ans.answerValue eq '5'}">checked</c:if> > 5</label>
          </div>
        </c:when>

        <c:otherwise>
          <div class="opts">
            <textarea name="q_${q.id}" rows="3" style="width:100%;"><c:out value="${ans.answerText}" /></textarea>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </c:forEach>

  <div class="btnRow">
    <button class="btn" type="submit" formaction="/usr/checklist/doSave">임시저장</button>
    <button class="btn" type="submit">제출</button>
  </div>
</form>

</body>
</html>