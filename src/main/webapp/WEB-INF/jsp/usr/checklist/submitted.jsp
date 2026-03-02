<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("loginedUserId", session.getAttribute("loginedUserId")); %>

<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>제출 완료 - LittleSteps</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body { font-family: Arial, sans-serif; margin: 0; background: #f5f7fa; }
    .wrap { max-width: 540px; margin: 60px auto; padding: 0 16px; text-align: center; }
    .icon { font-size: 48px; margin-bottom: 16px; }
    h2 { font-size: 22px; margin: 0 0 10px; }
    .sub { color: #666; font-size: 14px; margin-bottom: 28px; }
    .btn-row { display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; }
    .btn { display: inline-block; padding: 11px 22px; border-radius: 8px;
           font-size: 14px; text-decoration: none; border: none; cursor: pointer; }
    .btn-primary { background: #3a6de8; color: #fff; }
    .btn-primary:hover { background: #2c59c9; }
    .btn-secondary { background: #fff; color: #444; border: 1px solid #ccc; }
    .btn-secondary:hover { background: #f5f5f5; }
  </style>
</head>
<body>
<%@ include file="/WEB-INF/jsp/usr/common/header.jsp" %>
<div class="wrap">
  <div class="icon">✅</div>
  <h2>체크리스트 제출 완료</h2>
  <p class="sub">결과를 분석하여 지원 영역과 추천 기관을 안내해 드립니다.</p>
  <div class="btn-row">
    <a href="/usr/checklist/result?runId=${runId}" class="btn btn-primary">결과 보기</a>
    <a href="/usr/checklist/start" class="btn btn-secondary">새 체크리스트</a>
  </div>
</div>
</body>
</html>
