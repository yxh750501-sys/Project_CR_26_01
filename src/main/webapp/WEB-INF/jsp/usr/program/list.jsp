<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8"/>
  <title>프로그램 게시판 - CareRoute</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body   { font-family: Arial, sans-serif; margin: 0; background: #f5f5f5; color: #333; }
    .wrap  { max-width: 900px; margin: 40px auto; padding: 0 16px; }
    .hd    { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h2     { margin: 0; font-size: 22px; }
    .btn   { padding: 8px 18px; background: #4a90d9; color: #fff; border: none;
             border-radius: 6px; font-size: 14px; cursor: pointer; text-decoration: none; }
    .btn:hover { background: #3a7bc8; }
    table  { width: 100%; border-collapse: collapse; background: #fff;
             border-radius: 8px; overflow: hidden;
             box-shadow: 0 1px 4px rgba(0,0,0,.1); }
    th     { background: #4a90d9; color: #fff; padding: 11px 14px;
             font-size: 13px; text-align: left; }
    td     { padding: 11px 14px; font-size: 13px; border-bottom: 1px solid #eee; }
    tr:last-child td { border-bottom: none; }
    tr:hover td { background: #f8fbff; }
    .badge { display: inline-block; padding: 2px 8px; border-radius: 12px;
             font-size: 11px; font-weight: bold; }
    .camp    { background: #fde8c8; color: #b05a00; }
    .special { background: #d4eaff; color: #1a5fa8; }
    a.ttl  { color: #1a6abf; text-decoration: none; font-weight: 500; }
    a.ttl:hover { text-decoration: underline; }
    .empty { text-align: center; padding: 40px 0; color: #999; font-size: 15px; }
    .pager { display: flex; justify-content: center; gap: 6px; margin-top: 20px; }
    .pager a, .pager span { padding: 6px 12px; border: 1px solid #ddd; border-radius: 4px;
                             font-size: 13px; text-decoration: none; color: #555; background: #fff; }
    .pager .cur { background: #4a90d9; color: #fff; border-color: #4a90d9; }
    .nav   { margin-bottom: 14px; font-size: 13px; }
    .nav a { color: #4a90d9; text-decoration: none; }
  </style>
</head>
<body>
<div class="wrap">
  <p class="nav"><a href="/">홈</a> &gt; 프로그램 게시판</p>

  <div class="hd">
    <h2>프로그램 게시판 <small style="font-size:14px;color:#888;">(방학캠프·특강)</small></h2>
    <c:if test="${not empty loginedUserId}">
      <a href="/usr/program/write" class="btn">+ 글쓰기</a>
    </c:if>
  </div>

  <table>
    <thead>
      <tr>
        <th style="width:60px;">번호</th>
        <th style="width:70px;">분류</th>
        <th>제목</th>
        <th style="width:90px;">기간</th>
        <th style="width:80px;">작성자</th>
        <th style="width:90px;">등록일</th>
      </tr>
    </thead>
    <tbody>
      <c:choose>
        <c:when test="${empty posts}">
          <tr><td colspan="6" class="empty">등록된 게시글이 없습니다.</td></tr>
        </c:when>
        <c:otherwise>
          <c:forEach var="p" items="${posts}">
            <tr>
              <td>${p.id}</td>
              <td>
                <c:choose>
                  <c:when test="${p.category == 'CAMP'}"><span class="badge camp">캠프</span></c:when>
                  <c:when test="${p.category == 'SPECIAL'}"><span class="badge special">특강</span></c:when>
                  <c:otherwise>—</c:otherwise>
                </c:choose>
              </td>
              <td><a class="ttl" href="/usr/program/detail?id=${p.id}"><c:out value="${p.title}"/></a></td>
              <td style="font-size:11px;color:#666;">
                <c:choose>
                  <c:when test="${not empty p.startDate}">${p.startDate}<c:if test="${not empty p.endDate}">~${p.endDate}</c:if></c:when>
                  <c:otherwise>—</c:otherwise>
                </c:choose>
              </td>
              <td><c:out value="${p.authorName}"/></td>
              <td>${p.createdAt}</td>
            </tr>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </tbody>
  </table>

  <%-- 페이지네이션 --%>
  <c:if test="${totalPages > 1}">
    <div class="pager">
      <c:if test="${page > 1}"><a href="/usr/program/list?page=${page-1}">&laquo;</a></c:if>
      <c:forEach begin="1" end="${totalPages}" var="p">
        <c:choose>
          <c:when test="${p == page}"><span class="cur">${p}</span></c:when>
          <c:otherwise><a href="/usr/program/list?page=${p}">${p}</a></c:otherwise>
        </c:choose>
      </c:forEach>
      <c:if test="${page < totalPages}"><a href="/usr/program/list?page=${page+1}">&raquo;</a></c:if>
    </div>
  </c:if>
</div>
</body>
</html>
