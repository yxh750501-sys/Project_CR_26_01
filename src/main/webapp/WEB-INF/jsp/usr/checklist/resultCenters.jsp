<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>결과 + 치료기관 추천</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 20px; background: #f9f9f9; }
    .box { border: 1px solid #ddd; padding: 16px; border-radius: 10px; margin-bottom: 14px; background: #fff; }
    h2 { margin: 0 0 10px 0; }
    h3 { margin: 0 0 8px 0; font-size: 15px; }
    table { width: 100%; border-collapse: collapse; }
    th, td { border-bottom: 1px solid #eee; padding: 10px; text-align: left; vertical-align: top; }
    th { background: #fafafa; }
    .muted { color: #777; font-size: 12px; }
    .small { font-size: 12px; }
    .pill { display: inline-block; padding: 4px 10px; border: 1px solid #5b8dee;
            border-radius: 999px; margin: 3px 5px 3px 0; font-size: 12px;
            color: #2d4a9a; background: #f0f4ff; }
    .pill-green { border-color: #27ae60; color: #1a6b3c; background: #eafaf1; }
    .badge-low { color: #c0392b; font-weight: bold; font-size: 11px; }
    .therapy-block { border: 1px solid #d6e4ff; border-radius: 8px; padding: 12px; margin-bottom: 12px; }
    .therapy-block h4 { margin: 0 0 8px 0; color: #2d4a9a; font-size: 14px; }
    .empty { padding: 12px; color: #777; }
    .topbar a { color: #5b8dee; text-decoration: none; margin-right: 12px; font-size: 13px; }
  </style>
</head>
<body>

<div class="box">
  <h2>체크리스트 결과 &amp; 치료기관 추천</h2>
  <div class="muted">runId: <c:out value="${runId}" /></div>
  <div class="topbar" style="margin-top:8px;">
    <a href="/usr/checklist/result?runId=${runId}">상세 결과 보기</a>
    <a href="/usr/child/list">아이 목록</a>
    <a href="/usr/checklist/start">새 체크리스트</a>
  </div>
</div>

<%-- ① 도메인 점수 요약 --%>
<div class="box">
  <h2>기능영역 점수 요약</h2>
  <div class="muted" style="margin-bottom:8px;">※ 낮은 점수 영역 = 우선 지원 권장 (1=불가능 ~ 4=가능함)</div>

  <c:if test="${empty domainStats}">
    <div class="empty">도메인 점수 데이터가 없습니다.</div>
  </c:if>

  <c:if test="${not empty domainStats}">
    <table>
      <thead>
        <tr>
          <th>기능영역</th>
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
                <c:when test="${not empty domainLabelMap[d.domainCode]}">${domainLabelMap[d.domainCode]}</c:when>
                <c:otherwise><c:out value="${d.domainCode}" /></c:otherwise>
              </c:choose>
              <c:if test="${d.avgScore le 2.0}">
                <span class="badge-low"> ▲우선</span>
              </c:if>
            </td>
            <td><c:out value="${d.cnt}" /></td>
            <td><c:out value="${d.sumScore}" /></td>
            <td><fmt:formatNumber value="${d.avgScore}" maxFractionDigits="2" /></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </c:if>
</div>

<%-- ② 추천 도메인 + 치료타입 --%>
<div class="box">
  <h2>추천 영역 &amp; 치료/프로그램</h2>

  <div style="margin-bottom: 10px;">
    <b>우선 지원 권장 영역:</b>
    <c:if test="${empty topDomains}">
      <span class="muted">데이터 없음</span>
    </c:if>
    <c:forEach items="${topDomains}" var="x">
      <span class="pill">
        <c:choose>
          <c:when test="${not empty domainLabelMap[x]}">${domainLabelMap[x]}</c:when>
          <c:otherwise><c:out value="${x}" /></c:otherwise>
        </c:choose>
      </span>
    </c:forEach>
  </div>

  <div>
    <b>추천 치료/프로그램:</b>
    <c:if test="${empty therapyTypeCodes}">
      <span class="muted">데이터 없음 (domain_therapy_map 시드 확인)</span>
    </c:if>
    <c:forEach items="${therapyTypeCodes}" var="t">
      <span class="pill pill-green">
        <c:choose>
          <c:when test="${not empty therapyTypeLabelMap[t]}">${therapyTypeLabelMap[t]}</c:when>
          <c:otherwise><c:out value="${t}" /></c:otherwise>
        </c:choose>
      </span>
    </c:forEach>
  </div>
</div>

<%-- ③ 치료타입별 센터 블록 (BeforeActionInterceptor 주입: therapyCenterBlocks) --%>
<c:if test="${not empty therapyCenterBlocks}">
  <div class="box">
    <h2>치료타입별 기관 추천</h2>
    <c:forEach items="${therapyCenterBlocks}" var="block">
      <div class="therapy-block">
        <h4>
          <c:choose>
            <c:when test="${not empty therapyTypeLabelMap[block.therapyTypeCode]}">${therapyTypeLabelMap[block.therapyTypeCode]}</c:when>
            <c:otherwise><c:out value="${block.therapyTitle}" /></c:otherwise>
          </c:choose>
          <span class="muted"> ← ${block.domainTitle}</span>
        </h4>

        <c:if test="${empty block.centers}">
          <div class="empty">조건에 맞는 기관이 없습니다.</div>
        </c:if>

        <c:if test="${not empty block.centers}">
          <table>
            <thead>
              <tr>
                <th>기관명</th>
                <th>지역</th>
                <th>주소 / 연락처</th>
                <th>서비스</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach items="${block.centers}" var="ctr">
                <tr>
                  <td><b><c:out value="${ctr.centerName}" /></b></td>
                  <td class="small">
                    <c:out value="${ctr.sido}" />
                    <c:if test="${not empty ctr.sigungu}"> <c:out value="${ctr.sigungu}" /></c:if>
                    <c:if test="${not empty ctr.distanceKm}">
                      <div class="muted"><fmt:formatNumber value="${ctr.distanceKm}" maxFractionDigits="1" />km</div>
                    </c:if>
                  </td>
                  <td class="small">
                    <div><c:out value="${ctr.address}" /></div>
                    <c:if test="${not empty ctr.phone}">
                      <div class="muted">☎ <c:out value="${ctr.phone}" /></div>
                    </c:if>
                  </td>
                  <td class="small">
                    <c:if test="${not empty ctr.serviceName}">
                      <div><c:out value="${ctr.serviceName}" /></div>
                    </c:if>
                    <c:if test="${not empty ctr.priceType}">
                      <div class="muted">요금: <c:out value="${ctr.priceType}" /></div>
                    </c:if>
                    <c:if test="${ctr.waitlist eq 1}">
                      <div class="muted">대기 있음
                        <c:if test="${not empty ctr.waitlistNote}">: <c:out value="${ctr.waitlistNote}" /></c:if>
                      </div>
                    </c:if>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </c:if>
      </div>
    </c:forEach>
  </div>
</c:if>

<%-- ④ 치료타입 블록 없을 때: centers fallback (ChecklistResultMapper 조회) --%>
<c:if test="${empty therapyCenterBlocks}">
  <div class="box">
    <h2>추천 치료기관</h2>

    <c:if test="${empty centers}">
      <div class="empty">
        조건에 맞는 기관이 없습니다.<br>
        <span class="muted">(domain_therapy_map 또는 center_services 데이터를 확인해 주세요)</span>
      </div>
    </c:if>

    <c:if test="${not empty centers}">
      <table>
        <thead>
          <tr>
            <th>기관명</th>
            <th>지역</th>
            <th>주소 / 연락처</th>
            <th>제공 치료</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach items="${centers}" var="ctr">
            <tr>
              <td><b><c:out value="${ctr.name}" /></b></td>
              <td class="small">
                <c:out value="${ctr.sido}" />
                <c:if test="${not empty ctr.sigungu}"> <c:out value="${ctr.sigungu}" /></c:if>
              </td>
              <td class="small">
                <div><c:out value="${ctr.address}" /></div>
                <c:if test="${not empty ctr.phone}">
                  <div class="muted">☎ <c:out value="${ctr.phone}" /></div>
                </c:if>
              </td>
              <td class="small">
                <c:if test="${not empty ctr.therapyTypeCodes}">
                  <c:forEach items="${fn:split(ctr.therapyTypeCodes, ',')}" var="tc">
                    <c:set var="tc" value="${fn:trim(tc)}" />
                    <span class="pill">
                      <c:choose>
                        <c:when test="${not empty therapyTypeLabelMap[tc]}">${therapyTypeLabelMap[tc]}</c:when>
                        <c:otherwise><c:out value="${tc}" /></c:otherwise>
                      </c:choose>
                    </span>
                  </c:forEach>
                </c:if>
                <c:if test="${empty ctr.therapyTypeCodes}">
                  <span class="muted">정보 없음</span>
                </c:if>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:if>
  </div>
</c:if>

</body>
</html>
