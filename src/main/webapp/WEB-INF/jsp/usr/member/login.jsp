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
    .card {
      background: #fff; border-radius: 16px;
      box-shadow: 0 4px 24px rgba(74, 144, 217, .15);
      padding: 44px 40px 36px; width: 100%; max-width: 400px;
    }

    /* ── 브랜드 헤더 ── */
    .brand { text-align: center; margin-bottom: 28px; }
    .brand img { height: 52px; display: block; margin: 0 auto 10px;
                 object-fit: contain; }
    .brand-name { font-size: 22px; font-weight: 700; color: #2c3e50;
                  letter-spacing: -.3px; }
    .brand-desc { font-size: 13px; color: #7f8c8d; margin-top: 6px;
                  line-height: 1.6; }

    /* ── 알림 ── */
    .msg { border-radius: 8px; padding: 10px 14px; margin-bottom: 14px;
           font-size: 13px; line-height: 1.5; }
    .msg-error   { background: #fff3cd; border: 1px solid #ffeeba; color: #7a5c00; }
    .msg-success { background: #d4edda; border: 1px solid #c3e6cb; color: #155724; }
    .msg-danger  { background: #fdecea; border: 1px solid #f5c6cb; color: #842029; }

    /* ── 폼 ── */
    .field { margin-bottom: 14px; }
    label  { display: block; font-size: 13px; font-weight: 600;
             margin-bottom: 5px; color: #444; }
    input[type="text"], input[type="password"] {
      width: 100%; padding: 10px 13px; border: 1px solid #ccc;
      border-radius: 8px; font-size: 14px; font-family: inherit;
      transition: border-color .15s;
    }
    input:focus { outline: none; border-color: #4a90d9;
                  box-shadow: 0 0 0 3px rgba(74,144,217,.12); }

    .btn-login {
      width: 100%; padding: 12px; background: #4a90d9; color: #fff;
      border: none; border-radius: 8px; font-size: 15px;
      font-family: inherit; cursor: pointer; margin-top: 6px;
      font-weight: 600; transition: background .12s;
    }
    .btn-login:hover { background: #3a7bc8; }

    /* ── 구분선 ── */
    .divider { display: flex; align-items: center; gap: 12px; margin: 20px 0; }
    .divider::before, .divider::after {
      content: ''; flex: 1; height: 1px; background: #e0e0e0;
    }
    .divider span { font-size: 12px; color: #aaa; white-space: nowrap; }

    /* ── Google 버튼 영역 ── */
    .google-btn-wrap { display: flex; justify-content: center; min-height: 44px;
                       align-items: center; }
    .google-unavail { font-size: 12px; color: #bbb; text-align: center; }

    /* ── 하단 ── */
    .footer-row { margin-top: 20px; text-align: center; font-size: 13px; color: #666; }
    .footer-row a { color: #4a90d9; text-decoration: none; font-weight: 600; }
    .footer-row a:hover { text-decoration: underline; }

    /* ── 안내 문구 ── */
    .notice {
      margin-top: 20px; padding: 10px 14px;
      background: #f0f7ff;
      border-left: 3px solid #4a90d9;
      border-radius: 0 6px 6px 0;
      font-size: 12px; color: #5a7fa0; line-height: 1.6;
    }
  </style>
</head>
<body>

<div class="card">

  <%-- ── 브랜드 헤더 ── --%>
  <div class="brand">
    <img src="/img/LittleSteps.png" alt="LittleSteps"
         onerror="this.style.display='none'" />
    <div class="brand-name">LittleSteps</div>
    <div class="brand-desc">
      아이의 발달을 함께 살펴보고<br>맞는 지원 기관을 찾아드립니다.
    </div>
  </div>

  <%-- 가입 완료 메시지 --%>
  <c:if test="${param.joined eq '1'}">
    <div class="msg msg-success">회원가입이 완료됐습니다. 로그인해 주세요.</div>
  </c:if>

  <%-- Google 로그인 실패 메시지 (RedirectAttributes flash) --%>
  <c:if test="${not empty googleError}">
    <div class="msg msg-danger"><c:out value="${googleError}"/></div>
  </c:if>

  <%-- 일반 로그인 실패 메시지 (request attribute) --%>
  <c:if test="${not empty msg}">
    <div class="msg msg-error"><c:out value="${msg}"/></div>
  </c:if>

  <%-- ── 아이디/비밀번호 로그인 폼 ── --%>
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

  <%-- ── 구분선 ── --%>
  <div class="divider"><span>또는</span></div>

  <%-- ── Google 로그인 버튼 ── --%>
  <div class="google-btn-wrap">
    <c:choose>
      <c:when test="${not empty googleClientId}">
        <div id="g_id_onload"
             data-client_id="${googleClientId}"
             data-callback="onGoogleSignIn"
             data-auto_prompt="false"
             data-itp_support="true"></div>
        <div class="g_id_signin"
             data-type="standard"
             data-size="large"
             data-theme="outline"
             data-text="signin_with"
             data-shape="rectangular"
             data-logo_alignment="left"
             data-width="340"></div>
      </c:when>
      <c:otherwise>
        <span class="google-unavail">
          Google 로그인을 사용하려면<br>
          application.properties에 google.oauth.client-id 를 입력해 주세요.
        </span>
      </c:otherwise>
    </c:choose>
  </div>

  <%-- ── 회원가입 링크 ── --%>
  <div class="footer-row">
    계정이 없으신가요? <a href="/usr/member/join">회원가입</a>
  </div>

  <%-- ── 서비스 안내 ── --%>
  <div class="notice">
    ※ 체크리스트 결과는 참고용이며, 아이의 발달에 대한 정확한 판단은
    전문가 상담·평가를 권장합니다.
  </div>

</div>

<%-- Google Identity Services JS (client-id 가 설정된 경우만 로드) --%>
<c:if test="${not empty googleClientId}">
  <script src="https://accounts.google.com/gsi/client" async></script>
  <form id="googleLoginForm" method="post" action="/usr/member/doGoogleLogin"
        style="display:none">
    <input type="hidden" id="googleCredential" name="credential" />
  </form>
  <script>
    function onGoogleSignIn(response) {
      document.getElementById('googleCredential').value = response.credential;
      document.getElementById('googleLoginForm').submit();
    }
  </script>
</c:if>

</body>
</html>
