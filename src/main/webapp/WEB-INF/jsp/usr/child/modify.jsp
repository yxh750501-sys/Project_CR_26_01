<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>아이 프로필 수정 - LittleSteps</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body   { font-family: Arial, sans-serif; margin: 0; background: #f5f5f5; }
    .wrap  { max-width: 560px; margin: 20px auto; padding: 0 16px 48px; }
    .card  { background: #fff; border: 1px solid #ddd; border-radius: 12px; padding: 32px 36px; }
    .nav   { font-size: 13px; margin-bottom: 14px; }
    .nav a { color: #4a90d9; text-decoration: none; }
    h2     { margin: 0 0 24px; font-size: 20px; }
    .field { margin-bottom: 16px; }
    label  { display: block; font-size: 13px; font-weight: bold; margin-bottom: 5px; }
    input[type="text"], input[type="date"], select, textarea {
      width: 100%; padding: 9px 12px; border: 1px solid #ccc;
      border-radius: 6px; font-size: 14px; font-family: inherit; }
    textarea { height: 100px; resize: vertical; }
    select { background: #fafafa; }
    .btns  { display: flex; gap: 10px; margin-top: 24px; }
    .btn   { flex: 1; padding: 11px; border: none; border-radius: 8px;
             font-size: 15px; cursor: pointer; text-decoration: none; font-family: inherit;
             display: flex; align-items: center; justify-content: center; }
    .btn-submit { background: #4a90d9; color: #fff; }
    .btn-submit:hover { background: #3a7bc8; }
    .btn-cancel { background: #eee; color: #555; }
    .btn-cancel:hover { background: #e0e0e0; }
    .alert-error { background: #fdecea; border: 1px solid #f5c6cb; color: #842029;
                   border-radius: 8px; padding: 10px 14px; margin-bottom: 16px; font-size: 14px; }
  </style>
</head>
<body>
<%@ include file="/WEB-INF/jsp/usr/common/header.jsp" %>

<div class="wrap">
  <p class="nav"><a href="/usr/child/list">아이 프로필 관리</a> &gt; 수정</p>

  <div class="card">
    <h2>아이 프로필 수정</h2>

    <c:if test="${not empty msg}">
      <div class="alert-error"><c:out value="${msg}" /></div>
    </c:if>

    <form method="post" action="/usr/child/doModify">
      <input type="hidden" name="id" value="${child.id}" />

      <div class="field">
        <label>이름 <span style="color:#e74c3c;">*</span></label>
        <input type="text" name="name" value="<c:out value='${child.name}' />" required />
      </div>
      <div class="field">
        <label>생년월일</label>
        <input type="date" name="birthDate" value="<c:out value='${child.birthDate}' />" />
      </div>
      <div class="field">
        <label>성별</label>
        <select name="gender">
          <option value="U" ${child.gender eq 'U' ? 'selected' : ''}>미정</option>
          <option value="M" ${child.gender eq 'M' ? 'selected' : ''}>남</option>
          <option value="F" ${child.gender eq 'F' ? 'selected' : ''}>여</option>
        </select>
      </div>
      <div class="field">
        <label>메모</label>
        <textarea name="note"><c:out value="${child.note}" /></textarea>
      </div>
      <div class="btns">
        <a href="/usr/child/list" class="btn btn-cancel">취소</a>
        <button type="submit" class="btn btn-submit">저장</button>
      </div>
    </form>
  </div>
</div>
</body>
</html>
