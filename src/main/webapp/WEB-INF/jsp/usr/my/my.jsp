<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>내 정보 — CareRoute</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
           margin: 0; background: #f0f2f5; color: #222; }
    .page-wrap { max-width: 900px; margin: 0 auto; padding: 20px 16px 40px; }

    /* ── 알림 배너 ── */
    .alert {
      padding: 12px 16px; border-radius: 8px; margin-bottom: 16px;
      font-size: 14px;
    }
    .alert-success { background: #eafaf1; color: #1a7a45; border: 1px solid #a9dfbf; }
    .alert-error   { background: #fdecea; color: #a93226; border: 1px solid #f5c6c2; }

    /* ── 프로필 카드 ── */
    .profile-card {
      background: #fff;
      border-radius: 14px;
      box-shadow: 0 2px 10px rgba(0,0,0,.07);
      padding: 28px 24px;
      display: flex;
      align-items: center;
      gap: 24px;
      margin-bottom: 20px;
      flex-wrap: wrap;
    }
    .profile-avatar {
      flex-shrink: 0;
      width: 80px; height: 80px;
      border-radius: 50%;
      object-fit: cover;
      border: 3px solid #e0e9ff;
      background: #e8eef8;
    }
    .profile-avatar-placeholder {
      width: 80px; height: 80px;
      border-radius: 50%;
      background: #d1ddf5;
      display: flex; align-items: center; justify-content: center;
      font-size: 32px;
      flex-shrink: 0;
    }
    .profile-info { flex: 1; min-width: 0; }
    .profile-name { font-size: 20px; font-weight: 700; margin: 0 0 4px; }
    .profile-meta { font-size: 13px; color: #888; margin: 0; line-height: 1.7; }
    .profile-badge {
      display: inline-block;
      padding: 2px 10px; border-radius: 999px;
      font-size: 12px; font-weight: 600;
      background: #e8f0fe; color: #2a5fca;
      margin-right: 6px;
    }
    .profile-img-form { display: flex; align-items: center; gap: 8px; margin-top: 10px; }
    .btn-upload-img {
      padding: 6px 14px; border-radius: 6px;
      background: #f3f4f6; border: 1px solid #ddd;
      font-size: 13px; color: #555; cursor: pointer;
      white-space: nowrap;
    }
    .btn-upload-img:hover { background: #e9ebee; }

    /* ── 섹션 카드 ── */
    .section-card {
      background: #fff;
      border-radius: 14px;
      box-shadow: 0 1px 6px rgba(0,0,0,.06);
      padding: 22px 24px;
      margin-bottom: 20px;
    }
    .section-card__title {
      font-size: 16px; font-weight: 700;
      margin: 0 0 18px;
      padding-bottom: 12px;
      border-bottom: 1px solid #f0f0f0;
    }

    /* ── 폼 공통 ── */
    .form-row { display: flex; flex-direction: column; margin-bottom: 14px; }
    .form-row:last-child { margin-bottom: 0; }
    .form-row label {
      font-size: 12px; font-weight: 600; color: #888;
      margin-bottom: 5px; text-transform: uppercase; letter-spacing: .3px;
    }
    .form-row input[type="text"],
    .form-row input[type="email"],
    .form-row input[type="password"],
    .form-row input[type="tel"] {
      width: 100%; padding: 9px 12px;
      border: 1px solid #ddd; border-radius: 7px;
      font-size: 14px; color: #333;
    }
    .form-row input:focus { outline: none; border-color: #4a90d9; }
    .form-row input[readonly] { background: #f8f9fa; color: #888; cursor: not-allowed; }
    .form-row .hint { font-size: 11px; color: #bbb; margin-top: 3px; }
    .btn-save {
      display: inline-block; padding: 9px 22px; border-radius: 7px;
      background: #3a6de8; color: #fff; border: none;
      font-size: 14px; font-weight: 600; cursor: pointer; margin-top: 6px;
    }
    .btn-save:hover { background: #2c59c9; }

    /* ── 아이 탭 ── */
    .child-tabs { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 18px; }
    .child-tab { display: inline-block; padding: 7px 16px; border-radius: 999px;
                 font-size: 13px; text-decoration: none; border: 1px solid #ddd;
                 background: #fff; color: #555; transition: all .15s; }
    .child-tab.active { background: #4a90d9; color: #fff; border-color: #4a90d9; font-weight: 600; }
    .child-tab:hover:not(.active) { border-color: #4a90d9; color: #4a90d9; }

    /* ── 실행 카드 ── */
    .run-list { display: flex; flex-direction: column; gap: 10px; }
    .run-card { background: #f8f9fa; border-radius: 10px; padding: 14px 16px;
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
    .run-title { font-size: 14px; font-weight: 600;
                 white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .run-date  { font-size: 12px; color: #999; margin-top: 2px; }
    .btn-run-action { display: inline-block; padding: 7px 16px; border-radius: 8px;
                      font-size: 13px; text-decoration: none; white-space: nowrap; }
    .btn-result { background: #4a90d9; color: #fff; }
    .btn-result:hover { background: #3a7bc8; }
    .btn-resume { background: #27ae60; color: #fff; }
    .btn-resume:hover { background: #219a52; }

    /* ── 즐겨찾기 센터 칩 ── */
    .center-chips { display: flex; flex-wrap: wrap; gap: 8px; }
    .center-chip { display: inline-block; padding: 8px 16px; border-radius: 999px;
                   background: #f8f9fa; border: 1px solid #ddd;
                   font-size: 13px; color: #333; text-decoration: none;
                   max-width: 200px; white-space: nowrap; overflow: hidden;
                   text-overflow: ellipsis; transition: all .15s; }
    .center-chip::before { content: '★ '; color: #f39c12; }
    .center-chip:hover { border-color: #4a90d9; color: #4a90d9; }

    /* ── 빈 상태 ── */
    .empty-mini { padding: 20px; text-align: center; color: #bbb;
                  background: #f8f9fa; border-radius: 10px;
                  font-size: 13px; }
    .empty-full { text-align: center; padding: 48px 20px; color: #aaa;
                  background: #fff; border-radius: 14px;
                  box-shadow: 0 1px 6px rgba(0,0,0,.06); }
    .empty-full .icon { font-size: 44px; display: block; margin-bottom: 12px; }
    .empty-full p { margin: 6px 0; font-size: 15px; }

    /* ── CTA ── */
    .cta-bar { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 12px; }
    .btn-cta { display: inline-block; padding: 9px 20px; border-radius: 8px;
               font-size: 14px; text-decoration: none; }
    .btn-cta-primary { background: #4a90d9; color: #fff; }
    .btn-cta-primary:hover { background: #3a7bc8; }
    .btn-cta-outline { background: #fff; color: #4a90d9; border: 1px solid #4a90d9; }
    .btn-cta-outline:hover { background: #f0f6ff; }

    @media (max-width: 500px) {
      .profile-card { flex-direction: column; align-items: flex-start; }
      .run-card { flex-direction: column; align-items: flex-start; }
      .btn-run-action { width: 100%; text-align: center; }
    }
  </style>
</head>
<body>

<%@ include file="/WEB-INF/jsp/usr/common/header.jsp" %>

<div class="page-wrap">

  <%-- 알림 메시지 --%>
  <c:if test="${not empty msg}">
    <div class="alert alert-success"><c:out value="${msg}" /></div>
  </c:if>
  <c:if test="${not empty error}">
    <div class="alert alert-error"><c:out value="${error}" /></div>
  </c:if>

  <%-- ═══════════════════════════════════════════
       ① 프로필 카드
  ══════════════════════════════════════════════ --%>
  <div class="profile-card">
    <%-- 프로필 이미지 --%>
    <c:choose>
      <c:when test="${not empty loginedUser.profileImage}">
        <img src="/uploads/profile/${loginedUser.profileImage}"
             alt="프로필 사진" class="profile-avatar" />
      </c:when>
      <c:otherwise>
        <div class="profile-avatar-placeholder">👤</div>
      </c:otherwise>
    </c:choose>

    <div class="profile-info">
      <p class="profile-name"><c:out value="${loginedUser.name}" /></p>
      <p class="profile-meta">
        <span class="profile-badge">
          <c:choose>
            <c:when test="${loginedUser.memberType eq 'GENERAL'}">일반회원</c:when>
            <c:otherwise>보호자</c:otherwise>
          </c:choose>
        </span>
        <c:if test="${not empty loginedUser.displayRole}">
          <c:out value="${loginedUser.displayRole}" /> ·
        </c:if>
        <c:out value="${loginedUser.email}" /><br/>
        <c:if test="${not empty loginedUser.phone}">
          <c:out value="${loginedUser.phone}" /> ·
        </c:if>
        가입일: <c:out value="${loginedUser.regDate}" />
      </p>

      <%-- 프로필 사진 변경 폼 --%>
      <form action="/usr/my/doUploadProfileImage" method="post"
            enctype="multipart/form-data" class="profile-img-form">
        <input type="file" name="profileImage" id="profileImageFile"
               accept="image/jpeg,image/png,image/gif"
               style="display:none;"
               onchange="this.form.submit();" />
        <label for="profileImageFile" class="btn-upload-img">📷 사진 변경</label>
      </form>
    </div>
  </div>

  <%-- ═══════════════════════════════════════════
       ② 계정 정보 수정
  ══════════════════════════════════════════════ --%>
  <div class="section-card">
    <h2 class="section-card__title">계정 정보</h2>
    <form action="/usr/my/doUpdateProfile" method="post">
      <div class="form-row">
        <label>아이디</label>
        <input type="text" value="<c:out value='${loginedUser.loginId}'/>" readonly />
      </div>
      <div class="form-row">
        <label>이름 *</label>
        <input type="text" name="name" value="<c:out value='${loginedUser.name}'/>"
               required maxlength="50" />
      </div>
      <div class="form-row">
        <label>이메일</label>
        <input type="email" value="<c:out value='${loginedUser.email}'/>" readonly />
        <span class="hint">이메일은 변경할 수 없습니다.</span>
      </div>
      <div class="form-row">
        <label>전화번호</label>
        <input type="tel" name="phone" value="<c:out value='${loginedUser.phone}'/>"
               placeholder="010-1234-5678 (선택)" maxlength="20" />
      </div>
      <button type="submit" class="btn-save">저장</button>
    </form>
  </div>

  <%-- ═══════════════════════════════════════════
       ③ 비밀번호 변경
  ══════════════════════════════════════════════ --%>
  <div class="section-card">
    <h2 class="section-card__title">비밀번호 변경</h2>
    <form action="/usr/my/doChangePassword" method="post">
      <div class="form-row">
        <label>현재 비밀번호</label>
        <input type="password" name="currentPw" required placeholder="현재 비밀번호 입력" />
      </div>
      <div class="form-row">
        <label>새 비밀번호</label>
        <input type="password" name="newPw" required
               placeholder="8자 이상" minlength="8" maxlength="64" />
        <span class="hint">8~64자, 영문·숫자·특수문자 조합 권장</span>
      </div>
      <div class="form-row">
        <label>새 비밀번호 확인</label>
        <input type="password" name="newPwConfirm" required
               placeholder="새 비밀번호 재입력" />
      </div>
      <button type="submit" class="btn-save">비밀번호 변경</button>
    </form>
  </div>

  <%-- ═══════════════════════════════════════════
       ④ 내 기록 허브
  ══════════════════════════════════════════════ --%>

  <%-- 아이가 없을 때 전체 빈 상태 --%>
  <c:if test="${empty children}">
    <div class="empty-full">
      <span class="icon">👶</span>
      <p>등록된 아이 프로필이 없습니다.</p>
      <p style="font-size:13px;color:#bbb;">아이 프로필을 먼저 추가해 체크리스트를 시작해 보세요.</p>
      <div class="cta-bar" style="justify-content:center;margin-top:18px;">
        <a href="/usr/child/write" class="btn-cta btn-cta-primary">아이 프로필 추가 →</a>
        <a href="/usr/center/list" class="btn-cta btn-cta-outline">센터 둘러보기</a>
      </div>
    </div>
  </c:if>

  <c:if test="${not empty children}">

    <%-- 아이 탭 --%>
    <div class="child-tabs">
      <a href="/usr/my" class="child-tab ${selectedChildId == 0 ? 'active' : ''}">전체</a>
      <c:forEach items="${children}" var="ch">
        <a href="/usr/my?childId=${ch.id}"
           class="child-tab ${selectedChildId == ch.id ? 'active' : ''}">
          <c:out value="${ch.name}" />
        </a>
      </c:forEach>
    </div>

    <%-- ── 섹션: 최근 제출 결과 ── --%>
    <div class="section-card">
      <h2 class="section-card__title">최근 제출 결과</h2>
      <c:choose>
        <c:when test="${empty submittedRuns}">
          <div class="empty-mini">📄 아직 제출한 검사 결과가 없습니다.</div>
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

    <%-- ── 섹션: 이어하기 (임시저장) ── --%>
    <div class="section-card">
      <h2 class="section-card__title">이어하기 (임시저장)</h2>
      <c:choose>
        <c:when test="${empty draftRuns}">
          <div class="empty-mini">✏️ 임시저장된 검사가 없습니다.</div>
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

    <%-- ── 섹션: 즐겨찾기 센터 ── --%>
    <div class="section-card">
      <h2 class="section-card__title" style="display:flex;justify-content:space-between;align-items:center;">
        즐겨찾기 센터
        <a href="/usr/center/favorites" style="font-size:13px;color:#4a90d9;font-weight:400;text-decoration:none;">전체보기 →</a>
      </h2>
      <c:choose>
        <c:when test="${empty favoriteCenters}">
          <div class="empty-mini">☆ 즐겨찾기한 센터가 없습니다.</div>
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
