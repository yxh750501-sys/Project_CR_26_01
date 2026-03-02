<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8"/>
  <title><c:out value="${post.title}"/> - LittleSteps</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body   { font-family: Arial, sans-serif; margin: 0; background: #f5f5f5; color: #333; }
    .wrap  { max-width: 860px; margin: 20px auto; padding: 0 16px; }
    .card  { background: #fff; border: 1px solid #ddd; border-radius: 10px; padding: 32px 36px; }
    .nav   { font-size: 13px; margin-bottom: 14px; }
    .nav a { color: #4a90d9; text-decoration: none; }
    .post-title { font-size: 22px; font-weight: bold; margin-bottom: 12px; }
    .meta  { font-size: 13px; color: #888; margin-bottom: 20px;
             display: flex; gap: 16px; flex-wrap: wrap; }
    .badge { display: inline-block; padding: 2px 10px; border-radius: 12px;
             font-size: 12px; font-weight: bold; }
    .camp    { background: #fde8c8; color: #b05a00; }
    .special { background: #d4eaff; color: #1a5fa8; }
    hr     { border: none; border-top: 1px solid #eee; margin: 20px 0; }
    .body  { white-space: pre-wrap; line-height: 1.7; font-size: 15px; min-height: 120px; }
    .info-box { background: #f8fbff; border: 1px solid #d0e5f7; border-radius: 8px;
                padding: 16px 20px; margin: 20px 0; font-size: 14px; }
    .info-box .row { display: flex; gap: 12px; margin-bottom: 8px; }
    .info-box .row:last-child { margin-bottom: 0; }
    .info-box .lbl { font-weight: bold; color: #555; min-width: 70px; }
    .files { margin-top: 20px; }
    .files h4 { font-size: 14px; font-weight: bold; margin-bottom: 10px;
                color: #555; border-bottom: 1px solid #eee; padding-bottom: 6px; }
    .files ul { list-style: none; padding: 0; margin: 0; }
    .files li { padding: 7px 0; font-size: 13px; border-bottom: 1px solid #f0f0f0; }
    .files li:last-child { border-bottom: none; }
    .files a { color: #1a6abf; text-decoration: none; }
    .files a:hover { text-decoration: underline; }
    .actions { display: flex; gap: 10px; margin-top: 24px; }
    .btn  { padding: 9px 20px; border: none; border-radius: 6px; font-size: 14px;
            cursor: pointer; text-decoration: none; }
    .btn-back   { background: #eee; color: #555; }
    .btn-edit   { background: #4a90d9; color: #fff; }
    .btn-edit:hover { background: #3a7bc8; }
    .btn-del    { background: #e74c3c; color: #fff; }
    .btn-del:hover  { background: #c0392b; }
  </style>
</head>
<body>
<%@ include file="/WEB-INF/jsp/usr/common/header.jsp" %>
<div class="wrap">
  <p class="nav"><a href="/usr/program/list">프로그램 게시판</a> &gt; 상세</p>

  <div class="card">
    <div class="post-title">
      <c:if test="${post.category == 'CAMP'}"><span class="badge camp">캠프</span> </c:if>
      <c:if test="${post.category == 'SPECIAL'}"><span class="badge special">특강</span> </c:if>
      <c:out value="${post.title}"/>
    </div>
    <div class="meta">
      <span>작성자: <c:out value="${post.authorName}"/></span>
      <span>등록: ${post.createdAt}</span>
      <c:if test="${post.updatedAt != post.createdAt}">
        <span>수정: ${post.updatedAt}</span>
      </c:if>
    </div>

    <%-- PROGRAM 전용 상세 정보 --%>
    <c:if test="${not empty post.startDate or not empty post.location or post.fee != null}">
      <div class="info-box">
        <c:if test="${not empty post.startDate}">
          <div class="row">
            <span class="lbl">기간</span>
            <span>${post.startDate}<c:if test="${not empty post.endDate}"> ~ ${post.endDate}</c:if></span>
          </div>
        </c:if>
        <c:if test="${not empty post.location}">
          <div class="row">
            <span class="lbl">장소</span>
            <span><c:out value="${post.location}"/></span>
          </div>
        </c:if>
        <c:if test="${post.fee != null}">
          <div class="row">
            <span class="lbl">참가비</span>
            <span><c:choose><c:when test="${post.fee == 0}">무료</c:when><c:otherwise>${post.fee}원</c:otherwise></c:choose></span>
          </div>
        </c:if>
        <c:if test="${post.maxPeople != null}">
          <div class="row">
            <span class="lbl">최대 인원</span>
            <span>${post.maxPeople}명</span>
          </div>
        </c:if>
        <c:if test="${not empty post.applyUrl}">
          <div class="row">
            <span class="lbl">신청</span>
            <span><a href="${post.applyUrl}" target="_blank" rel="noopener">신청 페이지 바로가기 &rarr;</a></span>
          </div>
        </c:if>
      </div>
    </c:if>

    <hr/>
    <div class="body"><c:out value="${post.body}"/></div>

    <%-- 첨부파일 --%>
    <c:if test="${not empty post.files}">
      <div class="files">
        <h4>첨부 파일 (${post.files.size()})</h4>
        <ul>
          <c:forEach var="f" items="${post.files}">
            <li>
              <a href="/usr/file/download?id=${f.id}">
                📎 <c:out value="${f.origName}"/>
              </a>
              <span style="color:#bbb;font-size:11px;"> (${f.fileSize / 1024}KB)</span>
            </li>
          </c:forEach>
        </ul>
      </div>
    </c:if>

    <div class="actions">
      <a href="/usr/program/list" class="btn btn-back">목록</a>
      <c:if test="${loginedUserId == post.memberId}">
        <a href="/usr/program/modify?id=${post.id}" class="btn btn-edit">수정</a>
        <form method="post" action="/usr/program/doDelete" style="margin:0;"
              onsubmit="return confirm('정말 삭제하시겠습니까?');">
          <input type="hidden" name="id" value="${post.id}"/>
          <button type="submit" class="btn btn-del">삭제</button>
        </form>
      </c:if>
    </div>
  </div>
</div>
</body>
</html>
