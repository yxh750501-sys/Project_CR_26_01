<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>제출 완료</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 20px; }
    .box { border: 1px solid #ddd; padding: 14px; border-radius: 10px; }
    a { color:#333; }
  </style>
</head>
<body>
  <div class="box">
    <h2>제출 완료</h2>
    <div>runId: <c:out value="${runId}" /></div>
    <div style="margin-top:10px;">
      <a href="/usr/checklist/result?runId=${runId}">결과 화면으로 이동</a>
      &nbsp;|&nbsp;
      <a href="/usr/checklist/result-centers?runId=${runId}">결과+센터추천 화면으로 이동</a>
    </div>
  </div>
</body>
</html>