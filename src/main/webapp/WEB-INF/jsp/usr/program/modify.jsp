<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"    uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8"/>
  <title>프로그램 수정 - CareRoute</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body   { font-family: Arial, sans-serif; margin: 0; background: #f5f5f5; }
    .wrap  { max-width: 720px; margin: 40px auto; background: #fff;
             border: 1px solid #ddd; border-radius: 10px; padding: 36px 40px; }
    h2     { margin: 0 0 24px; font-size: 20px; }
    .field { margin-bottom: 16px; }
    label  { display: block; font-size: 13px; font-weight: bold; margin-bottom: 5px; }
    input[type="text"], input[type="date"], input[type="number"],
    input[type="url"], textarea, select {
      width: 100%; padding: 9px 12px; border: 1px solid #ccc;
      border-radius: 6px; font-size: 14px; }
    textarea { height: 200px; resize: vertical; }
    .row2  { display: flex; gap: 12px; }
    .row2 .field { flex: 1; }
    .err   { display: block; margin-top: 4px; font-size: 12px; color: #e74c3c; }
    .errmsg { padding: 10px 14px; background: #fdecea; border: 1px solid #f5c6cb;
              border-radius: 6px; font-size: 13px; color: #842029; margin-bottom: 16px; }
    .hint  { font-size: 11px; color: #999; margin-top: 3px; }
    .btns  { display: flex; gap: 10px; margin-top: 20px; }
    .btn   { flex: 1; padding: 11px; border: none; border-radius: 6px;
             font-size: 15px; cursor: pointer; }
    .btn-submit { background: #4a90d9; color: #fff; }
    .btn-submit:hover { background: #3a7bc8; }
    .btn-cancel { background: #eee; color: #555; text-decoration: none;
                  display: flex; align-items: center; justify-content: center; }
    .section-title { font-size: 13px; font-weight: bold; color: #555;
                     border-bottom: 2px solid #4a90d9; padding-bottom: 6px;
                     margin: 24px 0 16px; }
    .files-list { list-style: none; padding: 0; margin: 0 0 10px; }
    .files-list li { font-size: 13px; padding: 5px 0; border-bottom: 1px solid #f0f0f0;
                     color: #555; }
    .files-list li:last-child { border-bottom: none; }
    .nav   { font-size: 13px; margin-bottom: 14px; }
    .nav a { color: #4a90d9; text-decoration: none; }
  </style>
</head>
<body>
<div class="wrap">
  <p class="nav">
    <a href="/usr/program/list">프로그램 게시판</a> &gt;
    <a href="/usr/program/detail?id=${post.id}">상세</a> &gt; 수정
  </p>
  <h2>프로그램 수정</h2>

  <c:if test="${not empty errorMsg}">
    <div class="errmsg"><c:out value="${errorMsg}"/></div>
  </c:if>

  <form:form method="post" action="/usr/program/doModify"
             modelAttribute="postForm" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${post.id}"/>

    <form:errors path="" cssClass="errmsg" element="div" />

    <div class="field">
      <label>제목 <span style="color:#e74c3c;">*</span></label>
      <form:input path="title" />
      <form:errors path="title" cssClass="err" element="span" />
    </div>

    <div class="field">
      <label>내용 <span style="color:#e74c3c;">*</span></label>
      <form:textarea path="body" />
      <form:errors path="body" cssClass="err" element="span" />
    </div>

    <p class="section-title">프로그램 상세 정보 (선택)</p>

    <div class="field">
      <label>카테고리</label>
      <form:select path="category">
        <form:option value="">-- 선택 --</form:option>
        <form:option value="CAMP">방학캠프</form:option>
        <form:option value="SPECIAL">특강</form:option>
      </form:select>
    </div>

    <div class="row2">
      <div class="field">
        <label>시작일</label>
        <form:input path="startDate" type="date" />
      </div>
      <div class="field">
        <label>종료일</label>
        <form:input path="endDate" type="date" />
      </div>
    </div>

    <div class="field">
      <label>장소</label>
      <form:input path="location" />
    </div>

    <div class="row2">
      <div class="field">
        <label>참가비 (원)</label>
        <form:input path="fee" type="number" />
      </div>
      <div class="field">
        <label>최대 인원</label>
        <form:input path="maxPeople" type="number" />
      </div>
    </div>

    <div class="field">
      <label>신청 URL</label>
      <form:input path="applyUrl" type="text" />
      <form:errors path="applyUrl" cssClass="err" element="span" />
    </div>

    <p class="section-title">첨부 파일</p>

    <%-- 기존 파일 목록 --%>
    <c:if test="${not empty post.files}">
      <div style="margin-bottom:10px;">
        <span style="font-size:13px;color:#555;">현재 첨부: ${post.files.size()}개</span>
        <ul class="files-list">
          <c:forEach var="f" items="${post.files}">
            <li>📎 <c:out value="${f.origName}"/>
              <span style="color:#bbb;font-size:11px;">(${f.fileSize / 1024}KB)</span>
            </li>
          </c:forEach>
        </ul>
      </div>
    </c:if>

    <div class="field">
      <input type="file" name="files" multiple accept=".jpg,.jpeg,.png,.gif,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.hwp,.zip" />
      <span class="hint">새 파일 추가 (기존 파일 유지 · 총 5개 이하 · 파일당 10MB)</span>
    </div>

    <div class="btns">
      <a href="/usr/program/detail?id=${post.id}" class="btn btn-cancel">취소</a>
      <button type="submit" class="btn btn-submit">저장</button>
    </div>

  </form:form>
</div>
</body>
</html>
