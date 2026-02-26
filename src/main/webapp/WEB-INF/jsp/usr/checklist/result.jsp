<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>체크리스트 결과</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
      margin: 0; background: #f0f2f5; color: #222;
    }
    .page-wrap { max-width: 720px; margin: 0 auto; padding: 16px; }

    /* ── 카드 공통 ── */
    .card {
      background: #fff; border-radius: 12px;
      box-shadow: 0 1px 4px rgba(0,0,0,.08);
      padding: 20px; margin-bottom: 16px;
    }
    h2 { margin: 0 0 14px; font-size: 17px; }

    /* ── 요약 카드 ── */
    .summary-card.risk-HIGH     { border-left: 5px solid #e74c3c; }
    .summary-card.risk-MODERATE { border-left: 5px solid #f39c12; }
    .summary-card.risk-LOW      { border-left: 5px solid #27ae60; }

    .summary-meta { font-size: 13px; color: #666; margin-bottom: 14px; }
    .summary-meta span { margin-right: 10px; }

    .summary-row { display: flex; gap: 24px; flex-wrap: wrap; margin-bottom: 14px; }
    .summary-item { display: flex; flex-direction: column; }
    .summary-item .lbl { font-size: 11px; color: #999; margin-bottom: 4px; }
    .summary-item .val { font-size: 22px; font-weight: 700; }

    .risk-badge { display: inline-block; padding: 4px 12px; border-radius: 999px;
                  font-size: 14px; font-weight: 700; }
    .risk-badge.HIGH     { background: #fdecea; color: #c0392b; }
    .risk-badge.MODERATE { background: #fef3e2; color: #d35400; }
    .risk-badge.LOW      { background: #e8f5e9; color: #27ae60; }

    .next-step {
      background: #f4f7ff; border: 1px solid #dde5f8; border-radius: 8px;
      padding: 12px 14px; font-size: 14px; color: #2d4a9a; line-height: 1.5;
    }
    .next-step::before { content: "💡  "; }

    /* ── 도메인 테이블 ── */
    .domain-table { width: 100%; border-collapse: collapse; font-size: 14px; }
    .domain-table th {
      background: #f8f9fe; padding: 8px 10px; text-align: left;
      font-size: 12px; color: #666; border-bottom: 2px solid #e8e8e8;
    }
    .domain-table td { padding: 10px; border-bottom: 1px solid #f0f0f0; vertical-align: middle; }
    .domain-table tr:last-child td { border-bottom: none; }
    .avg-score { font-weight: 600; }
    .badge-priority { background: #fdecea; color: #c0392b; padding: 2px 7px;
                      border-radius: 999px; font-size: 11px; font-weight: 600; margin-left: 5px; }
    .badge-ok       { background: #e8f5e9; color: #27ae60; padding: 2px 7px;
                      border-radius: 999px; font-size: 11px; font-weight: 600; margin-left: 5px; }
    .badge-caution  { background: #fef3e2; color: #d35400; padding: 2px 7px;
                      border-radius: 999px; font-size: 11px; font-weight: 600; margin-left: 5px; }

    /* ── 태그 ── */
    .tag { display: inline-block; padding: 3px 10px; border-radius: 999px;
           font-size: 12px; margin: 2px 3px 2px 0; }
    .tag-domain  { background: #f0f4ff; color: #2d4a9a; border: 1px solid #c5d3f8; }
    .tag-therapy { background: #fff0f8; color: #8e2474; border: 1px solid #f0c0e0; }
    .tag-match   { background: #e8f5e9; color: #27ae60; border: 1px solid #b5dfc0; font-weight: 600; }

    /* ── 추천 영역 태그 블록 ── */
    .domain-tags { padding: 6px 0 2px; }

    /* ── 센터 카드 ── */
    .center-card {
      border: 1px solid #eee; border-radius: 10px;
      padding: 14px 16px; margin-bottom: 12px;
    }
    .center-card:last-child { margin-bottom: 0; }
    .center-header {
      display: flex; justify-content: space-between;
      align-items: flex-start; flex-wrap: wrap; gap: 6px; margin-bottom: 8px;
    }
    .center-name  { font-size: 15px; font-weight: 700; }
    .match-badge  { background: #e8f0ff; color: #2d4a9a; font-size: 12px;
                    padding: 3px 10px; border-radius: 999px; font-weight: 600; white-space: nowrap; }
    .center-info  { font-size: 13px; color: #555; margin-bottom: 6px; line-height: 1.6; }
    .section-lbl  { font-size: 11px; color: #aaa; display: block; margin: 6px 0 3px; }

    /* ── 빈 상태 ── */
    .empty-state { text-align: center; padding: 28px 16px; color: #aaa; }
    .empty-state .icon { font-size: 36px; display: block; margin-bottom: 8px; }
    .empty-state p { margin: 4px 0; font-size: 14px; }

    /* ── CTA ── */
    .cta-bar { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 4px; }
    .btn { display: inline-block; padding: 11px 20px; border-radius: 8px;
           font-size: 14px; text-decoration: none; text-align: center; cursor: pointer; }
    .btn-primary   { background: #4a90d9; color: #fff; border: none; }
    .btn-secondary { background: #fff; color: #4a90d9; border: 1px solid #4a90d9; }
    .btn:hover { opacity: .88; }

    /* ── 즐겨찾기 버튼 ── */
    .fav-btn {
      background: none; border: none; font-size: 22px; cursor: pointer;
      color: #ccc; padding: 0; line-height: 1; float: right;
      transition: color .2s;
    }
    .fav-btn.favorited { color: #f39c12; }
    .fav-btn:hover     { color: #f39c12; }

    .muted { color: #888; }
    .small { font-size: 12px; }

    @media (max-width: 480px) {
      .summary-row { gap: 16px; }
      .summary-item .val { font-size: 19px; }
      .domain-table th, .domain-table td { padding: 7px 6px; font-size: 13px; }
    }
  </style>
</head>
<body>
<div class="page-wrap">

  <%-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
       ① 결과 요약 카드
       ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ --%>
  <div class="card summary-card risk-${not empty riskLevel ? riskLevel : 'LOW'}">
    <h2>체크리스트 결과</h2>

    <div class="summary-meta">
      <span>🧒 <c:out value="${runInfo.childName}" /></span>
      <span>📋 <c:out value="${runInfo.checklistTitle}" /></span>
      <c:if test="${not empty runInfo.submittedAt}">
        <span>📅 <c:out value="${runInfo.submittedAt}" /></span>
      </c:if>
    </div>

    <div class="summary-row">
      <div class="summary-item">
        <span class="lbl">총점</span>
        <c:choose>
          <c:when test="${not empty runInfo.totalScore and runInfo.totalScore gt 0}">
            <span class="val"><c:out value="${runInfo.totalScore}" />점</span>
          </c:when>
          <c:otherwise><span class="val muted">-</span></c:otherwise>
        </c:choose>
      </div>
      <div class="summary-item">
        <span class="lbl">지원 필요도</span>
        <span>
          <c:choose>
            <c:when test="${riskLevel eq 'HIGH'}">
              <span class="risk-badge HIGH">높음 ▲</span>
            </c:when>
            <c:when test="${riskLevel eq 'MODERATE'}">
              <span class="risk-badge MODERATE">보통 ◆</span>
            </c:when>
            <c:otherwise>
              <span class="risk-badge LOW">낮음 ●</span>
            </c:otherwise>
          </c:choose>
        </span>
      </div>
    </div>

    <div class="next-step"><c:out value="${recommendationSummary}" /></div>
  </div>

  <%-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
       ② 기능영역 점수
       ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ --%>
  <div class="card">
    <h2>기능영역 점수</h2>
    <div class="muted small" style="margin-bottom:10px;">
      ※ 낮은 평균 점수 영역일수록 우선 지원이 권장됩니다 (1=불가능 ~ 4=가능함, 5=모름)
    </div>

    <c:choose>
      <c:when test="${empty domainStats}">
        <div class="empty-state">
          <span class="icon">📊</span>
          <p>도메인 점수 데이터가 없습니다.</p>
          <p class="small">문항에 답변 후 제출해 주세요.</p>
        </div>
      </c:when>
      <c:otherwise>
        <table class="domain-table">
          <thead>
            <tr>
              <th style="width:36%;">기능영역</th>
              <th style="width:11%;">문항</th>
              <th style="width:11%;">합계</th>
              <th style="width:14%;">평균</th>
              <th>상태</th>
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
                <td class="avg-score">
                  <fmt:formatNumber value="${d.avgScore}" maxFractionDigits="2" />
                </td>
                <td>
                  <c:choose>
                    <c:when test="${d.avgScore le 2.0}">
                      <span class="badge-priority">우선 지원 권장</span>
                    </c:when>
                    <c:when test="${d.avgScore gt 2.0 and d.avgScore le 3.0}">
                      <span class="badge-caution">경계 수준</span>
                    </c:when>
                    <c:otherwise>
                      <span class="badge-ok">양호</span>
                    </c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>

    <%-- 우선 지원 권장 영역 태그 --%>
    <c:if test="${not empty recommendedDomains}">
      <div style="margin-top:14px; padding-top:12px; border-top:1px solid #f0f0f0;">
        <span class="small muted">우선 지원 권장 영역: </span>
        <div class="domain-tags" style="display:inline;">
          <c:forEach items="${recommendedDomains}" var="dc">
            <span class="tag tag-domain">
              <c:choose>
                <c:when test="${not empty domainLabelMap[dc]}">${domainLabelMap[dc]}</c:when>
                <c:otherwise><c:out value="${dc}" /></c:otherwise>
              </c:choose>
            </span>
          </c:forEach>
        </div>
      </div>
    </c:if>
  </div>

  <%-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
       ③ 추천 치료기관 (추천 근거 포함)
       ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ --%>
  <div class="card">
    <h2>추천 치료기관</h2>

    <c:choose>
      <c:when test="${empty centers}">
        <div class="empty-state">
          <span class="icon">🏥</span>
          <p>현재 매칭되는 치료기관이 없습니다.</p>
          <p class="small">domain_therapy_map 또는 center_services 데이터를 확인해 주세요.</p>
        </div>
      </c:when>
      <c:otherwise>
        <c:forEach items="${centers}" var="ctr">
          <div class="center-card">

            <%-- 센터 헤더: 이름 + 매칭 배지 + 즐겨찾기 --%>
            <div class="center-header">
              <span class="center-name"><c:out value="${ctr.name}" /></span>
              <div style="display:flex;align-items:center;gap:6px;">
                <c:if test="${ctr.matchScore gt 0}">
                  <span class="match-badge">${ctr.matchScore}개 영역 매칭</span>
                </c:if>
                <c:set var="isFav" value="${not empty favoriteCenterIds and favoriteCenterIds.contains(ctr.id)}" />
                <button class="fav-btn ${isFav ? 'favorited' : ''}"
                        onclick="toggleFav(this, ${ctr.id})"
                        title="${isFav ? '즐겨찾기 해제' : '즐겨찾기 추가'}"
                        aria-label="${isFav ? '즐겨찾기 해제' : '즐겨찾기 추가'}">
                  ${isFav ? '★' : '☆'}
                </button>
              </div>
            </div>

            <%-- 지역 / 연락처 --%>
            <div class="center-info">
              <c:if test="${not empty ctr.sido}">
                📍 <c:out value="${ctr.sido}" />
                <c:if test="${not empty ctr.sigungu}"> <c:out value="${ctr.sigungu}" /></c:if>
                <c:if test="${not empty ctr.address}"> · <c:out value="${ctr.address}" /></c:if>
              </c:if>
              <c:if test="${not empty ctr.phone}">
                <br/>☎ <c:out value="${ctr.phone}" />
              </c:if>
              <c:if test="${not empty ctr.website}">
                <br/>🌐 <c:out value="${ctr.website}" />
              </c:if>
            </div>

            <%-- 매칭 도메인 태그 (추천 근거) --%>
            <c:if test="${not empty ctr.matchedDomains}">
              <span class="section-lbl">추천 근거 영역</span>
              <c:forEach items="${fn:split(ctr.matchedDomains, ',')}" var="dm">
                <c:set var="dm" value="${fn:trim(dm)}" />
                <span class="tag tag-domain">
                  <c:choose>
                    <c:when test="${not empty domainLabelMap[dm]}">${domainLabelMap[dm]}</c:when>
                    <c:otherwise><c:out value="${dm}" /></c:otherwise>
                  </c:choose>
                </span>
              </c:forEach>
            </c:if>

            <%-- 제공 치료 태그 --%>
            <c:if test="${not empty ctr.therapyTypes or not empty ctr.therapyTypeCodes}">
              <span class="section-lbl">제공 치료</span>
              <c:if test="${not empty ctr.therapyTypes}">
                <c:forEach items="${ctr.therapyTypes}" var="tt">
                  <span class="tag tag-therapy">
                    <c:choose>
                      <c:when test="${not empty therapyTypeLabelMap[tt.code]}">${therapyTypeLabelMap[tt.code]}</c:when>
                      <c:otherwise><c:out value="${tt.title}" /></c:otherwise>
                    </c:choose>
                  </span>
                </c:forEach>
              </c:if>
              <c:if test="${empty ctr.therapyTypes and not empty ctr.therapyTypeCodes}">
                <c:forEach items="${fn:split(ctr.therapyTypeCodes, ',')}" var="tc">
                  <c:set var="tc" value="${fn:trim(tc)}" />
                  <span class="tag tag-therapy">
                    <c:choose>
                      <c:when test="${not empty therapyTypeLabelMap[tc]}">${therapyTypeLabelMap[tc]}</c:when>
                      <c:otherwise><c:out value="${tc}" /></c:otherwise>
                    </c:choose>
                  </span>
                </c:forEach>
              </c:if>
            </c:if>

          </div>
        </c:forEach>
      </c:otherwise>
    </c:choose>
  </div>

  <%-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
       ④ CTA — 다음 단계 버튼
       ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ --%>
  <div class="card">
    <h2>다음 단계</h2>
    <div class="cta-bar">
      <a href="/usr/checklist/result-centers?runId=${runId}" class="btn btn-primary">
        전체 추천 기관 보기 →
      </a>
      <a href="/usr/child/list" class="btn btn-secondary">아이 목록</a>
    </div>
  </div>

</div>
<script>
function toggleFav(btn, centerId) {
  fetch('/usr/center/doToggleFavorite', {
    method: 'POST',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    body: 'centerId=' + centerId
  })
  .then(r => r.json())
  .then(data => {
    if (data.favorited) {
      btn.classList.add('favorited');
      btn.textContent = '★';
      btn.title = '즐겨찾기 해제';
    } else {
      btn.classList.remove('favorited');
      btn.textContent = '☆';
      btn.title = '즐겨찾기 추가';
    }
  })
  .catch(() => alert('즐겨찾기 처리 중 오류가 발생했습니다.'));
}
</script>
</body>
</html>
