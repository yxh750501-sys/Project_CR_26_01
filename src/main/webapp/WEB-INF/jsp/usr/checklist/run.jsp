<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="java.lang.reflect.*" %>

<%!
  private String safe(Object v){ return v==null ? "" : String.valueOf(v); }

  private Object callGetter(Object obj, String getter){
    if(obj==null) return null;
    try{
      Method m = obj.getClass().getMethod(getter);
      return m.invoke(obj);
    }catch(Exception e){
      return null;
    }
  }

  private Object readField(Object obj, String getter, String key){
    if(obj==null) return null;

    if(obj instanceof Map){
      Map map = (Map)obj;
      Object v = map.get(key);
      if(v == null) v = map.get(key.toUpperCase());
      if(v == null) v = map.get(key.toLowerCase());
      return v;
    }

    Object v = callGetter(obj, getter);
    if(v != null) return v;

    if(getter.startsWith("get")){
      String alt = "is" + getter.substring(3);
      v = callGetter(obj, alt);
      if(v != null) return v;
    }

    return null;
  }

  private long toLong(Object v, long def){
    try{ return Long.parseLong(safe(v).trim()); }catch(Exception e){ return def; }
  }

  private int extractQNum(String qCode){
    String s = safe(qCode).trim().toUpperCase();
    if(s.isEmpty()) return -1;
    StringBuilder digits = new StringBuilder();
    for(int i=0;i<s.length();i++){
      char c = s.charAt(i);
      if(c>='0' && c<='9') digits.append(c);
    }
    if(digits.length()==0) return -1;
    try{ return Integer.parseInt(digits.toString()); }catch(Exception e){ return -1; }
  }

  private String inferDomainCode(String domainCode, String qCode){
    String d = safe(domainCode).trim();
    if(!d.isEmpty() && !"GENERAL".equalsIgnoreCase(d)) return d;

    int n = extractQNum(qCode);
    if(n>=1  && n<=6)  return "COMMUNICATION";
    if(n>=7  && n<=12) return "SENSORY_DAILY";
    if(n>=13 && n<=18) return "BEHAVIOR_EMOTION";
    if(n>=19 && n<=24) return "MOTOR_FINE";
    if(n>=25 && n<=30) return "PLAY_SOCIAL";
    return "GENERAL";
  }

  private String inferDomainTitle(String domainTitle, String dCode){
    String t = safe(domainTitle).trim();
    if(!t.isEmpty() && !"기타/미분류".equals(t)) return t;

    String c = safe(dCode).trim().toUpperCase();
    if("COMMUNICATION".equals(c)) return "의사소통(표현/이해)";
    if("SENSORY_DAILY".equals(c)) return "감각·일상(양치/옷/식사/과민)";
    if("BEHAVIOR_EMOTION".equals(c)) return "행동·정서(전환/폭발/위험)";
    if("MOTOR_FINE".equals(c)) return "운동·미세(연필/가위/협응)";
    if("PLAY_SOCIAL".equals(c)) return "놀이·사회(공동주의/또래)";
    return "기타/미분류";
  }

  private boolean isRiskQuestion(String qCode){
    return "Q13".equalsIgnoreCase(safe(qCode).trim());
  }

  private Object findAnswer(Map answersMap, long qId){
    if(answersMap == null) return null;

    Object a = answersMap.get(Long.valueOf(qId));
    if(a != null) return a;

    a = answersMap.get(Integer.valueOf((int)qId));
    if(a != null) return a;

    a = answersMap.get(String.valueOf(qId));
    if(a != null) return a;

    try{
      for(Object k : answersMap.keySet()){
        if(k == null) continue;
        if(String.valueOf(k).equals(String.valueOf(qId))) return answersMap.get(k);
      }
    }catch(Exception e){}

    return null;
  }

  private String domainDesc(String code){
    code = safe(code).trim().toUpperCase();

    if("COMMUNICATION".equals(code))
      return "이해(수용) + 표현 기능을 봅니다. 이름 반응, 지시 이해, 선택, 요구/거절, AAC 사용 시도 포함.";
    if("SENSORY_DAILY".equals(code))
      return "감각/거부가 일상(식사·위생·옷·외출/전환·수면)에 주는 영향을 봅니다.";
    if("BEHAVIOR_EMOTION".equals(code))
      return "전환/기다리기/폭발/회복과 안전(Q13)을 봅니다. Q13은 결과 해석보다 ‘안전 우선’입니다.";
    if("MOTOR_FINE".equals(code))
      return "손 사용/협응/미세 조작과 AAC 실제 사용 가능성(정확 터치, 바로 꺼내기)을 봅니다.";
    if("PLAY_SOCIAL".equals(code))
      return "공동주의·모방·턴테이킹·또래 관심 같은 사회적 놀이 기반을 봅니다.";
    return "기타/미분류 영역입니다.";
  }
