<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>내 즐겨찾기</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
           margin: 0; background: #f0f2f5; color: #222; }
    .page-wrap { max-width: 900px; margin: 0 auto; padding: 16px; }

    .page-header { display: flex; justify-content: space-between; align-items: center;
                   margin-bottom: 14px; flex-wrap: wrap; gap: 8px; }
    .page-header h1 { margin: 0; font-size: 20px; }
    .header-links a { color: #4a90d9; text-decoration: none; font-size: 13px; margin-left: 10px; }

    .count-bar { font-size: 13px; color: #888; margin-bottom: 10px; padding: 0 4px; }

    .center-grid { display: grid;
                   grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
                   gap: 14px; margin-bottom: 20px; }
    .center-card { background: #fff; border-radius: 12px;
                   box-shadow: 0 1px 4px rgba(0,0,0,.07);
                   padding: 16px; transition: opacity .3s; }
    .card-top { display: flex; justify-content: space-between; align-items: flex-start;
                margin-bottom: 6px; }
    .center-name { font-size: 15px; font-weight: 700; color: #222; flex: 1; padding-right: 8px; }
    .fav-btn { background: none; border: none; font-size: 22px; cursor: pointer;
               color: #f39c12; padding: 0; line-height: 1; transition: color .2s; flex-shrink: 0; }
    .fav-btn:hover { color: #e67e22; }
    .center-location { font-size: 13px; color: #777; margin-bottom: 4px; }
    .center-address  { font-size: 12px; color: #999; margin-bottom: 6px;
                       white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .center-phone    { font-size: 12px; color: #888; margin-bottom: 8px; }
    .tags-row { display: flex; flex-wrap: wrap; gap: 4px; }
    .tag { display: inline-block; padding: 3px 9px; border-radius: 999px; font-size: 11px; }
    .tag-therapy { background: #fff0f8; color: #8e2474; border: 1px solid #f0c0e0; }

    /* 빈 상태 */
    .empty-state { text-align: center; padding: 56px 20px; color: #aaa;
                   background: #fff; border-radius: 12px;
                   box-shadow: 0 1px 4px rgba(0,0,0,.07); }
    .empty-state .icon { font-size: 48px; display: block; margin-bottom: 12px; }
    .empty-state p { margin: 6px 0; font-size: 15px; }
    .empty-state .sub { font-size: 13px; }
    .btn-explore { display: inline-block; margin-top: 18px; padding: 11px 24px;
                   background: #4a90d9; color: #fff; border-radius: 8px;
                   text-decoration: none; font-size: 14px; }
    .btn-explore:hover { background: #3a7bc8; }

    @media (max-width: 480px) {
      .center-grid { grid-template-columns: 1fr; }
    }
  </style>
</head>
<body>
<div class="page-wrap">

  <div class="page-header">
    <h1>★ 내 즐겨찾기</h1>
    <div class="header-links">
      <a href="/usr/center/list">기관 전체보기</a>
      <a href="/usr/child/list">아이 목록</a>
    </div>
  </div>

  <c:choose>
    <c:when test="${empty centers}">
      <div class="empty-state">
        <span class="icon">☆</span>
        <p>아직 즐겨찾기한 기관이 없습니다.</p>
        <p class="sub">마음에 드는 기관의 ☆ 버튼을 눌러 저장해 보세요.</p>
        <a href="/usr/center/list" class="btn-explore">센터 둘러보기 →</a>
      </div>
    </c:when>
    <c:otherwise>
      <div class="count-bar">즐겨찾기 <strong>${fn:length(centers)}</strong>개</div>
      <div class="center-grid" id="fav-grid">
        <c:forEach items="${centers}" var="ctr">
          <div class="center-card" id="card-${ctr.id}">
            <div class="card-top">
              <span class="center-name"><c:out value="${ctr.name}" /></span>
              <button class="fav-btn favorited"
                      onclick="removeFav(this, ${ctr.id})"
                      title="즐겨찾기 해제"
                      aria-label="즐겨찾기 해제">★</button>
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

</div>

<script>
/**
 * 즐겨찾기 페이지 전용 토글: 해제 시 카드를 부드럽게 제거.
 */
function removeFav(btn, centerId) {
  fetch('/usr/center/doToggleFavorite', {
    method: 'POST',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    body: 'centerId=' + centerId
  })
  .then(r => r.json())
  .then(data => {
    if (!data.favorited) {
      const card = document.getElementById('card-' + centerId);
      if (card) {
        card.style.opacity = '0';
        setTimeout(() => {
          card.remove();
          // 카드가 모두 없어지면 빈 상태 안내
          const grid = document.getElementById('fav-grid');
          if (grid && grid.children.length === 0) {
            location.reload();
          }
        }, 300);
      }
    }
  })
  .catch(() => alert('즐겨찾기 해제 중 오류가 발생했습니다.'));
}
</script>
</body>
</html>
