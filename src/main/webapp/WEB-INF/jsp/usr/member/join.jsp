<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"    uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>회원가입</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body    { font-family: Arial, sans-serif; margin: 0; background: #f5f5f5; }
    .wrap   { max-width: 480px; margin: 60px auto; background: #fff;
              border: 1px solid #ddd; border-radius: 10px; padding: 36px 40px; }
    h2      { margin: 0 0 24px; font-size: 22px; }
    .field  { margin-bottom: 18px; }
    label   { display: block; font-size: 13px; font-weight: bold;
              margin-bottom: 5px; color: #333; }
    input[type="text"],
    input[type="email"],
    input[type="password"],
    select  { width: 100%; padding: 9px 12px; border: 1px solid #ccc;
              border-radius: 6px; font-size: 14px; }
    input.error-field,
    select.error-field { border-color: #e74c3c; }
    .err    { display: block; margin-top: 4px; font-size: 12px; color: #e74c3c; }
    .hint   { display: block; margin-top: 3px; font-size: 11px; color: #999; }
    .btn    { width: 100%; padding: 11px; background: #4a90d9; color: #fff;
              border: none; border-radius: 6px; font-size: 15px; cursor: pointer;
              margin-top: 8px; }
    .btn:hover { background: #3a7bc8; }
    .footer-link { margin-top: 16px; text-align: center; font-size: 13px; }
    .footer-link a { color: #4a90d9; text-decoration: none; }
    .notice { margin-bottom: 18px; padding: 10px 14px;
              background: #fff8e1; border: 1px solid #ffe082;
              border-radius: 6px; font-size: 13px; color: #7a5c00; }
  </style>
</head>
<body>

<div class="wrap">
  <h2>회원가입</h2>

  <%-- 가입 성공 후 로그인 페이지에서 ?joined=1 파라미터로 이미 처리되므로 여기선 불필요.
       별도 메시지가 전달된 경우만 출력 (예: 외부 redirect 시) --%>
  <c:if test="${not empty param.error}">
    <div class="notice">${param.error}</div>
  </c:if>

  <%-- Spring form taglib: modelAttribute="joinForm" 이 모델에 있어야 렌더링 가능 --%>
  <form:form method="post" action="/usr/member/doJoin" modelAttribute="joinForm">

    <%-- 아이디 --%>
    <div class="field">
      <label for="loginId">아이디 <span style="color:#e74c3c;">*</span></label>
      <form:input path="loginId" id="loginId" type="text"
                  placeholder="4~20자 영문·숫자" cssErrorClass="error-field" />
      <form:errors path="loginId" cssClass="err" element="span" />
      <span class="hint">4~20자, 영문 소문자·숫자 조합 권장</span>
    </div>

    <%-- 이름 --%>
    <div class="field">
      <label for="name">이름 <span style="color:#e74c3c;">*</span></label>
      <form:input path="name" id="name" type="text"
                  placeholder="실명 또는 닉네임" cssErrorClass="error-field" />
      <form:errors path="name" cssClass="err" element="span" />
    </div>

    <%-- 이메일 --%>
    <div class="field">
      <label for="email">이메일 <span style="color:#e74c3c;">*</span></label>
      <form:input path="email" id="email" type="email"
                  placeholder="example@email.com" cssErrorClass="error-field" />
      <form:errors path="email" cssClass="err" element="span" />
    </div>

    <%-- 비밀번호 --%>
    <div class="field">
      <label for="loginPw">비밀번호 <span style="color:#e74c3c;">*</span></label>
      <form:password path="loginPw" id="loginPw"
                     placeholder="8자 이상" cssErrorClass="error-field" />
      <form:errors path="loginPw" cssClass="err" element="span" />
      <span class="hint">8자 이상, 영문·숫자·특수문자 조합 권장</span>
    </div>

    <%-- 비밀번호 확인 --%>
    <div class="field">
      <label for="loginPwConfirm">비밀번호 확인 <span style="color:#e74c3c;">*</span></label>
      <form:password path="loginPwConfirm" id="loginPwConfirm"
                     placeholder="비밀번호 재입력" cssErrorClass="error-field" />
      <form:errors path="loginPwConfirm" cssClass="err" element="span" />
    </div>

    <%-- 역할 (선택) --%>
    <div class="field">
      <label for="role">역할</label>
      <form:select path="role" id="role">
        <form:option value="GUARDIAN">보호자 (GUARDIAN)</form:option>
        <form:option value="THERAPIST">치료사 (THERAPIST)</form:option>
      </form:select>
    </div>

    <button type="submit" class="btn">가입하기</button>

  </form:form>

  <div class="footer-link">
    이미 계정이 있으신가요? <a href="/usr/member/login">로그인</a>
  </div>
</div>

</body>
</html>
