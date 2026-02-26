<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"    uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8"/>
  <title>글쓰기 - 자유게시판</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body   { font-family: Arial, sans-serif; margin: 0; background: #f5f5f5; }
    .wrap  { max-width: 720px; margin: 40px auto; background: #fff;
             border: 1px solid #ddd; border-radius: 10px; padding: 36px 40px; }
    h2     { margin: 0 0 24px; font-size: 20px; }
    .field { margin-bottom: 16px; }
    label  { display: block; font-size: 13px; font-weight: bold; margin-bottom: 5px; }
    input[type="text"], textarea {
      width: 100%; padding: 9px 12px; border: 1px solid #ccc;
      border-radius: 6px; font-size: 14px; }
    textarea { height: 250px; resize: vertical; }
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
    .nav   { font-size: 13px; margin-bottom: 14px; }
    .nav a { color: #4a90d9; text-decoration: none; }
  </style>
</head>
<body>
<div class="wrap">
  <p class="nav"><a href="/usr/free/list">자유게시판</a> &gt; 글쓰기</p>
  <h2>글쓰기</h2>

  <c:if test="${not empty errorMsg}">
    <div class="errmsg"><c:out value="${errorMsg}"/></div>
  </c:if>

  <form:form method="post" action="/usr/free/doWrite"
             modelAttribute="postForm" enctype="multipart/form-data">

    <form:errors path="" cssClass="errmsg" element="div" />

    <div class="field">
      <label>제목 <span style="color:#e74c3c;">*</span></label>
      <form:input path="title" placeholder="제목을 입력해 주세요." />
      <form:errors path="title" cssClass="err" element="span" />
    </div>

    <div class="field">
      <label>내용 <span style="color:#e74c3c;">*</span></label>
      <form:textarea path="body" placeholder="내용을 입력해 주세요." />
      <form:errors path="body" cssClass="err" element="span" />
    </div>

    <p class="section-title">첨부 파일</p>
    <div class="field">
      <input type="file" name="files" multiple accept=".jpg,.jpeg,.png,.gif,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.hwp,.zip" />
      <span class="hint">파일당 10MB 이하 · 최대 5개 · 허용: jpg/png/gif/pdf/doc/xls/ppt/hwp/zip</span>
    </div>

    <div class="btns">
      <a href="/usr/free/list" class="btn btn-cancel">취소</a>
      <button type="submit" class="btn btn-submit">등록</button>
    </div>

  </form:form>
</div>
</body>
</html>
