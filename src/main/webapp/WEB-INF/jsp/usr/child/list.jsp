<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>아이 프로필 관리 - LittleSteps</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
           margin: 0; background: #f0f2f5; color: #222; }
    .page-wrap { max-width: 900px; margin: 0 auto; padding: 20px 16px 48px; }

    /* ── 페이지 제목 ── */
    .page-title { font-size: 20px; font-weight: 700; margin: 0 0 16px; }

    /* ── 알림 ── */
    .alert { border-radius: 10px; padding: 12px 16px; margin-bottom: 12px;
             font-size: 14px; line-height: 1.5; }
    .alert-warn    { background: #fff8e1; border: 1px solid #ffd54f; color: #7a5c00; }
    .alert-success { background: #e8f5e9; border: 1px solid #81c784; color: #2e7d32; }

    /* ── 액션 바 ── */
    .action-bar { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }

    /* ── 버튼 ── */
    .btn { display: inline-flex; align-items: center; justify-content: center;
           padding: 9px 18px; border-radius: 8px; font-size: 14px; font-family: inherit;
           border: none; cursor: pointer; text-decoration: none; white-space: nowrap;
           transition: background .12s, opacity .12s; }
    .btn-primary { background: #4a90d9; color: #fff; }
    .btn-primary:hover { background: #3a7bc8; }
    .btn-outline { background: #fff; color: #555; border: 1px solid #ddd; }
    .btn-outline:hover { background: #f5f5f5; border-color: #bbb; color: #333; }
    .btn-sm { padding: 5px 12px; font-size: 13px; border-radius: 6px; }
    .btn-danger { background: #fff; color: #e74c3c; border: 1px solid #e9b8b4; }
    .btn-danger:hover { background: #fdecea; }

    /* ── 카드 ── */
    .card { background: #fff; border-radius: 12px;
            box-shadow: 0 1px 4px rgba(0,0,0,.08); padding: 20px 24px; }
    .card-title { font-size: 15px; font-weight: 700; margin: 0 0 16px; color: #333; }

    /* ── 테이블 ── */
    .tbl { width: 100%; border-collapse: collapse; font-size: 14px; }
    .tbl th { background: #f8f9fa; font-weight: 600; color: #555;
              padding: 10px 12px; text-align: left;
              border-bottom: 2px solid #e9ecef; white-space: nowrap; }
    .tbl td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0;
              vertical-align: middle; }
    .tbl tbody tr:last-child td { border-bottom: none; }
    .tbl tbody tr:hover { background: #fafbff; }
    .td-actions { display: flex; gap: 6px; align-items: center; }

    /* ── 대표 뱃지 ── */
    .badge-rep { display: inline-block; padding: 3px 10px; border-radius: 999px;
                 background: #e8f5e9; color: #2e7d32; font-size: 12px; font-weight: 600;
                 border: 1px solid #81c784; }

    /* ── 빈 상태 ── */
    .empty-state { text-align: center; padding: 40px 20px; color: #aaa; }
    .empty-state .icon { font-size: 36px; display: block; margin-bottom: 10px; }
    .empty-state p { margin: 0 0 12px; font-size: 14px; }

    /* ── 반응형 ── */
    @media (max-width: 600px) {
      .tbl th:nth-child(4), .tbl td:nth-child(4) { display: none; } /* 메모 숨김 */
      .card { padding: 16px; }
    }
  </style>
</head>
<body>
<%@ include file="/WEB-INF/jsp/usr/common/header.jsp" %>

<div class="page-wrap">

  <h1 class="page-title">아이 프로필 관리</h1>

  <%-- 알림 메시지 --%>
  <c:if test="${not empty msg}">
    <div class="alert alert-success"><c:out value="${msg}" /></div>
  </c:if>

  <c:if test="${not empty needSelect}">
    <div class="alert alert-warn">
      체크리스트·결과 기능을 사용하려면 대표 아이를 먼저 선택해 주세요.
    </div>
  </c:if>

  <c:if test="${not empty selectedChild}">
    <div class="alert alert-success">
      현재 대표 아이: <strong><c:out value="${selectedChild.name}" /></strong>
      <c:if test="${not empty selectedChild.birthDate}">
        <span style="color:#4a7a4a;font-size:13px;">
          &nbsp;(생년월일: <c:out value="${selectedChild.birthDate}" />)
        </span>
      </c:if>
    </div>
  </c:if>

  <%-- 액션 바 --%>
  <div class="action-bar">
    <a href="/usr/child/write" class="btn btn-primary">+ 아이 등록</a>
    <a href="/usr/checklist/start" class="btn btn-outline">체크리스트 시작</a>
    <a href="/usr/my" class="btn btn-outline">마이페이지</a>
  </div>

  <%-- 아이 목록 카드 --%>
  <div class="card">
    <div class="card-title">등록된 아이 목록</div>

    <c:choose>
      <c:when test="${empty children}">
        <div class="empty-state">
          <span class="icon">👶</span>
          <p>아직 등록된 아이가 없습니다.</p>
          <a href="/usr/child/write" class="btn btn-primary btn-sm">+ 아이 등록하기</a>
        </div>
      </c:when>
      <c:otherwise>
        <table class="tbl">
          <thead>
            <tr>
              <th>이름</th>
              <th>생년월일</th>
              <th>성별</th>
              <th>메모</th>
              <th>대표</th>
              <th>관리</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach items="${children}" var="child">
              <tr>
                <td><strong><c:out value="${child.name}" /></strong></td>
                <td>
                  <c:choose>
                    <c:when test="${not empty child.birthDate}"><c:out value="${child.birthDate}" /></c:when>
                    <c:otherwise><span style="color:#bbb;">—</span></c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${child.gender eq 'M'}">남</c:when>
                    <c:when test="${child.gender eq 'F'}">여</c:when>
                    <c:otherwise><span style="color:#bbb;">미정</span></c:otherwise>
                  </c:choose>
                </td>
                <td style="max-width:200px; white-space:nowrap; overflow:hidden;
                           text-overflow:ellipsis; color:#777;">
                  <c:out value="${child.note}" />
                </td>
                <td>
                  <c:choose>
                    <c:when test="${child.id == selectedChildId}">
                      <span class="badge-rep">대표</span>
                    </c:when>
                    <c:otherwise>
                      <form method="post" action="/usr/child/doSelect" style="margin:0;">
                        <input type="hidden" name="id" value="${child.id}" />
                        <button type="submit" class="btn btn-sm btn-outline">대표 선택</button>
                      </form>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <div class="td-actions">
                    <a href="/usr/child/modify?id=${child.id}" class="btn btn-sm btn-outline">수정</a>
                    <form method="post" action="/usr/child/doDelete" style="margin:0;"
                          onsubmit="return confirm('정말 삭제하시겠습니까?');">
                      <input type="hidden" name="id" value="${child.id}" />
                      <button type="submit" class="btn btn-sm btn-danger">삭제</button>
                    </form>
                  </div>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>
  </div>

</div>
</body>
</html>
