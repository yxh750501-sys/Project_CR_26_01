<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>LittleSteps — 아동 발달 관찰 기록 & 지원 기관 찾기</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
           margin: 0; background: #f5f7fa; color: #222; }

    /* ── 히어로 영역 ── */
    .hero {
      background: linear-gradient(140deg, #2f5fe8 0%, #4f43cc 100%);
      color: #fff;
      padding: 52px 20px 44px;
      text-align: center;
    }
    .hero-tag {
      display: inline-block;
      padding: 3px 12px; border-radius: 999px;
      background: rgba(255,255,255,.18);
      font-size: 12px; letter-spacing: .4px;
      margin-bottom: 14px;
    }
    .hero h1 {
      margin: 0 0 10px; font-size: 26px; font-weight: 700; line-height: 1.35;
    }
    .hero p {
      margin: 0 0 30px; font-size: 14px; opacity: .85; line-height: 1.7;
    }
    .hero-btns {
      display: flex; justify-content: center; gap: 12px; flex-wrap: wrap;
    }
    .btn-hero-primary {
      display: inline-block; padding: 12px 26px; border-radius: 8px;
      background: #fff; color: #3a6de8; font-size: 15px; font-weight: 700;
      text-decoration: none; transition: opacity .15s;
    }
    .btn-hero-primary:hover { opacity: .88; }
    .btn-hero-outline {
      display: inline-block; padding: 12px 26px; border-radius: 8px;
      background: rgba(255,255,255,.14); color: #fff; font-size: 15px;
      text-decoration: none; border: 1.5px solid rgba(255,255,255,.5);
      transition: background .15s;
    }
    .btn-hero-outline:hover { background: rgba(255,255,255,.24); }

    /* ── 로그인 상태: 기록 CTA 바 ── */
    .record-cta {
      background: #fff;
      border-bottom: 1px solid #e8edf5;
      padding: 16px 20px;
    }
    .record-cta__inner {
      max-width: 1000px; margin: 0 auto;
      display: flex; align-items: center; justify-content: space-between;
      flex-wrap: wrap; gap: 12px;
    }
    .record-cta__text {
      font-size: 14px; color: #555;
    }
    .record-cta__text strong { color: #222; }
    .record-cta__btns { display: flex; gap: 8px; flex-wrap: wrap; }
    .btn-cta {
      display: inline-block; padding: 8px 18px; border-radius: 7px;
      font-size: 13px; font-weight: 600; text-decoration: none;
    }
    .btn-cta-primary { background: #3a6de8; color: #fff; }
    .btn-cta-primary:hover { background: #2c59c9; }
    .btn-cta-outline {
      background: #fff; color: #3a6de8; border: 1.5px solid #3a6de8;
    }
    .btn-cta-outline:hover { background: #f0f4ff; }

    /* ── 페이지 본문 ── */
    .page-wrap { max-width: 1000px; margin: 0 auto; padding: 28px 16px 48px; }

    /* ── 게시판 그리드 ── */
    .board-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
    }
    @media (max-width: 640px) {
      .board-grid { grid-template-columns: 1fr; }
      .hero h1 { font-size: 21px; }
    }

    /* ── 게시판 카드 ── */
    .board-card {
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 1px 5px rgba(0,0,0,.07);
      overflow: hidden;
    }
    .board-card__header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 14px 18px 10px;
      border-bottom: 1px solid #f3f4f6;
    }
    .board-card__title { font-size: 15px; font-weight: 700; margin: 0; }
    .board-card__more  {
      font-size: 13px; color: #3a6de8; text-decoration: none; flex-shrink: 0;
    }
    .board-card__more:hover { text-decoration: underline; }

    .post-list { padding: 4px 0; list-style: none; margin: 0; }
    .post-item {
      display: flex; justify-content: space-between; align-items: center;
      padding: 9px 18px;
      border-bottom: 1px solid #f7f8fa;
      gap: 8px;
    }
    .post-item:last-child { border-bottom: none; }
    .post-item__title {
      flex: 1; min-width: 0; font-size: 14px; color: #333;
      white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
      text-decoration: none;
    }
    .post-item__title:hover { color: #3a6de8; text-decoration: underline; }
    .post-item__date { font-size: 12px; color: #c0c4cc; flex-shrink: 0; }
    .board-empty {
      padding: 30px 18px; text-align: center;
      font-size: 13px; color: #c0c4cc;
    }

    /* ── 서비스 소개 카드 (비로그인 하단) ── */
    .intro-strip {
      margin-top: 24px;
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 1px 5px rgba(0,0,0,.07);
      padding: 28px 24px;
      display: flex; justify-content: space-between; align-items: center;
      flex-wrap: wrap; gap: 16px;
    }
    .intro-strip__text h2 { margin: 0 0 6px; font-size: 17px; }
    .intro-strip__text p  { margin: 0; font-size: 13px; color: #777; line-height: 1.6; }
    .intro-strip__btns { display: flex; gap: 10px; flex-wrap: wrap; }
    .btn-intro-primary {
      display: inline-block; padding: 10px 22px; border-radius: 8px;
      background: #3a6de8; color: #fff; font-size: 14px; font-weight: 600;
      text-decoration: none;
    }
    .btn-intro-primary:hover { background: #2c59c9; }
    .btn-intro-outline {
      display: inline-block; padding: 10px 22px; border-radius: 8px;
      background: #fff; color: #3a6de8; border: 1.5px solid #3a6de8;
      font-size: 14px; font-weight: 600; text-decoration: none;
    }
    .btn-intro-outline:hover { background: #f0f4ff; }
  </style>
</head>
<body>

<%@ include file="/WEB-INF/jsp/usr/common/header.jsp" %>

<%-- ══════════════════════════════════════════════════════
     히어로 배너 — 로그인 여부로 분기
════════════════════════════════════════════════════════ --%>
<div class="hero">
  <c:choose>

    <%-- ── 비로그인 ── --%>
    <c:when test="${empty loginedUserId}">
      <span class="hero-tag">아동 발달 관찰 지원 서비스</span>
      <h1>아이의 성장 과정을<br/>함께 기록해요</h1>
      <p>관찰 체크리스트로 발달 영역을 파악하고<br/>가까운 지원 기관을 찾아보세요.</p>
      <div class="hero-btns">
        <a href="/usr/member/join"  class="btn-hero-primary">무료로 시작하기 →</a>
        <a href="/usr/member/login" class="btn-hero-outline">로그인</a>
      </div>
    </c:when>

    <%-- ── 로그인 상태 ── --%>
    <c:otherwise>
      <span class="hero-tag">기록된 관찰이 쌓일수록 더 정확해져요</span>
      <h1>오늘도 기록을 이어볼까요?</h1>
      <p>임시저장된 기록을 이어쓰거나<br/>새 관찰 기록을 시작할 수 있습니다.</p>
      <div class="hero-btns">
        <a href="/usr/checklist/start?checklistId=1" class="btn-hero-primary">새 기록 시작하기 →</a>
        <a href="/usr/my"                            class="btn-hero-outline">내 기록 확인하기</a>
      </div>
    </c:otherwise>

  </c:choose>
</div>

<%-- 로그인 상태: 기록 빠른 이동 바 --%>
<c:if test="${not empty loginedUserId}">
  <div class="record-cta">
    <div class="record-cta__inner">
      <div class="record-cta__text">
        <strong>관찰 기록</strong> — 이어쓰기 또는 결과를 마이페이지에서 확인하세요.
      </div>
      <div class="record-cta__btns">
        <a href="/usr/my"                            class="btn-cta btn-cta-outline">내 기록 이어보기</a>
        <a href="/usr/checklist/start?checklistId=1" class="btn-cta btn-cta-primary">새 기록 시작</a>
      </div>
    </div>
  </div>
</c:if>

<%-- ══════════════════════════════════════════════════════
     게시판 최신글 섹션
════════════════════════════════════════════════════════ --%>
<div class="page-wrap">

  <div class="board-grid">

    <%-- 프로그램 게시판 --%>
    <div class="board-card">
      <div class="board-card__header">
        <h2 class="board-card__title">📌 프로그램 안내</h2>
        <a href="/usr/program/list" class="board-card__more">더보기 →</a>
      </div>
      <c:choose>
        <c:when test="${empty programPosts}">
          <div class="board-empty">등록된 프로그램이 없습니다.</div>
        </c:when>
        <c:otherwise>
          <ul class="post-list">
            <c:forEach items="${programPosts}" var="p">
              <li class="post-item">
                <a href="/usr/program/detail?id=${p.id}" class="post-item__title"
                   title="<c:out value='${p.title}'/>">
                  <c:out value="${p.title}" />
                </a>
                <span class="post-item__date"><c:out value="${p.createdAt}" /></span>
              </li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </div>

    <%-- 자유 게시판 --%>
    <div class="board-card">
      <div class="board-card__header">
        <h2 class="board-card__title">💬 자유 게시판</h2>
        <a href="/usr/post/list" class="board-card__more">더보기 →</a>
      </div>
      <c:choose>
        <c:when test="${empty freePosts}">
          <div class="board-empty">등록된 글이 없습니다.</div>
        </c:when>
        <c:otherwise>
          <ul class="post-list">
            <c:forEach items="${freePosts}" var="p">
              <li class="post-item">
                <a href="/usr/post/detail?id=${p.id}" class="post-item__title"
                   title="<c:out value='${p.title}'/>">
                  <c:out value="${p.title}" />
                </a>
                <span class="post-item__date"><c:out value="${p.createdAt}" /></span>
              </li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </div>

  </div>

  <%-- 비로그인일 때만: 서비스 소개 CTA 카드 --%>
  <c:if test="${empty loginedUserId}">
    <div class="intro-strip">
      <div class="intro-strip__text">
        <h2>관찰 체크리스트가 처음이라면?</h2>
        <p>회원가입 후 30문항 체크리스트를 작성하면<br/>
           강점 영역과 지원이 필요한 영역을 한눈에 파악하고<br/>
           가까운 지원 기관을 추천받을 수 있습니다.</p>
      </div>
      <div class="intro-strip__btns">
        <a href="/usr/member/join"  class="btn-intro-primary">회원가입 →</a>
        <a href="/usr/center/list"  class="btn-intro-outline">센터 둘러보기</a>
      </div>
    </div>
  </c:if>

</div>

</body>
</html>
