package com.example.demo.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.ChecklistMapper;
import com.example.demo.vo.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChecklistService {

	private final ChecklistMapper checklistMapper;

	public List<Checklist> getChecklists() {
		return checklistMapper.getChecklists();
	}

	@Transactional
	public long createRun(long checklistId, long childId, long userId) {
		ChecklistRun run = new ChecklistRun();
		run.setChecklistId(checklistId);
		run.setChildId(childId);
		run.setUserId(userId);
		run.setStatus("DRAFT");
		run.setTotalScore(0);
		checklistMapper.insertRun(run);
		return run.getId();
	}

	public ChecklistRun getRun(long runId) {
		return checklistMapper.getRunById(runId);
	}

	public List<ChecklistQuestion> getQuestionsByChecklistId(long checklistId) {
		return checklistMapper.getQuestionsByChecklistId(checklistId);
	}

	public Map<Long, ChecklistAnswer> getAnswersMapByRunId(long runId) {
		List<ChecklistAnswer> answers = checklistMapper.getAnswersByRunId(runId);
		return answers.stream().collect(Collectors.toMap(ChecklistAnswer::getQuestionId, a -> a, (a, b) -> b));
	}

	@Transactional
	public int saveAnswersAndCalcTotalScore(long runId, List<ChecklistQuestion> questions, HttpServletRequest req) {
		int total = 0;

		for (ChecklistQuestion q : questions) {
			String rt = (q.getResponseType() == null) ? "" : q.getResponseType().trim().toUpperCase();
			boolean isText = "TEXT".equals(rt);

			String value = req.getParameter("v_" + q.getId());
			String text = req.getParameter("t_" + q.getId());

			if (value != null) value = value.trim();
			if (text != null) text = text.trim();

			Integer score = null;

			if (!isText) {
				if (value == null || value.isEmpty()) {
					score = 0;
				} else {
					int v = parseInt(value, 0);
					score = v * q.getWeight();
				}
				total += score;
			} else {
				score = 0;
			}

			checklistMapper.upsertAnswer(runId, q.getId(),
					(isText ? null : emptyToNull(value)),
					emptyToNull(text),
					score);
		}

		return total;
	}

	@Transactional
	public void submitRun(long runId, int totalScore) {
		checklistMapper.submitRun(runId, totalScore);
	}

	@Transactional
	public void generateAndSaveRecommendations(long runId, List<ChecklistQuestion> questions) {
		Map<Long, ChecklistAnswer> answersMap = getAnswersMapByRunId(runId);

		// code -> question
		Map<String, ChecklistQuestion> qByCode = new HashMap<>();
		for (ChecklistQuestion q : questions) {
			if (q.getCode() != null) qByCode.put(q.getCode().trim().toUpperCase(), q);
		}

		String envCode = qByCode.containsKey("Q23") ? "Q23" : (qByCode.containsKey("Q24") ? "Q24" : "");
		String modelCode = qByCode.containsKey("Q13") ? "Q13" : "";
		String accCode = qByCode.containsKey("Q19") ? "Q19" : "";
		String reinfCode = qByCode.containsKey("Q16") ? "Q16" : "";

		int envV = getAnswerValueInt(envCode, qByCode, answersMap);
		int modelV = getAnswerValueInt(modelCode, qByCode, answersMap);
		int accV = getAnswerValueInt(accCode, qByCode, answersMap);
		int reinfV = getAnswerValueInt(reinfCode, qByCode, answersMap);

		// 기존 저장 제거 후 새로 저장
		checklistMapper.deleteRunRecommendations(runId);

		// 추천 템플릿 id 로딩(코드 -> id)
		Map<String, Long> recoId = checklistMapper.getAllRecommendations().stream()
				.collect(Collectors.toMap(Recommendation::getCode, Recommendation::getId, (a, b) -> a));

		// 1) 환경/접근성
		if (!envCode.isEmpty()) {
			if (envV >= 1 && envV <= 2) {
				saveReco(runId, recoId, "RECO_ENV_SETUP",
						envCode + "=" + labelScale5(envV) + " → 보드가 바로 안 나오면 ‘기회 10번’을 만들 수 없어 학습이 멈춥니다.",
						qByCode.get(envCode).getId(),
						"근거: " + envCode + " 응답이 '" + labelScale5(envV) + "' 입니다.");
			} else if (envV == 3) {
				saveReco(runId, recoId, "RECO_ENV_OBSERVE",
						envCode + "=모름 → 최근 7일/10번 기회 관찰이 부족할 가능성이 큽니다.",
						qByCode.get(envCode).getId(),
						"근거: " + envCode + " 응답이 '모름' 입니다(기회 10번을 먼저 만들어 기록 필요).");
			}
		}

		// 2) 강화물 목록(Q16: 1/3/5)
		if (!reinfCode.isEmpty()) {
			if (reinfV == 1 || reinfV == 3) {
				saveReco(runId, recoId, "RECO_REINFORCER_LIST",
						reinfCode + "=" + labelQ16(reinfV) + " → 선택을 해도 즉시 제공할 강화가 없으면 AAC가 빠르게 무너집니다.",
						qByCode.get(reinfCode).getId(),
						"근거: " + reinfCode + " 응답이 '" + labelQ16(reinfV) + "' 입니다.");
			}
		}

		// 3) 정확도(Q19)
		if (!accCode.isEmpty()) {
			if (accV >= 1 && accV <= 2) {
				saveReco(runId, recoId, "RECO_TOUCH_ACCURACY",
						accCode + "=" + labelScale5(accV) + " → ‘첫 터치 성공률’이 낮으면 선택 학습이 불안정해집니다.",
						qByCode.get(accCode).getId(),
						"근거: " + accCode + " 응답이 '" + labelScale5(accV) + "' 입니다(2개 보기/칸 키우기 권장).");
			} else if (accV == 3) {
				saveReco(runId, recoId, "RECO_TOUCH_OBSERVE",
						accCode + "=모름 → 2개 보기로 10번만 시도해 첫 터치 성공 횟수를 기록하세요.",
						qByCode.get(accCode).getId(),
						"근거: " + accCode + " 응답이 '모름' 입니다(관찰 횟수 부족).");
			}
		}

		// 4) 모델링 수용(Q13)
		if (!modelCode.isEmpty()) {
			if (modelV >= 1 && modelV <= 2) {
				saveReco(runId, recoId, "RECO_MODELING_ACCEPT",
						modelCode + "=" + labelScale5(modelV) + " → 모델링은 짧게(1~2초) 하고 ‘아이 차례’를 바로 주는 방식으로 조정합니다.",
						qByCode.get(modelCode).getId(),
						"근거: " + modelCode + " 응답이 '" + labelScale5(modelV) + "' 입니다(거부 감소 전략 필요).");
			} else if (modelV == 3) {
				saveReco(runId, recoId, "RECO_MODELING_OBSERVE",
						modelCode + "=모름 → 짧은 모델링 10번을 시도하며 거부 행동 여부를 기록하세요.",
						qByCode.get(modelCode).getId(),
						"근거: " + modelCode + " 응답이 '모름' 입니다(기회 10번 관찰 필요).");
			}
		}
	}

	public List<RunRecommendationDto> getRunRecommendationsWithEvidence(long runId) {
		List<RunRecommendationDto> recs = checklistMapper.getRunRecommendations(runId);
		for (RunRecommendationDto r : recs) {
			r.setEvidences(checklistMapper.getEvidencesByRunRecommendationId(r.getId()));
		}
		return recs;
	}

	public List<ChecklistRun> getSubmittedRunsByUserAndChild(long userId, long childId) {
		return checklistMapper.getSubmittedRunsByUserAndChild(userId, childId);
	}

	// ======================
	// private helpers
	// ======================

	private void saveReco(long runId, Map<String, Long> recoId, String recoCode, String reasonText, Long questionId, String evidenceText) {
		Long recommendationId = recoId.get(recoCode);
		if (recommendationId == null) return;

		RunRecommendationParam param = new RunRecommendationParam();
		param.setRunId(runId);
		param.setRecommendationId(recommendationId);
		param.setReasonText(reasonText);

		checklistMapper.insertRunRecommendation(param); // id 채워짐
		long rrId = param.getId();

		checklistMapper.insertEvidence(rrId, questionId, evidenceText);
	}

	private int getAnswerValueInt(String code, Map<String, ChecklistQuestion> qByCode, Map<Long, ChecklistAnswer> answersMap) {
		if (code == null || code.isEmpty()) return 0;
		ChecklistQuestion q = qByCode.get(code);
		if (q == null) return 0;

		ChecklistAnswer a = answersMap.get(q.getId());
		if (a == null || a.getAnswerValue() == null || a.getAnswerValue().trim().isEmpty()) return 0;

		return parseInt(a.getAnswerValue(), 0);
	}

	private int parseInt(String s, int def) {
		try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
	}

	private String emptyToNull(String s) {
		if (s == null) return null;
		s = s.trim();
		return s.isEmpty() ? null : s;
	}

	private String labelScale5(int v) {
		if (v == 1) return "불가능";
		if (v == 2) return "어느정도 불가능";
		if (v == 3) return "모름";
		if (v == 4) return "어느정도 가능";
		if (v == 5) return "가능함";
		return "";
	}

	private String labelQ16(int v) {
		if (v == 1) return "없음";
		if (v == 3) return "모름";
		if (v == 5) return "있음";
		return "";
	}
}
