<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>내 기록</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
           margin: 0; background: #f0f2f5; color: #222; }
    .page-wrap { max-width: 900px; margin: 0 auto; padding: 16px; }

    /* ── 헤더 ── */
    .page-header { display: flex; justify-content: space-between; align-items: center;
                   margin-bottom: 14px; flex-wrap: wrap; gap: 8px; }
    .page-header h1 { margin: 0; font-size: 20px; }
    .header-links a { color: #4a90d9; text-decoration: none; font-size: 13px; margin-left: 10px; }

    /* ── 아이 탭 ── */
    .child-tabs { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 18px; }
    .child-tab { display: inline-block; padding: 7px 16px; border-radius: 999px;
                 font-size: 13px; text-decoration: none; border: 1px solid #ddd;
                 background: #fff; color: #555; transition: all .15s; }
    .child-tab.active { background: #4a90d9; color: #fff; border-color: #4a90d9; font-weight: 600; }
    .child-tab:hover:not(.active) { border-color: #4a90d9; color: #4a90d9; }

    /* ── 섹션 공통 ── */
    .section { margin-bottom: 24px; }
    .section-header { display: flex; justify-content: space-between; align-items: center;
                      margin-bottom: 10px; }
    .section-title { font-size: 16px; font-weight: 700; margin: 0; }
    .section-link  { font-size: 13px; color: #4a90d9; text-decoration: none; }
    .section-link:hover { text-decoration: underline; }

    /* ── 실행 카드 (SUBMITTED / DRAFT 공통) ── */
    .run-list { display: flex; flex-direction: column; gap: 10px; }
    .run-card { background: #fff; border-radius: 12px;
                box-shadow: 0 1px 4px rgba(0,0,0,.07);
                padding: 14px 16px;
                display: flex; justify-content: space-between; align-items: center;
                flex-wrap: wrap; gap: 8px; }
    .run-card-left { flex: 1; min-width: 0; }
    .run-card-meta { display: flex; align-items: center; gap: 6px; flex-wrap: wrap;
                     margin-bottom: 4px; }
    .badge-child { padding: 2px 8px; border-radius: 999px; font-size: 11px;
                   background: #e8f4fd; color: #1a6fba; border: 1px solid #b3d7f5; }
    .badge-risk { padding: 2px 8px; border-radius: 999px; font-size: 11px; font-weight: 600; }
    .badge-risk.HIGH     { background: #fdecea; color: #c0392b; border: 1px solid #f5c6c2; }
    .badge-risk.MODERATE { background: #fef4e6; color: #d68910; border: 1px solid #fad7a0; }
    .badge-risk.LOW      { background: #eafaf1; color: #1e8449; border: 1px solid #a9dfbf; }
    .run-title { font-size: 14px; font-weight: 600; white-space: nowrap;
                 overflow: hidden; text-overflow: ellipsis; }
    .run-date  { font-size: 12px; color: #999; margin-top: 2px; }
    .btn-run-action { display: inline-block; padding: 7px 16px; border-radius: 8px;
                      font-size: 13px; text-decoration: none; white-space: nowrap;
                      flex-shrink: 0; }
    .btn-result { background: #4a90d9; color: #fff; }
    .btn-result:hover { background: #3a7bc8; }
    .btn-resume { background: #27ae60; color: #fff; }
    .btn-resume:hover { background: #219a52; }

    /* ── 즐겨찾기 센터 칩 ── */
    .center-chips { display: flex; flex-wrap: wrap; gap: 8px; }
    .center-chip { display: inline-block; padding: 8px 16px; border-radius: 999px;
                   background: #fff; border: 1px solid #ddd;
                   font-size: 13px; color: #333; text-decoration: none;
                   box-shadow: 0 1px 3px rgba(0,0,0,.06); transition: all .15s;
                   max-width: 200px; white-space: nowrap; overflow: hidden;
                   text-overflow: ellipsis; }
    .center-chip::before { content: '★ '; color: #f39c12; }
    .center-chip:hover { border-color: #4a90d9; color: #4a90d9; }

    /* ── 빈 상태 ── */
    .empty-mini { padding: 20px; text-align: center; color: #aaa;
                  background: #fff; border-radius: 12px;
                  box-shadow: 0 1px 4px rgba(0,0,0,.07); font-size: 13px; }
    .empty-mini .em-icon { font-size: 28px; display: block; margin-bottom: 6px; }

    /* ── 아이 없음 전체 빈 상태 ── */
    .empty-full { text-align: center; padding: 48px 20px; color: #aaa;
                  background: #fff; border-radius: 12px;
                  box-shadow: 0 1px 4px rgba(0,0,0,.07); }
    .empty-full .icon { font-size: 44px; display: block; margin-bottom: 12px; }
    .empty-full p { margin: 6px 0; font-size: 15px; }
    .empty-full .sub { font-size: 13px; }

    /* ── CTA 영역 ── */
    .cta-bar { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 12px; }
    .btn-cta { display: inline-block; padding: 11px 22px; border-radius: 8px;
               font-size: 14px; text-decoration: none; }
    .btn-cta-primary  { background: #4a90d9; color: #fff; }
    .btn-cta-primary:hover  { background: #3a7bc8; }
    .btn-cta-outline  { background: #fff; color: #4a90d9;
                        border: 1px solid #4a90d9; }
    .btn-cta-outline:hover  { background: #f0f6ff; }

    @media (max-width: 480px) {
      .run-card { flex-direction: column; align-items: flex-start; }
      .btn-run-action { width: 100%; text-align: center; }
    }
  </style>
</head>
<body>
<div class="page-wrap">

  <!-- ── 헤더 ── -->
  <div class="page-header">
    <h1>📋 내 기록</h1>
    <div class="header-links">
      <a href="/usr/checklist/start?checklistId=1">새 체크리스트</a>
      <a href="/usr/center/list">센터 찾기</a>
      <a href="/usr/child/list">아이 관리</a>
    </div>
  </div>

  <!-- ── 아이 없음 전체 빈 상태 ── -->
  <c:if test="${empty children}">
    <div class="empty-full">
      <span class="icon">👶</span>
      <p>등록된 아이 프로필이 없습니다.</p>
      <p class="sub">아이 프로필을 먼저 추가해 체크리스트를 시작해 보세요.</p>
      <div class="cta-bar" style="justify-content:center; margin-top:18px;">
        <a href="/usr/child/write" class="btn-cta btn-cta-primary">아이 프로필 추가 →</a>
        <a href="/usr/center/list" class="btn-cta btn-cta-outline">센터 둘러보기</a>
      </div>
    </div>
  </c:if>

  <!-- ── 아이가 있을 때 ── -->
  <c:if test="${not empty children}">

    <!-- 아이 탭 -->
    <div class="child-tabs">
      <a href="/usr/my"
         class="child-tab ${selectedChildId == 0 ? 'active' : ''}">전체</a>
      <c:forEach items="${children}" var="ch">
        <a href="/usr/my?childId=${ch.id}"
           class="child-tab ${selectedChildId == ch.id ? 'active' : ''}">
          <c:out value="${ch.name}" />
        </a>
      </c:forEach>
    </div>

    <!-- ── 섹션 1: 최근 제출 결과 ── -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">최근 제출 결과</h2>
      </div>

      <c:choose>
        <c:when test="${empty submittedRuns}">
          <div class="empty-mini">
            <span class="em-icon">📄</span>
            아직 제출한 검사 결과가 없습니다.
          </div>
        </c:when>
        <c:otherwise>
          <div class="run-list">
            <c:forEach items="${submittedRuns}" var="run">
              <div class="run-card">
                <div class="run-card-left">
                  <div class="run-card-meta">
                    <span class="badge-child"><c:out value="${run.childName}" /></span>
                    <c:if test="${not empty run.riskLevel}">
                      <span class="badge-risk ${run.riskLevel}">
                        <c:choose>
                          <c:when test="${run.riskLevel eq 'HIGH'}">주의 필요</c:when>
                          <c:when test="${run.riskLevel eq 'MODERATE'}">관찰 필요</c:when>
                          <c:otherwise>양호</c:otherwise>
                        </c:choose>
                      </span>
                    </c:if>
                  </div>
                  <div class="run-title"><c:out value="${run.checklistTitle}" /></div>
                  <div class="run-date"><c:out value="${run.displayDate}" /></div>
                </div>
                <a href="/usr/checklist/result?runId=${run.runId}"
                   class="btn-run-action btn-result">결과 보기 →</a>
              </div>
            </c:forEach>
          </div>
        </c:otherwise>
      </c:choose>

      <div class="cta-bar">
        <a href="/usr/checklist/start?checklistId=1<c:if test='${selectedChildId != 0}'>&amp;childId=${selectedChildId}</c:if>"
           class="btn-cta btn-cta-primary">새 체크리스트 시작 →</a>
      </div>
    </div>

    <!-- ── 섹션 2: 이어하기 (임시저장) ── -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">이어하기 (임시저장)</h2>
      </div>

      <c:choose>
        <c:when test="${empty draftRuns}">
          <div class="empty-mini">
            <span class="em-icon">✏️</span>
            임시저장된 검사가 없습니다.
          </div>
        </c:when>
        <c:otherwise>
          <div class="run-list">
            <c:forEach items="${draftRuns}" var="run">
              <div class="run-card">
                <div class="run-card-left">
                  <div class="run-card-meta">
                    <span class="badge-child"><c:out value="${run.childName}" /></span>
                  </div>
                  <div class="run-title"><c:out value="${run.checklistTitle}" /></div>
                  <div class="run-date">마지막 저장: <c:out value="${run.displayDate}" /></div>
                </div>
                <a href="/usr/checklist/start?runId=${run.runId}"
                   class="btn-run-action btn-resume">이어하기 →</a>
              </div>
            </c:forEach>
          </div>
        </c:otherwise>
      </c:choose>
    </div>

    <!-- ── 섹션 3: 즐겨찾기 센터 ── -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">즐겨찾기 센터</h2>
        <a href="/usr/center/favorites" class="section-link">전체보기 →</a>
      </div>

      <c:choose>
        <c:when test="${empty favoriteCenters}">
          <div class="empty-mini">
            <span class="em-icon">☆</span>
            즐겨찾기한 센터가 없습니다.
          </div>
        </c:when>
        <c:otherwise>
          <div class="center-chips">
            <c:forEach items="${favoriteCenters}" var="ctr">
              <a href="/usr/center/list" class="center-chip"
                 title="<c:out value='${ctr.name}' />">
                <c:out value="${ctr.name}" />
              </a>
            </c:forEach>
          </div>
        </c:otherwise>
      </c:choose>

      <div class="cta-bar">
        <a href="/usr/center/list" class="btn-cta btn-cta-outline">센터 전체보기 →</a>
      </div>
    </div>

  </c:if><%-- end: children not empty --%>

</div>
</body>
</html>
