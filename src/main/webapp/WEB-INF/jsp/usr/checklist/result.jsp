<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>체크리스트 결과</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 20px; background: #f9f9f9; }
    .box { border: 1px solid #ddd; padding: 16px; border-radius: 10px; margin-bottom: 14px; background: #fff; }
    h2 { margin: 0 0 10px 0; }
    table { width: 100%; border-collapse: collapse; }
    th, td { border-bottom: 1px solid #eee; padding: 10px; text-align: left; vertical-align: top; }
    th { background: #fafafa; }
    .tag { display: inline-block; padding: 4px 10px; border: 1px solid #5b8dee; border-radius: 999px;
           margin: 0 6px 6px 0; font-size: 12px; color: #2d4a9a; background: #f0f4ff; }
    .muted { color: #777; }
    .small { font-size: 12px; }
    .empty { padding: 14px; color: #777; }
    .badge-low { color: #c0392b; font-weight: bold; }
    .topbar { margin-bottom: 10px; }
    .topbar a { color: #5b8dee; text-decoration: none; margin-right: 10px; font-size: 13px; }
  </style>
</head>
<body>

<div class="box">
  <h2>체크리스트 결과</h2>
  <div class="muted small">
    RunId: <c:out value="${runId}" /> /
    아이: <c:out value="${runInfo.childName}" /> /
    체크리스트: <c:out value="${runInfo.checklistTitle}" /> /
    생성일: <c:out value="${runInfo.createdAt}" />
  </div>
  <div class="topbar" style="margin-top:10px;">
    <a href="/usr/checklist/result-centers?runId=${runId}">▶ 추천 치료기관 보기</a>
    <a href="/usr/child/list">아이 목록</a>
  </div>
</div>

<div class="box">
  <h2>기능영역 점수 요약</h2>
  <div class="muted small" style="margin-bottom:8px;">※ 낮은 평균 점수 영역이 우선 지원 권장 대상입니다 (1=불가능 ~ 4=가능함)</div>

  <c:if test="${empty domainStats}">
    <div class="empty">도메인 점수 데이터가 없습니다. (문항에 답변 후 제출해 주세요)</div>
  </c:if>

  <c:if test="${not empty domainStats}">
    <table>
      <thead>
        <tr>
          <th style="width: 35%;">기능영역</th>
          <th style="width: 12%;">문항수</th>
          <th style="width: 12%;">합계</th>
          <th style="width: 12%;">평균</th>
          <th>비고</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${domainStats}" var="d">
          <tr>
            <td>
              <c:choose>
                <c:when test="${not empty domainLabelMap[d.domainCode]}">${domainLabelMap[d.domainCode]}</c:when>
                <c:otherwise><c:out value="${d.domainCode}" /></c:otherwise>
              </c:choose>
            </td>
            <td><c:out value="${d.cnt}" /></td>
            <td><c:out value="${d.sumScore}" /></td>
            <td>
              <fmt:formatNumber value="${d.avgScore}" maxFractionDigits="2" />
              <c:if test="${d.avgScore le 2.0}">
                <span class="badge-low">▲우선</span>
              </c:if>
            </td>
            <td class="muted small">
              <c:if test="${d.avgScore le 2.0}">우선 지원 권장</c:if>
              <c:if test="${d.avgScore gt 2.0 and d.avgScore le 3.0}">경계 수준</c:if>
              <c:if test="${d.avgScore gt 3.0}">양호</c:if>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </c:if>
</div>

<div class="box">
  <h2>우선 지원 권장 영역</h2>
  <c:if test="${empty recommendedDomains}">
    <div class="empty">추천 영역이 없습니다.</div>
  </c:if>
  <c:if test="${not empty recommendedDomains}">
    <c:forEach items="${recommendedDomains}" var="dc">
      <span class="tag">
        <c:choose>
          <c:when test="${not empty domainLabelMap[dc]}">${domainLabelMap[dc]}</c:when>
          <c:otherwise><c:out value="${dc}" /></c:otherwise>
        </c:choose>
      </span>
    </c:forEach>
  </c:if>
</div>

<div class="box">
  <h2>연관 치료기관</h2>

  <c:if test="${empty centers}">
    <div class="empty">
      연관 치료기관 정보가 없습니다.
      (domain_therapy_map 또는 center_services 데이터 확인 필요)
    </div>
  </c:if>

  <c:if test="${not empty centers}">
    <table>
      <thead>
        <tr>
          <th style="width: 25%;">센터명</th>
          <th style="width: 18%;">지역</th>
          <th>주소 / 연락처</th>
          <th style="width: 25%;">제공 치료</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${centers}" var="ctr">
          <tr>
            <td>
              <div><b><c:out value="${ctr.name}" /></b></div>
              <div class="muted small">ID: <c:out value="${ctr.id}" /></div>
            </td>
            <td class="small">
              <c:out value="${ctr.sido}" />
              <c:if test="${not empty ctr.sigungu}"> <c:out value="${ctr.sigungu}" /></c:if>
            </td>
            <td class="small">
              <div><c:out value="${ctr.address}" /></div>
              <c:if test="${not empty ctr.phone}">
                <div class="muted">☎ <c:out value="${ctr.phone}" /></div>
              </c:if>
              <c:if test="${not empty ctr.website}">
                <div class="muted">🌐 <c:out value="${ctr.website}" /></div>
              </c:if>
            </td>
            <td class="small">
              <%-- CenterRecommendMapper: therapyTypes 컬렉션 --%>
              <c:if test="${not empty ctr.therapyTypes}">
                <c:forEach items="${ctr.therapyTypes}" var="tt">
                  <span class="tag">
                    <c:choose>
                      <c:when test="${not empty therapyTypeLabelMap[tt.code]}">${therapyTypeLabelMap[tt.code]}</c:when>
                      <c:otherwise><c:out value="${tt.title}" /></c:otherwise>
                    </c:choose>
                  </span>
                </c:forEach>
              </c:if>
              <%-- ChecklistResultMapper: therapyTypeCodes (콤마 구분 문자열) --%>
              <c:if test="${empty ctr.therapyTypes and not empty ctr.therapyTypeCodes}">
                <c:forEach items="${fn:split(ctr.therapyTypeCodes, ',')}" var="tc">
                  <c:set var="tc" value="${fn:trim(tc)}" />
                  <span class="tag">
                    <c:choose>
                      <c:when test="${not empty therapyTypeLabelMap[tc]}">${therapyTypeLabelMap[tc]}</c:when>
                      <c:otherwise><c:out value="${tc}" /></c:otherwise>
                    </c:choose>
                  </span>
                </c:forEach>
              </c:if>
              <c:if test="${empty ctr.therapyTypes and empty ctr.therapyTypeCodes}">
                <span class="muted">정보 없음</span>
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
