<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>지원 기관 목록 - LittleSteps</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
           margin: 0; background: #f0f2f5; color: #222; }
    .page-wrap { max-width: 900px; margin: 0 auto; padding: 16px; }

    /* ── 헤더 ── */
    .page-header { display: flex; justify-content: space-between; align-items: center;
                   margin-bottom: 14px; flex-wrap: wrap; gap: 8px; }
    .page-header h1 { margin: 0; font-size: 20px; }
    .header-links a { color: #4a90d9; text-decoration: none; font-size: 13px; margin-left: 10px; }

    /* ── 필터 바 ── */
    .filter-card { background: #fff; border-radius: 12px;
                   box-shadow: 0 1px 4px rgba(0,0,0,.08);
                   padding: 16px 20px; margin-bottom: 14px; }
    .filter-form { display: flex; gap: 10px; flex-wrap: wrap; align-items: flex-end; }
    .filter-group { display: flex; flex-direction: column; flex: 1; min-width: 130px; }
    .filter-group label { font-size: 11px; color: #888; margin-bottom: 4px; }
    .filter-group input,
    .filter-group select { padding: 8px 10px; border: 1px solid #ddd; border-radius: 8px;
                           font-size: 14px; background: #fafafa; }
    .btn-search { padding: 8px 20px; background: #4a90d9; color: #fff;
                  border: none; border-radius: 8px; font-size: 14px; cursor: pointer; white-space: nowrap; }
    .btn-search:hover { background: #3a7bc8; }
    .btn-reset { padding: 8px 14px; background: #fff; color: #666;
                 border: 1px solid #ddd; border-radius: 8px; font-size: 14px;
                 cursor: pointer; text-decoration: none; white-space: nowrap; }

    /* ── 결과 수 ── */
    .count-bar { font-size: 13px; color: #888; margin-bottom: 10px; padding: 0 4px; }

    /* ── 카드 그리드 ── */
    .center-grid { display: grid;
                   grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
                   gap: 14px; margin-bottom: 20px; }
    .center-card { background: #fff; border-radius: 12px;
                   box-shadow: 0 1px 4px rgba(0,0,0,.07); padding: 16px; position: relative; }
    .card-top { display: flex; justify-content: space-between; align-items: flex-start;
                margin-bottom: 6px; }
    .center-name { font-size: 15px; font-weight: 700; color: #222; flex: 1; padding-right: 8px; }
    .fav-btn { background: none; border: none; font-size: 22px; cursor: pointer;
               color: #ccc; padding: 0; line-height: 1; transition: color .2s; flex-shrink: 0; }
    .fav-btn.favorited { color: #f39c12; }
    .fav-btn:hover     { color: #f39c12; }
    .center-location { font-size: 13px; color: #777; margin-bottom: 4px; }
    .center-address  { font-size: 12px; color: #999; margin-bottom: 6px;
                       white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .center-phone    { font-size: 12px; color: #888; margin-bottom: 8px; }
    .tags-row { display: flex; flex-wrap: wrap; gap: 4px; }
    .tag { display: inline-block; padding: 3px 9px; border-radius: 999px; font-size: 11px; }
    .tag-therapy { background: #fff0f8; color: #8e2474; border: 1px solid #f0c0e0; }

    /* ── 빈 상태 ── */
    .empty-state { text-align: center; padding: 48px 20px; color: #aaa;
                   background: #fff; border-radius: 12px; }
    .empty-state .icon { font-size: 40px; display: block; margin-bottom: 10px; }
    .empty-state p { margin: 6px 0; font-size: 14px; }

    /* ── 페이지네이션 ── */
    .pagination { display: flex; justify-content: center; gap: 6px;
                  flex-wrap: wrap; margin-top: 8px; }
    .pagination a, .pagination span {
      display: inline-block; padding: 7px 13px; border-radius: 8px;
      font-size: 14px; text-decoration: none; border: 1px solid #ddd;
      color: #444; background: #fff; }
    .pagination a:hover { background: #f0f4ff; border-color: #4a90d9; color: #4a90d9; }
    .pagination .active { background: #4a90d9; color: #fff; border-color: #4a90d9; font-weight: 700; }
    .pagination .disabled { color: #ccc; cursor: default; }

    @media (max-width: 480px) {
      .center-grid { grid-template-columns: 1fr; }
    }
  </style>
</head>
<body>
<div class="page-wrap">

  <!-- 헤더 -->
  <div class="page-header">
    <h1>지원 기관 찾기</h1>
    <div class="header-links">
      <a href="/usr/center/favorites">★ 내 즐겨찾기</a>
      <a href="/usr/child/list">아이 목록</a>
    </div>
  </div>

  <!-- 필터 -->
  <div class="filter-card">
    <form class="filter-form" method="get" action="/usr/center/list">
      <div class="filter-group">
        <label>센터명 검색</label>
        <input type="text" name="keyword" placeholder="센터명 또는 주소"
               value="<c:out value='${keyword}' />" />
      </div>
      <div class="filter-group">
        <label>지역</label>
        <select name="sido">
          <option value="">지역 전체</option>
          <c:forEach var="s" items="${['서울','경기','인천','부산','대구','광주','대전','울산','세종','강원','충북','충남','전북','전남','경북','경남','제주']}">
            <option value="${s}" ${sido eq s ? 'selected' : ''}><c:out value="${s}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="filter-group">
        <label>지원 영역</label>
        <select name="domain">
          <option value="">영역 전체</option>
          <c:forEach var="entry" items="${domainLabelMap}">
            <option value="${entry.key}" ${domain eq entry.key ? 'selected' : ''}>${entry.value}</option>
          </c:forEach>
        </select>
      </div>
      <button type="submit" class="btn-search">검색</button>
      <a href="/usr/center/list" class="btn-reset">초기화</a>
    </form>
  </div>

  <!-- 결과 수 -->
  <div class="count-bar">총 <strong>${total}</strong>개 기관</div>

  <!-- 센터 카드 -->
  <c:choose>
    <c:when test="${empty centers}">
      <div class="empty-state">
        <span class="icon">🏥</span>
        <p>검색 조건에 맞는 기관이 없습니다.</p>
        <p style="font-size:12px;">필터를 변경하거나 <a href="/usr/center/list" style="color:#4a90d9;">전체 목록</a>을 확인해 보세요.</p>
      </div>
    </c:when>
    <c:otherwise>
      <div class="center-grid">
        <c:forEach items="${centers}" var="ctr">
          <div class="center-card">
            <div class="card-top">
              <span class="center-name"><c:out value="${ctr.name}" /></span>
              <c:set var="isFav" value="${not empty favoriteIds and favoriteIds.contains(ctr.id)}" />
              <button class="fav-btn ${isFav ? 'favorited' : ''}"
                      onclick="toggleFav(this, ${ctr.id})"
                      title="${isFav ? '즐겨찾기 해제' : '즐겨찾기 추가'}"
                      aria-label="${isFav ? '즐겨찾기 해제' : '즐겨찾기 추가'}">
                ${isFav ? '★' : '☆'}
              </button>
            </div>
            <div class="center-location">
              <c:out value="${ctr.sido}" />
              <c:if test="${not empty ctr.sigungu}"> <c:out value="${ctr.sigungu}" /></c:if>
            </div>
            <c:if test="${not empty ctr.address}">
              <div class="center-address" title="${ctr.address}"><c:out value="${ctr.address}" /></div>
            </c:if>
            <c:if test="${not empty ctr.phone}">
              <div class="center-phone">☎ <c:out value="${ctr.phone}" /></div>
            </c:if>
            <c:if test="${not empty ctr.therapyTypeCodes}">
              <div class="tags-row">
                <c:forEach items="${fn:split(ctr.therapyTypeCodes, ',')}" var="tc">
                  <c:set var="tc" value="${fn:trim(tc)}" />
                  <span class="tag tag-therapy">
                    <c:choose>
                      <c:when test="${not empty therapyTypeLabelMap[tc]}">${therapyTypeLabelMap[tc]}</c:when>
                      <c:otherwise><c:out value="${tc}" /></c:otherwise>
                    </c:choose>
                  </span>
                </c:forEach>
              </div>
            </c:if>
          </div>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>

  <!-- 페이지네이션 -->
  <c:if test="${totalPages gt 1}">
    <c:set var="urlBase" value="?keyword=${keyword}&amp;sido=${sido}&amp;domain=${domain}&amp;page=" />
    <div class="pagination">
      <c:choose>
        <c:when test="${page gt 1}">
          <a href="${urlBase}${page - 1}">이전</a>
        </c:when>
        <c:otherwise><span class="disabled">이전</span></c:otherwise>
      </c:choose>

      <%-- 최대 10페이지 표시 --%>
      <c:set var="startPg" value="${page > 5 ? page - 4 : 1}" />
      <c:set var="endPg"   value="${startPg + 9 > totalPages ? totalPages : startPg + 9}" />
      <c:if test="${startPg gt 1}">
        <a href="${urlBase}1">1</a>
        <c:if test="${startPg gt 2}"><span class="disabled">…</span></c:if>
      </c:if>
      <c:forEach begin="${startPg}" end="${endPg}" var="p">
        <c:choose>
          <c:when test="${p eq page}"><span class="active">${p}</span></c:when>
          <c:otherwise><a href="${urlBase}${p}">${p}</a></c:otherwise>
        </c:choose>
      </c:forEach>
      <c:if test="${endPg lt totalPages}">
        <c:if test="${endPg lt totalPages - 1}"><span class="disabled">…</span></c:if>
        <a href="${urlBase}${totalPages}">${totalPages}</a>
      </c:if>

      <c:choose>
        <c:when test="${page lt totalPages}">
          <a href="${urlBase}${page + 1}">다음</a>
        </c:when>
        <c:otherwise><span class="disabled">다음</span></c:otherwise>
      </c:choose>
    </div>
  </c:if>

</div>

<script>
function toggleFav(btn, centerId) {
  fetch('/usr/center/doToggleFavorite', {
    method: 'POST',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    body: 'centerId=' + centerId
  })
  .then(r => r.json())
  .then(data => {
    if (data.favorited) {
      btn.classList.add('favorited');
      btn.textContent = '★';
      btn.title = '즐겨찾기 해제';
    } else {
      btn.classList.remove('favorited');
      btn.textContent = '☆';
      btn.title = '즐겨찾기 추가';
    }
  })
  .catch(() => alert('즐겨찾기 처리 중 오류가 발생했습니다.'));
}
</script>
</body>
</html>
