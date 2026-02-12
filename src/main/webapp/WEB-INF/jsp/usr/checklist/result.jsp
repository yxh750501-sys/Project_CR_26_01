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

  private int toInt(Object v, int def){
    try{ return Integer.parseInt(safe(v).trim()); }catch(Exception e){ return def; }
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

  private String labelScale(String v){
    v = safe(v).trim();
    if("4".equals(v)) return "가능함";
    if("3".equals(v)) return "거의 가능";
    if("2".equals(v)) return "거의 불가능";
    if("1".equals(v)) return "불가능";
    if("0".equals(v)) return "모름";
    return v;
  }

  private String labelYN(String v){
    v = safe(v).trim();
    if("4".equals(v)) return "예";
    if("1".equals(v)) return "아니오";
    if("0".equals(v)) return "모름";
    return v;
  }

  private String labelByAvg(double avg){
    if(avg <= 1.6) return "불가능 중심";
    if(avg <= 2.4) return "거의 불가능 중심";
    if(avg <= 3.2) return "거의 가능 중심";
    return "가능 중심";
  }
%>

<%
  Object run = request.getAttribute("run");
  List questions = (List)request.getAttribute("questions");
  Map answersMap = (Map)request.getAttribute("answersMap");

  if(questions == null) questions = new ArrayList();
  if(answersMap == null) answersMap = new HashMap();

  long runId = toLong(readField(run, "getId", "id"), 0);
  String status = safe(readField(run, "getStatus", "status"));
  String totalScore = safe(readField(run, "getTotalScore", "totalScore"));

  Map stats = new LinkedHashMap(); // domainCode -> {title,sumW,wSum,cnt}

  String riskLabel = "";
  String riskMemo = "";

  for(Object q : questions){
    String rt = safe(readField(q, "getResponseType", "responseType")).trim().toUpperCase();
    String qCode = safe(readField(q, "getCode", "code")).trim();
    long qId = toLong(readField(q, "getId", "id"), 0);

    Object a = answersMap.get(Long.valueOf(qId));
    if(a==null) a = answersMap.get(Integer.valueOf((int)qId));
    if(a==null) a = answersMap.get(String.valueOf(qId));
    if(a==null){
      try{
        for(Object k : answersMap.keySet()){
          if(k!=null && String.valueOf(k).equals(String.valueOf(qId))){ a = answersMap.get(k); break; }
        }
      }catch(Exception e){}
    }
    if(a == null) continue;

    String av = safe(readField(a, "getAnswerValue", "answerValue")).trim();
    String at = safe(readField(a, "getAnswerText", "answerText"));

    if(av.isEmpty()) continue;

    if(isRiskQuestion(qCode)){
      riskLabel = labelYN(av);
      riskMemo = at;
      continue;
    }

    /* ✅ YN은 평균 점수 계산에서 제외(해석 왜곡 방지) */
    if("YN".equals(rt)) continue;
    if("TEXT".equals(rt)) continue;

    int v = toInt(av, 0);
    if(v <= 0) continue; // 모름 제외

    int w = toInt(readField(q, "getWeight", "weight"), 1);
    if(w <= 0) w = 1;

    String rawDCode = safe(readField(q, "getDomainCode", "domainCode")).trim();
    String rawDTitle = safe(readField(q, "getDomainTitle", "domainTitle")).trim();

    String dCode = inferDomainCode(rawDCode, qCode);
    String dTitle = inferDomainTitle(rawDTitle, dCode);

    Map st = (Map)stats.get(dCode);
    if(st == null){
      st = new HashMap();
      st.put("title", dTitle);
      st.put("sumW", Double.valueOf(0));
      st.put("wSum", Double.valueOf(0));
      st.put("cnt", Integer.valueOf(0));
      stats.put(dCode, st);
    }

    double sumW = ((Double)st.get("sumW")).doubleValue();
    double wSum = ((Double)st.get("wSum")).doubleValue();
    int cnt = ((Integer)st.get("cnt")).intValue();

    sumW += (v * w);
    wSum += w;
    cnt += 1;

    st.put("sumW", Double.valueOf(sumW));
    st.put("wSum", Double.valueOf(wSum));
    st.put("cnt", Integer.valueOf(cnt));
  }

  List domainRows = new ArrayList();
  for(Object key : stats.keySet()){
    String dCode = String.valueOf(key);
    Map st = (Map)stats.get(dCode);
    double sumW = ((Double)st.get("sumW")).doubleValue();
    double wSum = ((Double)st.get("wSum")).doubleValue();
    int cnt = ((Integer)st.get("cnt")).intValue();
    double avg = (wSum<=0 ? 0 : (sumW/wSum));

    Map row = new HashMap();
    row.put("code", dCode);
    row.put("title", st.get("title"));
    row.put("avg", Double.valueOf(avg));
    row.put("cnt", Integer.valueOf(cnt));
    domainRows.add(row);
  }

  Collections.sort(domainRows, new Comparator(){
    public int compare(Object o1, Object o2){
      Map a = (Map)o1;
      Map b = (Map)o2;
      double av = ((Double)a.get("avg")).doubleValue();
      double bv = ((Double)b.get("avg")).doubleValue();
      return Double.compare(av, bv);
    }
  });

  List weakTop = new ArrayList();
  for(int i=0; i<domainRows.size() && weakTop.size()<3; i++){
    Map row = (Map)domainRows.get(i);
    int cnt = ((Integer)row.get("cnt")).intValue();
    if(cnt >= 1) weakTop.add(row);
  }
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>체크리스트 결과</title>
<style>
  body{ font-family: Arial, sans-serif; }
  .card{ margin:14px 0; padding:14px; border:1px solid #ddd; border-radius:12px; }
  .title{ font-weight:900; font-size:18px; margin-bottom:8px; }
  .small{ color:#555; font-size:13px; white-space:pre-line; line-height:1.6; }
  table{ width:100%; border-collapse:collapse; margin-top:8px; }
  th, td{ padding:10px; border-bottom:1px solid #eee; text-align:left; }
  .pill{ display:inline-block; padding:6px 10px; border-radius:999px; font-weight:900; font-size:12px; border:1px solid #ddd; background:#f7f7f7; }
  .bad{ background:#ffecef; border:1px solid #ffc2cc; }
  .warn{ background:#fff7e6; border:1px solid #ffe0a3; }
  .good{ background:#e9f8ee; border:1px solid #bfe8c9; }
</style>
</head>
<body>

<h2>체크리스트 결과</h2>

<div class="card">
  <div>Run ID: <b><%= (runId==0 ? "-" : runId) %></b></div>
  <div>상태: <b><%= safe(status) %></b></div>
  <div>총점: <b><%= safe(totalScore) %></b></div>
</div>

<% if(riskLabel != null && !riskLabel.trim().isEmpty()) { %>
  <div class="card" style="background:#fff7e6;border:1px solid #ffe0a3;">
    <div class="title">안전 체크(Q13)</div>
    <div class="small">
최근 7일 기준 위험(자/타해) 여부: <b><%= riskLabel %></b>
<% if(riskMemo != null && !riskMemo.trim().isEmpty()) { %>
메모: <%= riskMemo %>
<% } %>

※ ‘예’라면 결과 해석보다 안전 계획/전문가 상담이 우선입니다.
    </div>
  </div>
<% } %>

<div class="card">
  <div class="title">기능 5영역 요약(SCALE5만, 모름 제외 평균)</div>

  <% if(domainRows.isEmpty()) { %>
    <div class="small">요약을 계산할 SCALE5 응답이 부족합니다(모름이 많거나 미응답).</div>
  <% } else { %>
    <table>
      <thead>
        <tr>
          <th>기능 영역</th>
          <th>평균(1~4)</th>
          <th>해석</th>
          <th>문항 수(모름 제외)</th>
        </tr>
      </thead>
      <tbody>
        <%
          for(int i=0;i<domainRows.size();i++){
            Map row = (Map)domainRows.get(i);
            String dTitle = safe(row.get("title"));
            double avg = ((Double)row.get("avg")).doubleValue();
            int cnt = ((Integer)row.get("cnt")).intValue();
            String tagClass = (avg<=2.2? "bad" : (avg<=3.0? "warn" : "good"));
        %>
          <tr>
            <td><b><%= dTitle %></b></td>
            <td><span class="pill <%= tagClass %>"><%= String.format("%.2f", avg) %></span></td>
            <td><%= labelByAvg(avg) %></td>
            <td><%= cnt %></td>
          </tr>
        <% } %>
      </tbody>
    </table>
  <% } %>
</div>

<p>
  <a href="/usr/checklist/history">지난 결과(히스토리)</a> |
  <a href="/usr/checklist/start">체크리스트 목록</a> |
  <a href="/usr/child/list">아이 프로필 관리</a>
</p>

</body>
</html>
