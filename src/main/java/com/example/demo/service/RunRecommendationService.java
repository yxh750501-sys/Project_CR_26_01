package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.RunRecommendationMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RunRecommendationService {

	private final RunRecommendationMapper runRecommendationMapper;

	@Transactional
	public void ensureGenerated(long runId, long userId) {
		if (runId <= 0 || userId <= 0) return;

		int exists = runRecommendationMapper.countRunRecommendationsForUser(runId, userId);
		if (exists > 0) return;

		Map<String, Object> runInfo = runRecommendationMapper.getRunInfoForUser(runId, userId);
		if (runInfo == null || runInfo.isEmpty()) return;

		// 혹시 이전에 실패한 흔적이 있으면 재생성 가능하도록 클린업(안전)
		runRecommendationMapper.deleteEvidenceByRunId(runId);
		runRecommendationMapper.deleteRunRecommendationsByRunId(runId);

		List<Map<String, Object>> rows = runRecommendationMapper.getAnsweredRowsForRun(runId);
		if (rows == null) rows = new ArrayList<>();

		// Q13(안전), Q16(강화물) 해석
		String q13 = findAnswerValue(rows, "Q13");
		String q13Memo = findAnswerText(rows, "Q13");
		String q16 = findAnswerValue(rows, "Q16");
		String q16Memo = findAnswerText(rows, "Q16");

		// 1) 안전 우선(있으면 무조건 최상단 추천 저장)
		if ("4".equals(q13)) {
			insertRecoWithEvidence(runId, "RECO_SAFETY_PLAN",
					"최근 7일 기준 위험 신호가 확인되어 안전/상담/환경 조정이 최우선입니다.",
					buildYnEvidence("Q13", "안전(자/타해 위험) 신호가 있었나요?", q13, q13Memo));
		}

		// 2) 강화물 확보(없으면 우선 추천)
		if ("1".equals(q16)) {
			insertRecoWithEvidence(runId, "RECO_REINFORCER_LIST",
					"강화물이 부족하면 전환/기다리기/AAC 훈련이 잘 유지되지 않습니다. 즉시 제공 가능한 강화물부터 확보하세요.",
					buildYnEvidence("Q16", "효과적인 강화물을 ‘구체 목록’으로 확보했나요?", q16, q16Memo));
		}

		// 3) 기능영역 TOP3(낮은 평균)
		Map<String, DomainStat> stats = buildDomainStats(rows);

		List<DomainStat> domainList = new ArrayList<>(stats.values());
		Collections.sort(domainList, Comparator.comparingDouble(DomainStat::avg));

		int picked = 0;
		for (DomainStat ds : domainList) {
			if (picked >= 3) break;

			// 너무 응답이 적으면(모름 제외 카운트) 추천 정확도가 떨어져서 스킵
			if (ds.count < 2) continue;

			String recoCode = domainRecoCode(ds.domainCode);
			String reason = buildReason(ds);

			List<String> evidences = buildScaleEvidences(ds);

			if (evidences.isEmpty()) {
				evidences.add(ds.domainTitle + " 영역에서 낮은 응답이 뚜렷하지 않거나 ‘모름’이 많았습니다. 다음 실행에서 관찰 기회를 10번 만들어 정확도를 올려주세요.");
			}

			insertRecoWithEvidence(runId, recoCode, reason, evidences);
			picked++;
		}
	}

	public List<Map<String, Object>> getRunRecommendationsWithEvidence(long runId, long userId) {
		if (runId <= 0 || userId <= 0) return new ArrayList<>();

		List<Map<String, Object>> recos = runRecommendationMapper.getRunRecommendationsForUser(runId, userId);
		List<Map<String, Object>> evs = runRecommendationMapper.getRunRecommendationEvidenceForUser(runId, userId);

		Map<Long, List<Map<String, Object>>> byRrId = new HashMap<>();
		for (Map<String, Object> e : evs) {
			long rrId = toL(e.get("runRecommendationId"), 0);
			byRrId.computeIfAbsent(rrId, k -> new ArrayList<>()).add(e);
		}

		for (Map<String, Object> r : recos) {
			long rrId = toL(r.get("runRecommendationId"), 0);
			List<Map<String, Object>> list = byRrId.getOrDefault(rrId, new ArrayList<>());
			r.put("evidenceList", list);
		}
		return recos;
	}

	public Map<Long, String> getRecoSummaryByRunIds(long userId, List<Long> runIds) {
		Map<Long, String> out = new HashMap<>();
		if (userId <= 0 || runIds == null || runIds.isEmpty()) return out;

		List<Map<String, Object>> rows = runRecommendationMapper.getRecoSummariesByRunIdsForUser(userId, runIds);
		for (Map<String, Object> r : rows) {
			long runId = toL(r.get("runId"), 0);
			String titles = s(r.get("titles"));
			out.put(runId, titles);
		}
		return out;
	}

	/* =========================
	   내부 로직
	   ========================= */

	private void insertRecoWithEvidence(long runId, String recoCode, String reasonText, List<String> evidenceTexts) {
		Long recoId = runRecommendationMapper.getRecommendationIdByCode(recoCode);
		if (recoId == null) return;

		runRecommendationMapper.insertRunRecommendation(runId, recoId.longValue(), reasonText);
		Long rrId = runRecommendationMapper.getLastInsertId();
		if (rrId == null) return;

		for (String e : evidenceTexts) {
			runRecommendationMapper.insertEvidence(rrId.longValue(), null, e);
		}
	}

	private void insertRecoWithEvidence(long runId, String recoCode, String reasonText, String singleEvidence) {
		List<String> ls = new ArrayList<>();
		ls.add(singleEvidence);
		insertRecoWithEvidence(runId, recoCode, reasonText, ls);
	}

	private List<String> buildYnEvidence(String qCode, String qTitle, String av, String memo) {
		List<String> ls = new ArrayList<>();
		String label = labelYn(av);
		String base = "[" + qCode + "] " + qTitle + " → " + label;
		if (memo != null && !memo.trim().isEmpty()) base += " / 메모: " + memo.trim();
		ls.add(base);
		return ls;
	}

	private Map<String, DomainStat> buildDomainStats(List<Map<String, Object>> rows) {
		Map<String, DomainStat> stats = new LinkedHashMap<>();

		for (Map<String, Object> r : rows) {
			String qCode = s(r.get("questionCode")).trim().toUpperCase();
			String rt = s(r.get("responseType")).trim().toUpperCase();
			String av = s(r.get("answerValue")).trim();
			String qText = s(r.get("questionText")).trim();
			int weight = toI(r.get("weight"), 1);
			if (weight <= 0) weight = 1;

			if (!"SCALE5".equals(rt)) continue;

			int v = toI(av, 0);
			if (v <= 0) continue; // 모름 제외

			String dCode = domainCodeByQ(qCode);
			String dTitle = domainTitle(dCode);

			DomainStat ds = stats.get(dCode);
			if (ds == null) {
				ds = new DomainStat(dCode, dTitle);
				stats.put(dCode, ds);
			}

			ds.sum += (v * weight);
			ds.wSum += weight;
			ds.count += 1;

			if (v <= 2) {
				ds.weakItems.add(new WeakItem(qCode, qText, v, s(r.get("answerText"))));
			}
		}

		return stats;
	}

	private String buildReason(DomainStat ds) {
		double avg = ds.avg();
		String lv = (avg <= 2.2) ? "우선 개입 필요" : (avg <= 3.0 ? "경계(상황 따라 흔들림)" : "기반 유지(확장)");
		String lowCodes = "";
		for (int i = 0; i < ds.weakItems.size() && i < 2; i++) {
			if (i > 0) lowCodes += ", ";
			lowCodes += ds.weakItems.get(i).qCode;
		}
		if (!lowCodes.isEmpty()) lowCodes = " 낮은 문항: " + lowCodes + ".";
		return ds.domainTitle + " 평균 " + String.format("%.2f", avg) + " (" + lv + ")." + lowCodes;
	}

	private List<String> buildScaleEvidences(DomainStat ds) {
		List<String> ls = new ArrayList<>();
		for (int i = 0; i < ds.weakItems.size() && i < 6; i++) {
			WeakItem w = ds.weakItems.get(i);
			String base = "[" + w.qCode + "] " + w.qText + " → " + labelScale(w.value)
					+ " (최근 7일, 기회 10번 기준)";
			if (w.memo != null && !w.memo.trim().isEmpty()) base += " / 메모: " + w.memo.trim();
			ls.add(base);
		}
		return ls;
	}

	private String domainRecoCode(String dCode) {
		if ("COMMUNICATION".equals(dCode)) return "RECO_COMMUNICATION_AAC";
		if ("SENSORY_DAILY".equals(dCode)) return "RECO_SENSORY_DAILY_ROUTINE";
		if ("BEHAVIOR_EMOTION".equals(dCode)) return "RECO_BEHAVIOR_EMOTION_REGULATION";
		if ("MOTOR_FINE".equals(dCode)) return "RECO_MOTOR_FINE_ACCESS";
		if ("PLAY_SOCIAL".equals(dCode)) return "RECO_PLAY_SOCIAL_JOINT";
		return "RECO_COMMUNICATION_AAC";
	}

	private String domainCodeByQ(String qCode) {
		int n = qNum(qCode);
		if (n >= 1 && n <= 6) return "COMMUNICATION";
		if (n >= 7 && n <= 12) return "SENSORY_DAILY";
		if (n >= 13 && n <= 18) return "BEHAVIOR_EMOTION";
		if (n >= 19 && n <= 24) return "MOTOR_FINE";
		if (n >= 25 && n <= 30) return "PLAY_SOCIAL";
		return "COMMUNICATION";
	}

	private String domainTitle(String dCode) {
		if ("COMMUNICATION".equals(dCode)) return "의사소통(표현/이해)";
		if ("SENSORY_DAILY".equals(dCode)) return "감각·일상(양치/옷/식사/과민)";
		if ("BEHAVIOR_EMOTION".equals(dCode)) return "행동·정서(전환/폭발/위험)";
		if ("MOTOR_FINE".equals(dCode)) return "운동·미세(연필/가위/협응)";
		if ("PLAY_SOCIAL".equals(dCode)) return "놀이·사회(공동주의/또래)";
		return "의사소통(표현/이해)";
	}

	private String findAnswerValue(List<Map<String, Object>> rows, String qCode) {
		for (Map<String, Object> r : rows) {
			String code = s(r.get("questionCode")).trim().toUpperCase();
			if (qCode.equalsIgnoreCase(code)) return s(r.get("answerValue")).trim();
		}
		return "";
	}

	private String findAnswerText(List<Map<String, Object>> rows, String qCode) {
		for (Map<String, Object> r : rows) {
			String code = s(r.get("questionCode")).trim().toUpperCase();
			if (qCode.equalsIgnoreCase(code)) return s(r.get("answerText"));
		}
		return "";
	}

	private String labelScale(int v) {
		if (v == 4) return "가능함";
		if (v == 3) return "거의 가능";
		if (v == 2) return "거의 불가능";
		if (v == 1) return "불가능";
		return "모름";
	}

	private String labelYn(String v) {
		if ("4".equals(v)) return "예";
		if ("1".equals(v)) return "아니오";
		return "모름";
	}

	private int qNum(String qCode) {
		String t = s(qCode).trim().toUpperCase();
		StringBuilder d = new StringBuilder();
		for (int i = 0; i < t.length(); i++) {
			char c = t.charAt(i);
			if (c >= '0' && c <= '9') d.append(c);
		}
		if (d.length() == 0) return -1;
		try { return Integer.parseInt(d.toString()); } catch (Exception e) { return -1; }
	}

	private long toL(Object v, long def) {
		try { return Long.parseLong(s(v).trim()); } catch (Exception e) { return def; }
	}

	private int toI(Object v, int def) {
		try { return Integer.parseInt(s(v).trim()); } catch (Exception e) { return def; }
	}

	private String s(Object v) {
		return v == null ? "" : String.valueOf(v);
	}

	private static class DomainStat {
		String domainCode;
		String domainTitle;
		double sum = 0;
		double wSum = 0;
		int count = 0;
		List<WeakItem> weakItems = new ArrayList<>();

		DomainStat(String domainCode, String domainTitle) {
			this.domainCode = domainCode;
			this.domainTitle = domainTitle;
		}

		double avg() {
			if (wSum <= 0) return 0;
			return sum / wSum;
		}
	}

	private static class WeakItem {
		String qCode;
		String qText;
		int value;
		String memo;

		WeakItem(String qCode, String qText, int value, String memo) {
			this.qCode = qCode;
			this.qText = qText;
			this.value = value;
			this.memo = memo;
		}
	}
}