%>

<%
  Object run = request.getAttribute("run");
  List questions = (List)request.getAttribute("questions");
  Map answersMap = (Map)request.getAttribute("answersMap");

  if(questions == null) questions = new ArrayList();
  if(answersMap == null) answersMap = new HashMap();

  long runId = toLong(readField(run, "getId", "id"), 0);

  Map domainMap = new LinkedHashMap();      // domainCode -> List(questions)
  Map domainTitleMap = new LinkedHashMap(); // domainCode -> title

  for(Object q : questions){
    String qCode = safe(readField(q, "getCode", "code")).trim();
    String rawDCode = safe(readField(q, "getDomainCode", "domainCode")).trim();
    String rawDTitle = safe(readField(q, "getDomainTitle", "domainTitle")).trim();

    String dCode = inferDomainCode(rawDCode, qCode);
    String dTitle = inferDomainTitle(rawDTitle, dCode);

    domainTitleMap.put(dCode, dTitle);

    List list = (List)domainMap.get(dCode);
    if(list == null){
      list = new ArrayList();
      domainMap.put(dCode, list);
    }
    list.add(q);
  }
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>체크리스트 진행</title>
<style>
  body{ font-family: Arial, sans-serif; }
  .card{ margin:14px 0; padding:14px; border:1px solid #ddd; border-radius:12px; }
  .badge{ display:inline-block; padding:6px 10px; border-radius:999px; font-weight:900; font-size:12px; border:1px solid #ddd; background:#f7f7f7; }
  .muted{ color:#555; font-size:13px; line-height:1.65; white-space:pre-line; }
  .guide{ background:#f6f8ff; border:1px solid #dbe3ff; }
  .guide .gt{ font-weight:900; font-size:15px; }
  .domainHeader{ margin-top:18px; padding:14px; border-radius:12px; border:1px solid #cfe0ff; background:#f6f8ff; }
  .domainHeader .dtitle{ font-weight:900; font-size:16px; }
  .domainHeader .ddesc{ margin-top:6px; color:#2b3a67; font-size:13px; line-height:1.55; white-space:pre-line; }
  .qcard{ margin:12px 0; padding:14px; border:1px solid #eee; border-radius:12px; }
  .qtitle{ font-weight:900; margin-bottom:8px; }
  .help{ margin-top:10px; padding:12px; border-radius:12px; background:#fcfcfc; border:1px solid #eee; white-space:pre-line; color:#333; font-size:13px; line-height:1.65; }
  select, textarea{ width:100%; padding:10px; border:1px solid #ddd; border-radius:10px; font-size:14px; margin-top:8px; }
  .actions{ margin-top:16px; }
  button{ padding:12px 16px; border:0; border-radius:12px; font-weight:900; cursor:pointer; }
  .btn{ background:#111; color:#fff; }
</style>
</head>
<body>

<h2>체크리스트 진행</h2>

<div class="card guide">
  <div class="gt">SCALE5 라벨 선택 기준(기회 10번 중 성공 횟수)</div>
  <div class="muted" style="margin-top:8px;">
- 불가능: 0~1회
- 거의 불가능: 2~4회
- 거의 가능: 5~7회
- 가능함: 8~10회
- 모름: 기회를 충분히 만들지 못함/확신 없음
  </div>
  <div class="muted" style="margin-top:10px;">
<b>AAC 보드</b>: 그림/사진/카드/태블릿 등으로 ‘선택/요구/거절’을 돕는 자료(말 대신/말 보조).
  </div>
</div>

<div class="card">
  <div>Run ID: <b><%= runId==0 ? "-" : runId %></b> <span class="badge">기능 5영역</span></div>
</div>

<form method="post" action="/usr/checklist/doSubmit">
  <input type="hidden" name="runId" value="<%= runId %>"/>

  <%
    for(Object dCodeObj : domainMap.keySet()){
      String dCode = safe(dCodeObj);
      String dTitle = safe(domainTitleMap.get(dCode));
      List qs = (List)domainMap.get(dCode);
  %>

    <div class="domainHeader">
      <div class="dtitle"><%= dTitle %></div>
      <div class="ddesc"><%= domainDesc(dCode) %></div>
    </div>

    <%
      for(Object q : qs){
        long qId = toLong(readField(q, "getId", "id"), 0);

        String qCode = safe(readField(q, "getCode", "code")).trim();
        String qText = safe(readField(q, "getQuestionText", "questionText")).trim();
        String helpText = safe(readField(q, "getHelpText", "helpText"));

        String rt = safe(readField(q, "getResponseType", "responseType")).trim().toUpperCase();

        Object ans = findAnswer(answersMap, qId);
        String curV = safe(readField(ans, "getAnswerValue", "answerValue")).trim();
        String curT = safe(readField(ans, "getAnswerText", "answerText"));

        boolean isText = "TEXT".equals(rt);
        boolean isYN = "YN".equals(rt);
        boolean isRisk = isRiskQuestion(qCode);
    %>

      <div class="qcard">
        <div class="qtitle"><%= qCode %>. <%= qText %></div>

        <% if(isText) { %>
          <textarea name="t_<%= qId %>" rows="4" placeholder="내용을 입력하세요"><%= curT %></textarea>
        <% } else { %>

          <select name="v_<%= qId %>" required>
            <option value="">선택하세요</option>

            <% if(isYN || isRisk) { %>
              <option value="4" <%= "4".equals(curV) ? "selected" : "" %>>예</option>
              <option value="1" <%= "1".equals(curV) ? "selected" : "" %>>아니오</option>
              <option value="0" <%= "0".equals(curV) ? "selected" : "" %>>모름</option>
            <% } else { %>
              <option value="1" <%= "1".equals(curV) ? "selected" : "" %>>불가능</option>
              <option value="2" <%= "2".equals(curV) ? "selected" : "" %>>거의 불가능</option>
              <option value="3" <%= "3".equals(curV) ? "selected" : "" %>>거의 가능</option>
              <option value="4" <%= "4".equals(curV) ? "selected" : "" %>>가능함</option>
              <option value="0" <%= "0".equals(curV) ? "selected" : "" %>>모름</option>
            <% } %>
          </select>

          <textarea name="t_<%= qId %>" rows="2" placeholder="메모(선택): 관찰한 상황/특이사항을 적어두면 해석이 더 정확해집니다."><%= curT %></textarea>
        <% } %>

        <% if(helpText != null && !helpText.trim().isEmpty()) { %>
          <div class="help"><%= helpText %></div>
        <% } %>
      </div>

    <% } %>
  <% } %>

  <div class="actions">
    <button class="btn" type="submit" onclick="return confirm('제출하면 결과가 저장됩니다. 제출할까요?');">제출</button>
  </div>
</form>

<p style="margin-top:18px;">
  <a href="/usr/checklist/start">체크리스트 목록</a> |
  <a href="/usr/child/list">아이 프로필 관리</a>
</p>

</body>
</html>
