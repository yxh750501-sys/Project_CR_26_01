<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  공통 네비게이션 헤더.
  사용법: <%@ include file="/WEB-INF/jsp/usr/common/header.jsp" %>
  필요 모델 속성: loginedUserId (세션 유무로 메뉴 분기)
--%>
<style>
  .site-nav {
    background: #fff;
    border-bottom: 1px solid #e5e7eb;
    position: sticky;
    top: 0;
    z-index: 200;
    box-shadow: 0 1px 3px rgba(0,0,0,.06);
  }
  .site-nav__inner {
    max-width: 1100px;
    margin: 0 auto;
    padding: 0 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 52px;
  }
  .site-nav__logo {
    font-size: 17px;
    font-weight: 800;
    color: #3a6de8;
    text-decoration: none;
    letter-spacing: -0.5px;
  }
  .site-nav__links {
    display: flex;
    align-items: center;
    gap: 2px;
    list-style: none;
    margin: 0;
    padding: 0;
  }
  .site-nav__links a,
  .site-nav__logout-btn {
    display: inline-block;
    padding: 6px 12px;
    border-radius: 6px;
    font-size: 14px;
    color: #444;
    text-decoration: none;
    background: none;
    border: none;
    cursor: pointer;
    font-family: inherit;
    transition: background .12s, color .12s;
  }
  .site-nav__links a:hover,
  .site-nav__logout-btn:hover { background: #f3f4f6; color: #3a6de8; }
  .site-nav__links .nav-btn-join {
    background: #3a6de8;
    color: #fff;
    font-weight: 600;
    padding: 6px 14px;
    margin-left: 4px;
  }
  .site-nav__links .nav-btn-join:hover { background: #2c59c9; color: #fff; }
  .site-nav__logout-btn { color: #aaa; font-size: 13px; padding: 6px 10px; }
  @media (max-width: 520px) {
    .site-nav__links a { padding: 5px 8px; font-size: 13px; }
    .site-nav__logo { font-size: 15px; }
  }
</style>

<nav class="site-nav">
  <div class="site-nav__inner">
    <a href="/" class="site-nav__logo">CareRoute</a>
    <ul class="site-nav__links">
      <li><a href="/">홈</a></li>
      <li><a href="/usr/program/list">프로그램</a></li>
      <li><a href="/usr/post/list">자유게시판</a></li>
      <li><a href="/usr/center/list">센터 찾기</a></li>
      <c:choose>
        <c:when test="${not empty loginedUserId}">
          <li><a href="/usr/my">마이페이지</a></li>
          <li>
            <form action="/usr/member/doLogout" method="post" style="display:inline;margin:0;">
              <button type="submit" class="site-nav__logout-btn">로그아웃</button>
            </form>
          </li>
        </c:when>
        <c:otherwise>
          <li><a href="/usr/member/login">로그인</a></li>
          <li><a href="/usr/member/join" class="nav-btn-join">회원가입</a></li>
        </c:otherwise>
      </c:choose>
    </ul>
  </div>
</nav>
