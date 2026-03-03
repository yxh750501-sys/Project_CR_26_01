<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>로그인 - LittleSteps</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }

    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
      margin: 0;
      background: linear-gradient(135deg, #e8f0fe 0%, #f5f9ff 100%);
      min-height: 100vh;
      display: flex; align-items: center; justify-content: center;
      padding: 24px 16px;
    }

    /* ── 카드 (가로 넓게) ── */
    .card {
      background: #fff;
      border-radius: 18px;
      box-shadow: 0 4px 28px rgba(74, 144, 217, .13);
      padding: 40px 44px 36px;
      width: 100%;
      max-width: 780px;
    }

    /* ── 브랜드 헤더 ── */
    .brand { text-align: center; margin-bottom: 28px; }
    .brand img { height: 46px; display: block; margin: 0 auto 10px; object-fit: contain; }
    .brand-name { font-size: 21px; font-weight: 700; color: #2c3e50; letter-spacing: -.3px; }
    .brand-desc { font-size: 13px; color: #7f8c8d; margin-top: 5px; line-height: 1.6; }

    /* ── 알림 ── */
    .msg { border-radius: 8px; padding: 10px 14px; margin-bottom: 20px;
           font-size: 13px; line-height: 1.5; }
    .msg-error   { background: #fff3cd; border: 1px solid #ffd54f; color: #7a5c00; }
    .msg-success { background: #d4edda; border: 1px solid #81c784; color: #155724; }
    .msg-danger  { background: #fdecea; border: 1px solid #f5c6cb; color: #842029; }

    /* ── 2컬럼 레이아웃 ── */
    .login-columns {
      display: flex;
      align-items: stretch;
    }

    /* 왼쪽: 소셜 */
    .col-social {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 16px 36px;
      gap: 14px;
    }
    .col-label {
      font-size: 12px;
      font-weight: 700;
      letter-spacing: .06em;
      color: #aaa;
      text-transform: uppercase;
      margin-bottom: 4px;
    }

    /* Google 버튼 */
    .btn-google {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      width: 100%;
      max-width: 260px;
      padding: 13px 18px;
      border: 1.5px solid #dadce0;
      border-radius: 10px;
      background: #fff;
      font-size: 15px;
      font-weight: 600;
      color: #3c4043;
      cursor: pointer;
      font-family: inherit;
      transition: background .13s, border-color .13s, box-shadow .13s;
    }
    .btn-google:hover:not(:disabled) {
      background: #f6f9ff;
      border-color: #b0c8f0;
      box-shadow: 0 2px 10px rgba(74,144,217,.15);
    }
    .btn-google:active:not(:disabled) {
      background: #edf3ff;
      box-shadow: none;
    }
    .btn-google:disabled {
      opacity: .45;
      cursor: not-allowed;
    }
    .google-note {
      font-size: 11px;
      color: #bbb;
      text-align: center;
      margin: 0;
      line-height: 1.5;
    }

    /* ── 세로 OR 구분선 ── */
    .or-divider {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 0 8px;
      flex-shrink: 0;
    }
    .or-line {
      flex: 1;
      width: 1px;
      background: #ebebeb;
    }
    .or-circle {
      width: 32px; height: 32px;
      border-radius: 50%;
      background: #f5f6f8;
      border: 1px solid #e4e4e4;
      display: flex; align-items: center; justify-content: center;
      font-size: 10px; font-weight: 700; color: #c0c0c0;
      flex-shrink: 0;
      margin: 8px 0;
      letter-spacing: 0;
    }

    /* 오른쪽: 일반 로그인 */
    .col-form {
      flex: 1;
      padding: 16px 36px;
    }
    .field { margin-bottom: 14px; }
    label  { display: block; font-size: 13px; font-weight: 600;
             margin-bottom: 5px; color: #444; }
    input[type="text"], input[type="password"] {
      width: 100%; padding: 10px 13px; border: 1px solid #d0d5dd;
      border-radius: 8px; font-size: 14px; font-family: inherit;
      transition: border-color .15s, box-shadow .15s;
    }
    input:focus {
      outline: none; border-color: #4a90d9;
      box-shadow: 0 0 0 3px rgba(74,144,217,.12);
    }
    .btn-login {
      width: 100%; padding: 12px; background: #4a90d9; color: #fff;
      border: none; border-radius: 8px; font-size: 15px;
      font-family: inherit; cursor: pointer; margin-top: 4px;
      font-weight: 600; transition: background .12s;
    }
    .btn-login:hover { background: #3a7bc8; }
    .join-link {
      margin-top: 14px; text-align: center;
      font-size: 13px; color: #666;
    }
    .join-link a { color: #4a90d9; text-decoration: none; font-weight: 600; }
    .join-link a:hover { text-decoration: underline; }

    /* ── 안내 문구 ── */
    .notice {
      margin-top: 24px; padding: 10px 14px;
      background: #f0f7ff;
      border-left: 3px solid #4a90d9;
      border-radius: 0 6px 6px 0;
      font-size: 12px; color: #5a7fa0; line-height: 1.6;
    }

    /* ── 반응형: 620px 이하 세로 스택 ── */
    @media (max-width: 620px) {
      .card { padding: 32px 20px 28px; }
      .login-columns { flex-direction: column; }
      .col-social { padding: 16px 0 20px; }
      .col-form   { padding: 0; }
      .btn-google { max-width: 100%; }
      /* 세로 → 가로 구분선 */
      .or-divider { flex-direction: row; padding: 6px 0; }
      .or-line    { flex: 1; width: auto; height: 1px; }
      .or-circle  { margin: 0 10px; }
    }
  </style>
</head>
<body>

<%-- GIS 초기화 div (client-id 설정 시만) --%>
<c:if test="${not empty googleClientId}">
  <div id="g_id_onload"
       data-client_id="${googleClientId}"
       data-callback="onGoogleSignIn"
       data-auto_prompt="false"
       data-itp_support="true"></div>
</c:if>

<%-- Google G 로고 SVG 심볼 정의 --%>
<svg style="display:none" xmlns="http://www.w3.org/2000/svg">
  <symbol id="ic-google" viewBox="0 0 24 24">
    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
  </symbol>
</svg>

<div class="card">

  <%-- 브랜드 헤더 --%>
  <div class="brand">
    <img src="/img/LittleSteps.png" alt="LittleSteps"
         onerror="this.style.display='none'" />
    <div class="brand-name">LittleSteps</div>
    <div class="brand-desc">아이의 발달을 함께 살펴보고 맞는 지원 기관을 찾아드립니다.</div>
  </div>

  <%-- 알림 메시지 --%>
  <c:if test="${param.joined eq '1'}">
    <div class="msg msg-success">회원가입이 완료됐습니다. 로그인해 주세요.</div>
  </c:if>
  <c:if test="${not empty googleError}">
    <div class="msg msg-danger"><c:out value="${googleError}"/></div>
  </c:if>
  <c:if test="${not empty msg}">
    <div class="msg msg-error"><c:out value="${msg}"/></div>
  </c:if>

  <%-- ── 2컬럼 ── --%>
  <div class="login-columns">

    <%-- 왼쪽: 소셜 로그인 --%>
    <div class="col-social">
      <p class="col-label">소셜 로그인</p>

      <c:choose>
        <c:when test="${not empty googleClientId}">
          <button class="btn-google" onclick="handleGoogleLogin()">
            <svg width="20" height="20" aria-hidden="true">
              <use href="#ic-google"/>
            </svg>
            Google로 로그인
          </button>
        </c:when>
        <c:otherwise>
          <button class="btn-google" disabled>
            <svg width="20" height="20" aria-hidden="true">
              <use href="#ic-google"/>
            </svg>
            Google로 로그인
          </button>
          <p class="google-note">관리자가 client-id를 설정해야 사용 가능</p>
        </c:otherwise>
      </c:choose>
    </div>

    <%-- 가운데: 세로 OR 구분선 --%>
    <div class="or-divider">
      <div class="or-line"></div>
      <div class="or-circle">OR</div>
      <div class="or-line"></div>
    </div>

    <%-- 오른쪽: 아이디 로그인 --%>
    <div class="col-form">
      <p class="col-label">아이디로 로그인</p>

      <form method="post" action="/usr/member/doLogin">
        <div class="field">
          <label for="loginId">아이디</label>
          <input type="text" id="loginId" name="loginId"
                 placeholder="아이디를 입력해 주세요."
                 autocomplete="username" />
        </div>
        <div class="field">
          <label for="loginPw">비밀번호</label>
          <input type="password" id="loginPw" name="loginPw"
                 placeholder="비밀번호를 입력해 주세요."
                 autocomplete="current-password" />
        </div>
        <button type="submit" class="btn-login">로그인</button>
      </form>

      <div class="join-link">
        계정이 없으신가요? <a href="/usr/member/join">회원가입</a>
      </div>
    </div>

  </div><%-- /.login-columns --%>

  <%-- 안내 문구 --%>
  <div class="notice">
    ※ 체크리스트 결과는 참고용이며, 아이의 발달에 대한 정확한 판단은
    전문가 상담·평가를 권장합니다.
  </div>

</div><%-- /.card --%>

<%-- GIS JS + credential 전송 폼 --%>
<c:if test="${not empty googleClientId}">
  <script src="https://accounts.google.com/gsi/client" async></script>
  <form id="googleLoginForm" method="post" action="/usr/member/doGoogleLogin"
        style="display:none">
    <input type="hidden" id="googleCredential" name="credential" />
  </form>
  <script>
    function handleGoogleLogin() {
      if (typeof google === 'undefined' || !google.accounts) {
        alert('Google 라이브러리가 아직 로딩 중입니다. 잠시 후 다시 시도해 주세요.');
        return;
      }
      google.accounts.id.prompt(function(notification) {
        if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
          alert('Google 로그인 팝업이 표시되지 않았습니다.\n팝업 차단을 해제하거나 잠시 후 다시 시도해 주세요.');
        }
      });
    }
    function onGoogleSignIn(response) {
      document.getElementById('googleCredential').value = response.credential;
      document.getElementById('googleLoginForm').submit();
    }
  </script>
</c:if>

</body>
</html>
