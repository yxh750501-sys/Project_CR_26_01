package com.example.demo.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.ChecklistStartRepository;
import com.example.demo.vo.AnswerForStart;
import com.example.demo.vo.ChecklistForStart;
import com.example.demo.vo.ChecklistQuestionForStart;

import jakarta.servlet.http.HttpSession;

@Service
public class ChecklistStartService {

	private final ChecklistStartRepository checklistStartRepository;

	public ChecklistStartService(ChecklistStartRepository checklistStartRepository) {
		this.checklistStartRepository = checklistStartRepository;
	}

	public ChecklistForStart getChecklist(long checklistId) {
		return checklistStartRepository.getChecklistById(checklistId);
	}

	public List<ChecklistQuestionForStart> getQuestions(long checklistId) {
		return checklistStartRepository.getQuestionsByChecklistId(checklistId);
	}

	public Long resolveChildId(HttpSession session, long userId, Long childIdParam) {
		if (childIdParam != null && childIdParam > 0) {
			return childIdParam;
		}

		List<String> keys = Arrays.asList(
				"selectedChildId",
				"representativeChildId",
				"repChildId",
				"childId",
				"selectedChildProfileId",
				"loginedChildId"
		);

		for (String key : keys) {
			Long v = toLong(session.getAttribute(key));
			if (v != null && v > 0) {
				return v;
			}
		}

		return checklistStartRepository.getFirstChildIdByUserId(userId);
	}

	@Transactional
	public long getOrCreateDraftRun(long userId, long childId, long checklistId) {
		Long existing = checklistStartRepository.getLatestDraftRunId(userId, childId, checklistId);
		if (existing != null && existing > 0) {
			return existing;
		}
		checklistStartRepository.createRun(checklistId, childId, userId);
		return checklistStartRepository.getLastInsertId();
	}

	public boolean isRunOwnedByUser(long runId, long userId) {
		return checklistStartRepository.countRunOwnedByUser(runId, userId) > 0;
	}

	public String getRunStatus(long runId, long userId) {
		return checklistStartRepository.getRunStatusByIdAndUserId(runId, userId);
	}

	/**
	 * ★ 핵심: JSP에서 숫자 키 타입(Integer/Long) 불일치가 자주 나서
	 * 키를 String(questionId)로 통일해서 내려준다.
	 */
	public Map<String, AnswerForStart> getAnswersMap(long runId) {
		List<AnswerForStart> list = checklistStartRepository.getAnswersByRunId(runId);
		Map<String, AnswerForStart> map = new HashMap<>();
		for (AnswerForStart a : list) {
			map.put(String.valueOf(a.getQuestionId()), a);
		}
		return map;
	}

	@Transactional
	public void saveAnswer(long runId, long questionId, String answerValue, String answerText, Integer score) {
		checklistStartRepository.upsertAnswer(runId, questionId, answerValue, answerText, score);
	}

	@Transactional
	public void submitRun(long runId, int totalScore) {
		checklistStartRepository.submitRun(runId, totalScore);
	}

	private Long toLong(Object v) {
		if (v == null) return null;
		if (v instanceof Number) return ((Number) v).longValue();
		try {
			return Long.parseLong(v.toString());
		} catch (Exception e) {
			return null;
		}
	}
}