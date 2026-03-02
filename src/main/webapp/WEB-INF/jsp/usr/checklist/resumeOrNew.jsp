<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>체크리스트 이어하기 - LittleSteps</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: Arial, sans-serif;
      background: #f5f7fa;
      padding: 40px 20px;
      display: flex;
      justify-content: center;
    }
    .card {
      background: #fff;
      border: 1px solid #e0e0e0;
      border-radius: 14px;
      padding: 36px 32px;
      width: 100%;
      max-width: 460px;
      text-align: center;
      box-shadow: 0 2px 12px rgba(0,0,0,0.07);
    }
    .icon { font-size: 40px; margin-bottom: 14px; }
    h2 { font-size: 20px; color: #222; margin-bottom: 8px; }
    .sub { color: #777; font-size: 14px; margin-bottom: 24px; line-height: 1.5; }
    .info-box {
      background: #f0f4ff;
      border-radius: 8px;
      padding: 12px 16px;
      margin-bottom: 28px;
      font-size: 13px;
      color: #555;
      text-align: left;
    }
    .info-box .label { color: #888; font-size: 12px; margin-bottom: 2px; }
    .info-box .value { color: #333; font-weight: bold; }
    .btn {
      display: block;
      width: 100%;
      padding: 14px;
      border-radius: 8px;
      font-size: 15px;
      cursor: pointer;
      border: none;
      text-decoration: none;
      margin-bottom: 10px;
      transition: opacity 0.15s;
    }
    .btn:last-child { margin-bottom: 0; }
    .btn:hover { opacity: 0.85; }
    .btn-resume {
      background: #3a6de8;
      color: #fff;
    }
    .btn-new {
      background: #fff;
      color: #444;
      border: 1px solid #ccc;
    }
    .btn-new:hover { background: #f7f7f7; }
    .divider {
      margin: 16px 0;
      color: #bbb;
      font-size: 13px;
      position: relative;
    }
    .divider::before, .divider::after {
      content: '';
      display: inline-block;
      width: 36%;
      height: 1px;
      background: #e0e0e0;
      vertical-align: middle;
      margin: 0 8px;
    }
  </style>
</head>
<body>
<%@ include file="/WEB-INF/jsp/usr/common/header.jsp" %>
<div class="card">
  <div class="icon">📋</div>
  <h2>임시저장 내용이 있어요</h2>
  <p class="sub">이어서 작성하거나 처음부터 다시 시작할 수 있습니다.</p>

  <c:if test="${not empty draftInfo}">
    <div class="info-box">
      <div class="label">마지막 저장 시각</div>
      <div class="value"><c:out value="${draftInfo.lastSavedAt}" /></div>
      <c:if test="${not empty draftInfo.childName}">
        <div class="label" style="margin-top:8px;">대상 아이</div>
        <div class="value"><c:out value="${draftInfo.childName}" /></div>
      </c:if>
    </div>
  </c:if>

  <%-- 불러오기: 기존 DRAFT runId로 start 진입 → answersMap 복원 --%>
  <a href="/usr/checklist/start?runId=${draftRunId}"
     class="btn btn-resume">임시저장 내용 불러오기</a>

  <div class="divider">또는</div>

  <%-- 새로 시작: 기존 DRAFT 폐기(answers 포함) 후 신규 생성 --%>
  <form action="/usr/checklist/doDiscardAndNew" method="post">
    <input type="hidden" name="draftRunId"   value="${draftRunId}" />
    <input type="hidden" name="checklistId"  value="${checklistId}" />
    <input type="hidden" name="childId"      value="${childId}" />
    <button class="btn btn-new" type="submit">새로 시작하기</button>
  </form>
</div>

</body>
</html>
