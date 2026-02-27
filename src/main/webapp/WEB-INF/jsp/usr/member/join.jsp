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
    .wrap   { max-width: 520px; margin: 60px auto; background: #fff;
              border: 1px solid #ddd; border-radius: 10px; padding: 36px 40px; }
    h2      { margin: 0 0 24px; font-size: 22px; }
    .field  { margin-bottom: 18px; }
    label   { display: block; font-size: 13px; font-weight: bold;
              margin-bottom: 5px; color: #333; }
    input[type="text"],
    input[type="email"],
    input[type="tel"],
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
    .type-tabs { display: flex; gap: 8px; margin-bottom: 4px; }
    .type-tab  { flex: 1; padding: 9px; border: 2px solid #ccc; border-radius: 6px;
                 background: #fff; font-size: 14px; cursor: pointer; text-align: center;
                 transition: all .15s; }
    .type-tab.active { border-color: #4a90d9; background: #eaf3fb; color: #2a70b9; font-weight: bold; }
    .general-fields { display: none; }
  </style>
</head>
<body>

<div class="wrap">
  <h2>회원가입</h2>

  <c:if test="${not empty param.error}">
    <div class="notice">${param.error}</div>
  </c:if>

  <form:form method="post" action="/usr/member/doJoin" modelAttribute="joinForm">

    <form:errors path="" cssClass="notice" element="div" delimiter="<br/>" />

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

    <%-- 전화번호 (선택) --%>
    <div class="field">
      <label for="phone">전화번호</label>
      <form:input path="phone" id="phone" type="tel"
                  placeholder="010-1234-5678 (선택)" cssErrorClass="error-field" />
      <form:errors path="phone" cssClass="err" element="span" />
      <span class="hint">선택 입력 — 010-xxxx-xxxx 형식 권장</span>
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

    <%-- 회원 유형 --%>
    <div class="field">
      <label>회원 유형 <span style="color:#e74c3c;">*</span></label>
      <div class="type-tabs">
        <div class="type-tab active" id="tab-guardian" onclick="selectMemberType('GUARDIAN')">
          보호자<br/><small style="font-weight:normal;color:#666;">아동 보호자·가족</small>
        </div>
        <div class="type-tab" id="tab-general" onclick="selectMemberType('GENERAL')">
          일반회원<br/><small style="font-weight:normal;color:#666;">치료사·센터·기관 등</small>
        </div>
      </div>
      <form:hidden path="memberType" id="memberType" />
    </div>

    <%-- 일반회원 추가 정보 (GENERAL 선택 시 노출) --%>
    <div class="general-fields" id="generalFields">
      <div class="field">
        <label for="displayRole">역할 구분</label>
        <form:select path="displayRole" id="displayRole">
          <form:option value="">-- 선택 (선택 사항) --</form:option>
          <form:option value="치료사">치료사</form:option>
          <form:option value="센터">센터</form:option>
          <form:option value="기관">기관</form:option>
          <form:option value="기타">기타</form:option>
        </form:select>
        <form:errors path="displayRole" cssClass="err" element="span" />
      </div>
      <div class="field">
        <label for="orgName">소속 기관명</label>
        <form:input path="orgName" id="orgName" type="text"
                    placeholder="기관명 (선택 사항)" cssErrorClass="error-field" />
        <form:errors path="orgName" cssClass="err" element="span" />
      </div>
    </div>

    <%-- 역할 (기존 호환 — hidden) --%>
    <form:hidden path="role" />

    <button type="submit" class="btn">가입하기</button>

  </form:form>

  <div class="footer-link">
    이미 계정이 있으신가요? <a href="/usr/member/login">로그인</a>
  </div>
</div>

<script>
  // 페이지 로드 시 초기화
  (function() {
    var mt = document.getElementById('memberType').value;
    if (mt === 'GENERAL') {
      selectMemberType('GENERAL');
    } else {
      selectMemberType('GUARDIAN');
    }
  })();

  function selectMemberType(type) {
    document.getElementById('memberType').value = type;
    var gTab = document.getElementById('tab-guardian');
    var genTab = document.getElementById('tab-general');
    var genFields = document.getElementById('generalFields');

    if (type === 'GENERAL') {
      gTab.classList.remove('active');
      genTab.classList.add('active');
      genFields.style.display = 'block';
    } else {
      gTab.classList.add('active');
      genTab.classList.remove('active');
      genFields.style.display = 'none';
    }
  }
</script>

</body>
</html>
