<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>안내</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 40px; background: #f9f9f9; }
    .box { max-width: 480px; margin: 0 auto; border: 1px solid #ddd; padding: 24px;
           border-radius: 10px; background: #fff; text-align: center; }
    .msg { font-size: 15px; color: #333; margin-bottom: 20px; }
    .btn { display: inline-block; padding: 10px 22px; background: #333; color: #fff;
           border-radius: 8px; text-decoration: none; font-size: 14px; cursor: pointer; border: none; }
    .btn-back { background: #888; }
  </style>
</head>
<body>
<div class="box">
  <div class="msg"><c:out value="${msg}" /></div>
  <c:if test="${not empty redirectUrl}">
    <script>setTimeout(function(){ location.href='<c:out value="${redirectUrl}"/>'; }, 1500);</script>
    <a class="btn" href="<c:out value='${redirectUrl}'/>">이동하기</a>
  </c:if>
  <c:if test="${historyBack and empty redirectUrl}">
    <button class="btn btn-back" onclick="history.back()">뒤로 가기</button>
  </c:if>
  <c:if test="${empty redirectUrl and not historyBack}">
    <a class="btn" href="/usr/child/list">홈으로</a>
  </c:if>
</div>
</body>
</html>
