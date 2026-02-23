<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>체크리스트 결과 + 센터 추천</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 20px; }
    .box { border: 1px solid #ddd; padding: 14px; border-radius: 10px; margin-bottom: 14px; }
    h2 { margin: 0 0 10px 0; }
    h3 { margin: 0 0 10px 0; }
    table { width:100%; border-collapse: collapse; }
    th, td { border-bottom:1px solid #eee; padding:10px; text-align:left; vertical-align: top; }
    th { background:#fafafa; }
    .muted { color:#777; font-size:12px; }
    .pill { display:inline-block; padding:4px 10px; border:1px solid #ddd; border-radius:999px; margin:4px 6px 0 0; }
  </style>
</head>
<body>

<div class="box">
  <h2>체크리스트 결과 + 센터 추천</h2>
  <div class="muted">runId: <c:out value="${runId}" /></div>
</div>

<div class="box">
  <h3>도메인 점수</h3>
  <table>
    <thead>
      <tr>
        <th>도메인</th>
        <th>문항 수</th>
        <th>점수 합계</th>
        <th>평균 점수</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach items="${domainStats}" var="d">
        <tr>
          <td>
            <c:choose>
              <c:when test="${d.domainCode eq 'COMMUNICATION'}">의사소통</c:when>
              <c:when test="${d.domainCode eq 'SENSORY_DAILY'}">감각·일상</c:when>
              <c:when test="${d.domainCode eq 'BEHAVIOR_EMOTION'}">행동·정서</c:when>
              <c:when test="${d.domainCode eq 'MOTOR_FINE'}">운동·미세</c:when>
              <c:when test="${d.domainCode eq 'PLAY_SOCIAL'}">놀이·사회성</c:when>
              <c:otherwise><c:out value="${d.domainCode}" /></c:otherwise>
            </c:choose>
            <span class="muted">(<c:out value="${d.domainCode}" />)</span>
          </td>
          <td><c:out value="${d.cnt}" /></td>
          <td><c:out value="${d.sumScore}" /></td>
          <td><c:out value="${d.avgScore}" /></td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<div class="box">
  <h3>추천 도메인 / 치료타입</h3>

  <div style="margin-bottom:10px;">
    <b>추천 도메인:</b>
    <c:forEach items="${topDomains}" var="x">
      <span class="pill">
        <c:choose>
          <c:when test="${x eq 'COMMUNICATION'}">의사소통</c:when>
          <c:when test="${x eq 'SENSORY_DAILY'}">감각·일상</c:when>
          <c:when test="${x eq 'BEHAVIOR_EMOTION'}">행동·정서</c:when>
          <c:when test="${x eq 'MOTOR_FINE'}">운동·미세</c:when>
          <c:when test="${x eq 'PLAY_SOCIAL'}">놀이·사회성</c:when>
          <c:otherwise><c:out value="${x}" /></c:otherwise>
        </c:choose>
        <span class="muted">(<c:out value="${x}" />)</span>
      </span>
    </c:forEach>
  </div>

  <div>
    <b>추천 치료/프로그램:</b>
    <c:forEach items="${therapyTypeCodes}" var="t">
      <span class="pill">
        <c:choose>
          <c:when test="${t eq 'SPEECH_THERAPY'}">언어치료</c:when>
          <c:when test="${t eq 'AAC_COACHING'}">AAC 코칭/도구 세팅</c:when>
          <c:when test="${t eq 'OT_SENSORY'}">작업치료(감각·일상)</c:when>
          <c:when test="${t eq 'OT_FINE'}">작업치료(미세·협응)</c:when>
          <c:when test="${t eq 'ABA_PARENT'}">행동상담/부모코칭(ABA)</c:when>
          <c:when test="${t eq 'PLAY_THERAPY'}">놀이치료/사회성</c:when>
          <c:when test="${t eq 'PSY_COUNSEL'}">심리/정서 상담</c:when>
          <c:otherwise><c:out value="${t}" /></c:otherwise>
        </c:choose>
        <span class="muted">(<c:out value="${t}" />)</span>
      </span>
    </c:forEach>
  </div>
</div>

<div class="box">
  <h3>센터 목록</h3>

  <c:if test="${empty centers}">
    <div>조건에 맞는 센터가 없습니다. (현재는 샘플 데이터만 있을 수 있어요)</div>
  </c:if>

  <c:if test="${not empty centers}">
    <table>
      <thead>
        <tr>
          <th>센터명</th>
          <th>지역</th>
          <th>주소</th>
          <th>연락처</th>
          <th>제공 치료/프로그램</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${centers}" var="c">
          <tr>
            <td><c:out value="${c.name}" /></td>
            <td><c:out value="${c.sido}" /> <c:out value="${c.sigungu}" /></td>
            <td><c:out value="${c.address}" /></td>
            <td><c:out value="${c.phone}" /></td>
            <td>
              <c:if test="${empty c.therapyTypeCodes}">
                <span class="muted">정보 없음</span>
              </c:if>

              <c:if test="${not empty c.therapyTypeCodes}">
                <c:forEach items="${fn:split(c.therapyTypeCodes, ',')}" var="tc">
                  <span class="pill">
                    <c:choose>
                      <c:when test="${tc eq 'SPEECH_THERAPY'}">언어치료</c:when>
                      <c:when test="${tc eq 'AAC_COACHING'}">AAC 코칭/도구 세팅</c:when>
                      <c:when test="${tc eq 'OT_SENSORY'}">작업치료(감각·일상)</c:when>
                      <c:when test="${tc eq 'OT_FINE'}">작업치료(미세·협응)</c:when>
                      <c:when test="${tc eq 'ABA_PARENT'}">행동상담/부모코칭(ABA)</c:when>
                      <c:when test="${tc eq 'PLAY_THERAPY'}">놀이치료/사회성</c:when>
                      <c:when test="${tc eq 'PSY_COUNSEL'}">심리/정서 상담</c:when>
                      <c:otherwise><c:out value="${tc}" /></c:otherwise>
                    </c:choose>
                    <span class="muted">(<c:out value="${tc}" />)</span>
                  </span>
                </c:forEach>
              </c:if>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </c:if>
</div>

</body>
</html>