package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.ChecklistMapper;
import com.example.demo.vo.Checklist;
import com.example.demo.vo.ChecklistAnswer;
import com.example.demo.vo.ChecklistQuestion;
import com.example.demo.vo.ChecklistRun;
import com.example.demo.vo.RecommendationRule;
import com.example.demo.vo.RunRecommendationEvidence;
import com.example.demo.vo.RunRecommendationItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChecklistService {

	private final ChecklistMapper checklistMapper;
	private final ObjectMapper objectMapper;

	public ChecklistService(ChecklistMapper checklistMapper, ObjectMapper objectMapper) {
		this.checklistMapper = checklistMapper;
		this.objectMapper = objectMapper;
	}

	public List<Checklist> getChecklists() {
		return checklistMapper.getChecklists();
	}

	public Checklist getChecklistById(long id) {
		return checklistMapper.getChecklistById(id);
	}

	public List<ChecklistQuestion> getQuestionsByChecklistId(long checklistId) {
		return checklistMapper.getQuestionsByChecklistId(checklistId);
	}

	@Transactional
	public long startRun(long checklistId, long childId, long userId) {
		checklistMapper.insertRun(checklistId, childId, userId);
		return checklistMapper.getLastInsertId();
	}

	public ChecklistRun getRunByIdAndUserId(long runId, long userId) {
		return checklistMapper.getRunByIdAndUserId(runId, userId);
	}

	public Map<Long, ChecklistAnswer> getAnswerMapByRunId(long runId) {
		List<ChecklistAnswer> answers = checklistMapper.getAnswersByRunId(runId);
		Map<Long, ChecklistAnswer> map = new HashMap<>();
		for (ChecklistAnswer a : answers) {
			map.put(a.getQuestionId(), a);
		}
		return map;
	}

	private Integer calcScore(ChecklistQuestion q, String answerValue) {
		if (answerValue == null) return 0;

		String type = q.getResponseType();
		int w = q.getWeight();

		if ("YN".equalsIgnoreCase(type)) {
			return "Y".equalsIgnoreCase(answerValue) ? w : 0;
		}

		if ("SCALE5".equalsIgnoreCase(type)) {
			try {
				int v = Integer.parseInt(answerValue);
				if (v < 1) v = 1;
				if (v > 5) v = 5;
				return v * w;
			} catch (Exception e) {
				return 0;
			}
		}

		return 0;
	}

	@Transactional
	public SaveResult saveOrSubmit(long runId, long userId, List<ChecklistQuestion> questions, Map<Long, String> answerValueMap,
			Map<Long, String> answerTextMap, boolean submit) {

		ChecklistRun run = checklistMapper.getRunByIdAndUserId(runId, userId);
		if (run == null) {
			return new SaveResult(false, "실행 정보를 찾을 수 없습니다.", 0, false);
		}
		if (!"DRAFT".equalsIgnoreCase(run.getStatus())) {
			return new SaveResult(false, "이미 제출된 체크리스트입니다.", run.getTotalScore(), true);
		}

		int total = 0;

		for (ChecklistQuestion q : questions) {
			long qid = q.getId();

			String v = answerValueMap.get(qid);
			String t = answerTextMap.get(qid);

			if (v != null) v = v.trim();
			if (t != null) t = t.trim();

			Integer score = calcScore(q, v);
			total += (score == null ? 0 : score);

			checklistMapper.upsertAnswer(runId, qid, v, t, score);
		}

		if (submit) {
			int updated = checklistMapper.submitRun(runId, userId, total);
			if (updated == 0) {
				return new SaveResult(false, "제출에 실패했습니다.", total, false);
			}

			applyRecommendations(runId, userId, run.getChecklistId(), total, questions);

			return new SaveResult(true, "제출되었습니다.", total, true);
		}

		checklistMapper.updateRunTotalScore(runId, userId, total);
		return new SaveResult(true, "저장되었습니다.", total, false);
	}

	private void applyRecommendations(long runId, long userId, long checklistId, int totalScore, List<ChecklistQuestion> questions) {
		Map<String, ChecklistQuestion> questionByCode = new HashMap<>();
		Map<Long, ChecklistQuestion> questionById = new HashMap<>();
		for (ChecklistQuestion q : questions) {
			questionByCode.put(q.getCode(), q);
			questionById.put(q.getId(), q);
		}

		Map<Long, ChecklistAnswer> answers = getAnswerMapByRunId(runId);

		List<RecommendationRule> rules = checklistMapper.getRecommendationRulesByChecklistId(checklistId);

		Map<Long, List<RecommendationRule>> rulesByReco = new HashMap<>();
		for (RecommendationRule r : rules) {
			rulesByReco.computeIfAbsent(r.getRecommendationId(), k -> new ArrayList<>()).add(r);
		}

		checklistMapper.deleteRunRecommendationsByRunId(runId);

		for (Map.Entry<Long, List<RecommendationRule>> entry : rulesByReco.entrySet()) {
			long recoId = entry.getKey();
			List<RecommendationRule> recoRules = entry.getValue();

			boolean allPass = true;
			List<EvidenceDraft> evidences = new ArrayList<>();

			for (RecommendationRule rule : recoRules) {
				EvalResult eval = evalRule(rule, totalScore, questionByCode, answers);
				if (!eval.pass) {
					allPass = false;
					break;
				}
				evidences.addAll(eval.evidences);
			}

			if (!allPass) continue;

			String reason = "응답 결과에 따라 추천됩니다.";
			checklistMapper.insertRunRecommendation(runId, recoId, reason);
			long runRecoId = checklistMapper.getLastInsertId();

			for (EvidenceDraft ed : evidences) {
				checklistMapper.insertRunRecommendationEvidence(runRecoId, ed.questionId, ed.text);
			}
		}
	}

	private static class EvidenceDraft {
		Long questionId;
		String text;

		EvidenceDraft(Long questionId, String text) {
			this.questionId = questionId;
			this.text = text;
		}
	}

	private static class EvalResult {
		boolean pass;
		List<EvidenceDraft> evidences;

		EvalResult(boolean pass, List<EvidenceDraft> evidences) {
			this.pass = pass;
			this.evidences = evidences;
		}
	}

	private EvalResult evalRule(RecommendationRule rule, int totalScore, Map<String, ChecklistQuestion> questionByCode,
			Map<Long, ChecklistAnswer> answersByQid) {

		String type = rule.getRuleType();
		String params = rule.getParamsJson();

		List<EvidenceDraft> evidences = new ArrayList<>();

		try {
			JsonNode node = objectMapper.readTree(params == null ? "{}" : params);

			if ("TOTAL_SCORE_RANGE".equalsIgnoreCase(type)) {
				int min = node.has("min") ? node.get("min").asInt(Integer.MIN_VALUE) : Integer.MIN_VALUE;
				int max = node.has("max") ? node.get("max").asInt(Integer.MAX_VALUE) : Integer.MAX_VALUE;

				boolean pass = totalScore >= min && totalScore <= max;
				evidences.add(new EvidenceDraft(null, "총점 " + totalScore + "이(가) 범위(" + min + "~" + max + ")에 해당"));
				return new EvalResult(pass, evidences);
			}

			if ("ANSWER_MATCH".equalsIgnoreCase(type)) {
				String questionCode = node.has("questionCode") ? node.get("questionCode").asText() : null;
				if (questionCode == null || questionCode.isBlank()) {
					return new EvalResult(false, evidences);
				}

				ChecklistQuestion q = questionByCode.get(questionCode);
				if (q == null) return new EvalResult(false, evidences);

				ChecklistAnswer a = answersByQid.get(q.getId());
				String av = a == null ? null : a.getAnswerValue();

				if (node.has("answer")) {
					String expected = node.get("answer").asText();
					boolean pass = expected != null && expected.equalsIgnoreCase(av == null ? "" : av);
					evidences.add(new EvidenceDraft(q.getId(), questionCode + " 응답이 '" + expected + "' 인 경우 (현재: " + (av == null ? "미응답" : av) + ")"));
					return new EvalResult(pass, evidences);
				}

				if (node.has("max")) {
					int max = node.get("max").asInt();
					int v = 999;
					try { v = Integer.parseInt(av == null ? "999" : av); } catch (Exception ignore) {}
					boolean pass = v <= max;
					evidences.add(new EvidenceDraft(q.getId(), questionCode + " 값이 " + max + " 이하 (현재: " + (av == null ? "미응답" : av) + ")"));
					return new EvalResult(pass, evidences);
				}

				if (node.has("min")) {
					int min = node.get("min").asInt();
					int v = -999;
					try { v = Integer.parseInt(av == null ? "-999" : av); } catch (Exception ignore) {}
					boolean pass = v >= min;
					evidences.add(new EvidenceDraft(q.getId(), questionCode + " 값이 " + min + " 이상 (현재: " + (av == null ? "미응답" : av) + ")"));
					return new EvalResult(pass, evidences);
				}
			}

		} catch (Exception e) {
			return new EvalResult(false, evidences);
		}

		return new EvalResult(false, evidences);
	}

	public List<RunRecommendationItem> getResultItems(long runId, long userId) {
		ChecklistRun run = checklistMapper.getRunByIdAndUserId(runId, userId);
		if (run == null) return new ArrayList<>();

		List<RunRecommendationItem> items = checklistMapper.getRunRecommendationItems(runId);
		List<RunRecommendationEvidence> evidences = checklistMapper.getRunRecommendationEvidences(runId);

		Map<Long, RunRecommendationItem> map = new HashMap<>();
		for (RunRecommendationItem item : items) {
			map.put(item.getRunRecommendationId(), item);
		}
		for (RunRecommendationEvidence e : evidences) {
			RunRecommendationItem item = map.get(e.getRunRecommendationId());
			if (item != null) item.getEvidences().add(e);
		}

		return items;
	}

	public static class SaveResult {
		public final boolean ok;
		public final String msg;
		public final int totalScore;
		public final boolean submitted;

		public SaveResult(boolean ok, String msg, int totalScore, boolean submitted) {
			this.ok = ok;
			this.msg = msg;
			this.totalScore = totalScore;
			this.submitted = submitted;
		}
	}
}
