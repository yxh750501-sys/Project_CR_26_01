package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.service.ChecklistStartService;
import com.example.demo.vo.AnswerForStart;
import com.example.demo.vo.ChecklistDomain;
import com.example.demo.vo.ChecklistForStart;
import com.example.demo.vo.ChecklistQuestionForStart;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class UsrChecklistStartController {

	private final ChecklistStartService checklistStartService;

	public UsrChecklistStartController(ChecklistStartService checklistStartService) {
		this.checklistStartService = checklistStartService;
	}

	// ────────────────────────────────────────────────────────────
	// GET /usr/checklist/start
	// runIdParam 있음: 해당 run 이어하기 (SUBMITTED이면 결과 화면 안내)
	// runIdParam 없음: DRAFT 재사용 or 새 DRAFT 생성
	// ────────────────────────────────────────────────────────────
	@RequestMapping("/usr/checklist/start")
	public String start(
			@RequestParam(value = "checklistId", required = false) Long checklistIdParam,
			@RequestParam(value = "childId",     required = false) Long childIdParam,
			@RequestParam(value = "runId",       required = false) Long runIdParam,
			@RequestParam(value = "saved",       required = false) String saved,
			HttpSession session, Model model) {

		Long userId = toLong(session.getAttribute("loginedUserId"));
		if (userId == null || userId <= 0) {
			return "redirect:/usr/member/login";
		}

		long checklistId = (checklistIdParam == null || checklistIdParam <= 0) ? 1L : checklistIdParam;

		Long childId = checklistStartService.resolveChildId(session, userId, childIdParam);
		if (childId == null || childId <= 0) {
			model.addAttribute("msg", "아이 프로필이 없습니다. 아이를 등록하고 대표 아이를 선택해 주세요.");
			model.addAttribute("redirectUrl", "/usr/child/list");
			return "usr/common/js";
		}
		session.setAttribute("selectedChildId", childId);

		ChecklistForStart checklist = checklistStartService.getChecklist(checklistId);
		if (checklist == null) {
			model.addAttribute("msg", "체크리스트가 존재하지 않습니다. (checklistId=" + checklistId + ")");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		List<ChecklistQuestionForStart> questions = checklistStartService.getQuestions(checklistId);
		if (questions == null || questions.isEmpty()) {
			model.addAttribute("msg", "체크리스트 문항이 없습니다. (checklistId=" + checklistId + ")");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		long runId;
		if (runIdParam != null && runIdParam > 0) {
			// 소유권 확인
			if (!checklistStartService.isRunOwnedByUser(runIdParam, userId)) {
				model.addAttribute("msg", "접근 권한이 없습니다.");
				model.addAttribute("historyBack", true);
				return "usr/common/js";
			}
			// SUBMITTED → 수정 불가, 결과 화면으로 안내
			String status = checklistStartService.getRunStatus(runIdParam, userId);
			if ("SUBMITTED".equalsIgnoreCase(status)) {
				model.addAttribute("msg", "이미 제출된 체크리스트입니다. 결과 화면으로 이동합니다.");
				model.addAttribute("redirectUrl", "/usr/checklist/result-centers?runId=" + runIdParam);
				return "usr/common/js";
			}
			runId = runIdParam;
		} else {
			// DRAFT 재사용 or 신규 생성
			runId = checklistStartService.getOrCreateDraftRun(userId, childId, checklistId);
		}

		Map<String, AnswerForStart> answersMap = checklistStartService.getAnswersMap(runId);

		model.addAttribute("checklist", checklist);
		model.addAttribute("questions", questions);
		model.addAttribute("runId", runId);
		model.addAttribute("childId", childId);
		model.addAttribute("answersMap", answersMap);
		model.addAttribute("domainLabelMap", ChecklistDomain.getLabelMap());
		if ("1".equals(saved)) {
			model.addAttribute("savedSuccess", true);
		}

		return "usr/checklist/start";
	}

	// ────────────────────────────────────────────────────────────
	// POST /usr/checklist/doSave  (임시저장)
	// ────────────────────────────────────────────────────────────
	@PostMapping("/usr/checklist/doSave")
	public String doSave(
			@RequestParam("runId")       long runId,
			@RequestParam("checklistId") long checklistId,
			HttpServletRequest req, HttpSession session, Model model) {

		Long userId = toLong(session.getAttribute("loginedUserId"));
		if (userId == null || userId <= 0) {
			return "redirect:/usr/member/login";
		}

		// 소유권
		if (!checklistStartService.isRunOwnedByUser(runId, userId)) {
			model.addAttribute("msg", "접근 권한이 없습니다.");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		// 이미 제출됨 → 수정 불가
		String status = checklistStartService.getRunStatus(runId, userId);
		if ("SUBMITTED".equalsIgnoreCase(status)) {
			model.addAttribute("msg", "이미 제출된 체크리스트는 수정할 수 없습니다.");
			model.addAttribute("redirectUrl", "/usr/checklist/result-centers?runId=" + runId);
			return "usr/common/js";
		}

		// form checklistId 위변조 방어
		if (!checklistStartService.isChecklistIdMatchingRun(runId, checklistId)) {
			model.addAttribute("msg", "잘못된 요청입니다.");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		List<ChecklistQuestionForStart> questions = checklistStartService.getQuestions(checklistId);
		if (questions == null || questions.isEmpty()) {
			model.addAttribute("msg", "문항을 불러오지 못했습니다.");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		for (ChecklistQuestionForStart q : questions) {
			processAndSaveAnswer(req, q, runId, false);
		}

		return "redirect:/usr/checklist/start?runId=" + runId + "&saved=1";
	}

	// ────────────────────────────────────────────────────────────
	// POST /usr/checklist/doSubmit  (최종 제출)
	// ────────────────────────────────────────────────────────────
	@PostMapping("/usr/checklist/doSubmit")
	public String doSubmit(
			@RequestParam("runId")       long runId,
			@RequestParam("checklistId") long checklistId,
			HttpServletRequest req, HttpSession session, Model model) {

		Long userId = toLong(session.getAttribute("loginedUserId"));
		if (userId == null || userId <= 0) {
			return "redirect:/usr/member/login";
		}

		// 소유권
		if (!checklistStartService.isRunOwnedByUser(runId, userId)) {
			model.addAttribute("msg", "접근 권한이 없습니다.");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		// 이미 제출됨 → 결과 화면으로
		String status = checklistStartService.getRunStatus(runId, userId);
		if ("SUBMITTED".equalsIgnoreCase(status)) {
			return "redirect:/usr/checklist/result-centers?runId=" + runId;
		}

		// form checklistId 위변조 방어
		if (!checklistStartService.isChecklistIdMatchingRun(runId, checklistId)) {
			model.addAttribute("msg", "잘못된 요청입니다.");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		List<ChecklistQuestionForStart> questions = checklistStartService.getQuestions(checklistId);
		if (questions == null || questions.isEmpty()) {
			model.addAttribute("msg", "문항을 불러오지 못했습니다.");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		int totalScore = 0;
		for (ChecklistQuestionForStart q : questions) {
			totalScore += processAndSaveAnswer(req, q, runId, true);
		}

		// affected == 0이면 이미 SUBMITTED됨 (동시 요청 등) → 그냥 결과로 이동
		checklistStartService.submitRun(runId, totalScore);

		return "redirect:/usr/checklist/result-centers?runId=" + runId;
	}

	// ────────────────────────────────────────────────────────────
	// 내부 헬퍼
	// ────────────────────────────────────────────────────────────

	/**
	 * 단일 문항의 답변을 파싱하고 저장한다.
	 * @param calcScore true이면 가중치 점수를 반환(doSubmit 용), false이면 0 반환(doSave 용)
	 * @return 이 문항의 가중치 점수
	 */
	private int processAndSaveAnswer(HttpServletRequest req,
			ChecklistQuestionForStart q, long runId, boolean calcScore) {

		String v = req.getParameter("q_" + q.getId());
		String responseType = q.getResponseType() == null ? "" : q.getResponseType().toUpperCase();
		int weight = q.getWeight() <= 0 ? 1 : q.getWeight();

		String answerValue = null;
		String answerText  = null;
		int score = 0;

		switch (responseType) {
			case "TEXT":
				answerText = (v == null || v.isBlank()) ? null : v;
				score = 0;
				break;
			case "YN":
				answerValue = (v == null || v.isBlank()) ? null : v;
				score = "Y".equalsIgnoreCase(v) ? 1 : 0;
				break;
			default: // SCALE5
				answerValue = (v == null || v.isBlank()) ? null : v;
				try {
					if (v != null && !v.isBlank()) score = Integer.parseInt(v);
				} catch (NumberFormatException ignored) {
					score = 0;
				}
				break;
		}

		int weightedScore = score * weight;
		checklistStartService.saveAnswer(runId, q.getId(), answerValue, answerText, weightedScore);
		return calcScore ? weightedScore : 0;
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
