<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="java.lang.reflect.*" %>

<%!
  private String s(Object v){ return v==null ? "" : String.valueOf(v); }

  private Object call(Object obj, String m){
    if(obj==null) return null;
    try{
      Method mm = obj.getClass().getMethod(m);
      return mm.invoke(obj);
    }catch(Exception e){
      return null;
    }
  }

  private Object get(Object obj, String getter, String key){
    if(obj==null) return null;

    if(obj instanceof Map){
      Map map = (Map)obj;
      Object v = map.get(key);
      if(v==null) v = map.get(key.toUpperCase());
      if(v==null) v = map.get(key.toLowerCase());
      return v;
    }

    Object v = call(obj, getter);
    if(v!=null) return v;

    if(getter.startsWith("get")){
      String alt = "is" + getter.substring(3);
      v = call(obj, alt);
      if(v!=null) return v;
    }
    return null;
  }

  private long toL(Object v, long def){
    try{ return Long.parseLong(s(v).trim()); }catch(Exception e){ return def; }
  }
%>

<%
  Object runsObj = request.getAttribute("runs");
  if(runsObj==null) runsObj = request.getAttribute("historyRuns");
  if(runsObj==null) runsObj = request.getAttribute("runList");
  if(runsObj==null) runsObj = request.getAttribute("checklistRuns");

  List runs = (runsObj instanceof List) ? (List)runsObj : new ArrayList();

  Map recoSummaryMap = (Map)request.getAttribute("recoSummaryMap");
  if(recoSummaryMap==null) recoSummaryMap = new HashMap();
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>체크리스트 히스토리</title>
<style>
  body{ font-family: Arial, sans-serif; margin:0; padding:18px; background:#fafafa; }
  .wrap{ max-width:980px; margin:0 auto; }
  .card{ background:#fff; border:1px solid #e8e8e8; border-radius:14px; padding:16px; margin:14px 0; }
  .h1{ font-size:20px; font-weight:900; margin:0 0 10px; }
  table{ width:100%; border-collapse:collapse; }
  th, td{ border-bottom:1px solid #f0f0f0; padding:10px; text-align:left; }
  th{ font-size:13px; }
  td{ font-size:14px; vertical-align:top; }
  .sub{ color:#555; font-size:13px; line-height:1.6; white-space:pre-line; }
  a{ color:#111; font-weight:900; text-decoration:none; }
  .pill{ display:inline-block; padding:5px 10px; border-radius:999px; font-weight:900; font-size:12px; border:1px solid #ddd; background:#f7f7f7; }
</style>
</head>
<body>
<div class="wrap">

  <div class="card">
    <div class="h1">체크리스트 히스토리</div>
    <div class="sub">각 실행(run)의 결과와 추천 요약을 확인할 수 있습니다.</div>
  </div>

  <div class="card">
    <% if(runs.isEmpty()) { %>
      <div class="sub">히스토리가 없습니다.</div>
    <% } else { %>
      <table>
        <thead>
          <tr>
            <th style="width:90px;">Run</th>
            <th style="width:140px;">상태</th>
            <th style="width:180px;">날짜</th>
            <th>추천 요약</th>
            <th style="width:100px;">링크</th>
          </tr>
        </thead>
        <tbody>
          <%
            for(int i=0;i<runs.size();i++){
              Object r = runs.get(i);
              long runId = toL(get(r, "getId", "id"), 0);
              String status = s(get(r, "getStatus", "status"));
              String date = s(get(r, "getSubmittedDate", "submittedDate"));
              if(date==null || date.trim().isEmpty()) date = s(get(r, "getRegDate", "regDate"));

              String summary = s(recoSummaryMap.get(Long.valueOf(runId)));
              if(summary==null || summary.trim().isEmpty()) summary = "-";
          %>
            <tr>
              <td><b><%= runId %></b></td>
              <td><span class="pill"><%= (status==null || status.trim().isEmpty()) ? "-" : status %></span></td>
              <td><%= (date==null || date.trim().isEmpty()) ? "-" : date %></td>
              <td><%= summary %></td>
              <td><a href="/usr/checklist/result?runId=<%= runId %>">결과</a></td>
            </tr>
          <%
            }
          %>
        </tbody>
      </table>
    <% } %>
  </div>

  <div class="card">
    <a href="/usr/checklist/start">체크리스트 목록</a> |
    <a href="/usr/child/list">아이 프로필 관리</a>
  </div>

</div>
</body>
</html>
