<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>체크리스트 결과</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 20px; }
    .box { border: 1px solid #ddd; padding: 14px; border-radius: 10px; margin-bottom: 14px; }
    h2 { margin: 0 0 10px 0; }
    table { width: 100%; border-collapse: collapse; }
    th, td { border-bottom: 1px solid #eee; padding: 10px; text-align: left; vertical-align: top; }
    th { background: #fafafa; }
    .tag { display: inline-block; padding: 4px 10px; border: 1px solid #ddd; border-radius: 999px; margin: 0 6px 6px 0; font-size: 12px; }
    .muted { color: #777; }
    .center-name { font-weight: bold; }
    .small { font-size: 12px; }
    .empty { padding: 14px; color: #777; }
  </style>
</head>
<body>

  <div class="box">
    <h2>체크리스트 결과</h2>
    <div class="muted small">
      RunId: <c:out value="${runInfo.runId}" /> /
      아이: <c:out value="${runInfo.childName}" /> /
      체크리스트: <c:out value="${runInfo.checklistTitle}" /> /
      생성일: <c:out value="${runInfo.createdAt}" />
    </div>
  </div>

  <div class="box">
    <h2>기능영역 점수 요약</h2>

    <c:if test="${empty domainStats}">
      <div class="empty">도메인 점수 데이터가 없습니다.</div>
    </c:if>

    <c:if test="${not empty domainStats}">
      <table>
        <thead>
          <tr>
            <th style="width: 35%;">기능영역(domain)</th>
            <th style="width: 15%;">문항수</th>
            <th style="width: 15%;">합계</th>
            <th style="width: 15%;">평균</th>
            <th>메모</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach items="${domainStats}" var="d">
            <tr>
              <td><c:out value="${d.domainCode}" /></td>
              <td><c:out value="${d.questionCount}" /></td>
              <td><c:out value="${d.totalScore}" /></td>
              <td><c:out value="${d.avgScore}" /></td>
              <td class="muted small">
                평균이 높을수록(현 스케일 기준) 우선 추천 대상으로 간주
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:if>
  </div>

  <div class="box">
    <h2>이번 런 기준 추천 기능영역</h2>
    <c:if test="${empty recommendedDomains}">
      <div class="empty">추천 기능영역이 없습니다.</div>
    </c:if>

    <c:if test="${not empty recommendedDomains}">
      <c:forEach items="${recommendedDomains}" var="dc">
        <span class="tag"><c:out value="${dc}" /></span>
      </c:forEach>
    </c:if>
  </div>

  <div class="box">
    <h2>센터 추천(전국 단위)</h2>

    <c:if test="${empty centers}">
      <div class="empty">
        domain_therapy_map 또는 center_services 데이터가 없어서 추천 결과가 비어있습니다.
      </div>
    </c:if>

    <c:if test="${not empty centers}">
      <table>
        <thead>
          <tr>
            <th style="width: 28%;">센터</th>
            <th style="width: 22%;">지역</th>
            <th>주소/연락처</th>
            <th style="width: 22%;">치료타입/서비스</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach items="${centers}" var="cCenter">
            <tr>
              <td>
                <div class="center-name"><c:out value="${cCenter.name}" /></div>
                <div class="muted small">ID: <c:out value="${cCenter.id}" /></div>
              </td>
              <td><c:out value="${cCenter.region}" /></td>
              <td class="small">
                <div><c:out value="${cCenter.address}" /></div>
                <div class="muted">☎ <c:out value="${cCenter.phone}" /></div>
                <c:if test="${not empty cCenter.homepage}">
                  <div class="muted">홈페이지: <c:out value="${cCenter.homepage}" /></div>
                </c:if>
              </td>
              <td class="small">
                <c:if test="${not empty cCenter.therapyTypes}">
                  <div class="muted">치료타입</div>
                  <c:forEach items="${cCenter.therapyTypes}" var="tt">
                    <span class="tag"><c:out value="${tt.name}" /></span>
                  </c:forEach>
                </c:if>

                <c:if test="${not empty cCenter.services}">
                  <div class="muted" style="margin-top:10px;">서비스</div>
                  <ul style="margin: 6px 0 0 16px; padding: 0;">
                    <c:forEach items="${cCenter.services}" var="sv">
                      <li>
                        <c:out value="${sv.serviceName}" />
                        <c:if test="${not empty sv.description}">
                          <span class="muted"> - <c:out value="${sv.description}" /></span>
                        </c:if>
                      </li>
                    </c:forEach>
                  </ul>
                </c:if>

                <c:if test="${empty cCenter.therapyTypes and empty cCenter.services}">
                  <span class="muted">표시할 상세 정보가 없습니다.</span>
                </c:if>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:if>
  </div>

</body>
</html>