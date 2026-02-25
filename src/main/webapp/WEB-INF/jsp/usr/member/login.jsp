<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    Object msgObj = request.getAttribute("msg");
    String msg = msgObj == null ? null : String.valueOf(msgObj);
%>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>로그인</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body   { font-family: Arial, sans-serif; margin: 0; background: #f5f5f5; }
    .wrap  { max-width: 400px; margin: 80px auto; background: #fff;
             border: 1px solid #ddd; border-radius: 10px; padding: 36px 40px; }
    h2     { margin: 0 0 24px; font-size: 22px; }
    .field { margin-bottom: 16px; }
    label  { display: block; font-size: 13px; font-weight: bold;
             margin-bottom: 5px; color: #333; }
    input  { width: 100%; padding: 9px 12px; border: 1px solid #ccc;
             border-radius: 6px; font-size: 14px; }
    .btn   { width: 100%; padding: 11px; background: #4a90d9; color: #fff;
             border: none; border-radius: 6px; font-size: 15px; cursor: pointer;
             margin-top: 8px; }
    .btn:hover { background: #3a7bc8; }
    .msg-error   { margin-bottom: 14px; padding: 10px 14px;
                   background: #fff3cd; border: 1px solid #ffeeba;
                   border-radius: 6px; font-size: 13px; color: #7a5c00; }
    .msg-success { margin-bottom: 14px; padding: 10px 14px;
                   background: #d4edda; border: 1px solid #c3e6cb;
                   border-radius: 6px; font-size: 13px; color: #155724; }
    .footer-link { margin-top: 16px; text-align: center; font-size: 13px; }
    .footer-link a { color: #4a90d9; text-decoration: none; }
  </style>
</head>
<body>

<div class="wrap">
  <h2>로그인</h2>

  <%-- 가입 완료 후 redirect 파라미터 메시지 --%>
  <c:if test="${param.joined eq '1'}">
    <div class="msg-success">회원가입이 완료됐습니다. 로그인해 주세요.</div>
  </c:if>

  <%-- 로그인 실패 메시지 (req.setAttribute 로 전달) --%>
  <% if (msg != null && !msg.trim().isEmpty()) { %>
    <div class="msg-error"><%= msg %></div>
  <% } %>

  <form method="post" action="/usr/member/doLogin">
    <div class="field">
      <label for="loginId">아이디</label>
      <input type="text" id="loginId" name="loginId" placeholder="아이디" />
    </div>
    <div class="field">
      <label for="loginPw">비밀번호</label>
      <input type="password" id="loginPw" name="loginPw" placeholder="비밀번호" />
    </div>
    <button type="submit" class="btn">로그인</button>
  </form>

  <div class="footer-link">
    계정이 없으신가요? <a href="/usr/member/join">회원가입</a>
  </div>
</div>

</body>
</html>
