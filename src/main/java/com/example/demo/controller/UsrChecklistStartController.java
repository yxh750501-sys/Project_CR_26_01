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

	@RequestMapping("/usr/checklist/start")
	public String start(
			@RequestParam(value = "checklistId", required = false) Long checklistIdParam,
			@RequestParam(value = "childId", required = false) Long childIdParam,
			@RequestParam(value = "runId", required = false) Long runIdParam,
			HttpSession session, Model model) {

		Long userId = toLong(session.getAttribute("loginedUserId"));
		if (userId == null || userId <= 0) {
			return "redirect:/usr/member/login";
		}

		long checklistId = (checklistIdParam == null || checklistIdParam <= 0) ? 1L : checklistIdParam;

		Long childId = checklistStartService.resolveChildId(session, userId, childIdParam);
		if (childId == null || childId <= 0) {
			model.addAttribute("msg", "아이 프로필이 없어서 체크리스트를 시작할 수 없습니다. 아이를 등록하고 대표아이를 선택해 주세요.");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		// ★ childId 고정(세션 키가 프로젝트마다 달라서 가장 무난한 키로 한 번 더 저장)
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
			if (!checklistStartService.isRunOwnedByUser(runIdParam, userId)) {
				model.addAttribute("msg", "접근 권한이 없습니다.");
				model.addAttribute("historyBack", true);
				return "usr/common/js";
			}
			String status = checklistStartService.getRunStatus(runIdParam, userId);
			if (status != null && "SUBMITTED".equalsIgnoreCase(status)) {
				model.addAttribute("msg", "이미 제출한 체크리스트는 수정할 수 없습니다. 새로 시작해 주세요.");
				model.addAttribute("historyBack", true);
				return "usr/common/js";
			}
			runId = runIdParam;
		} else {
			runId = checklistStartService.getOrCreateDraftRun(userId, childId, checklistId);
		}

		// ★ String 키 Map
		Map<String, AnswerForStart> answersMap = checklistStartService.getAnswersMap(runId);

		model.addAttribute("checklist", checklist);
		model.addAttribute("questions", questions);
		model.addAttribute("runId", runId);
		model.addAttribute("childId", childId);
		model.addAttribute("answersMap", answersMap);

		return "usr/checklist/start";
	}

	@PostMapping("/usr/checklist/doSave")
	public String doSave(@RequestParam("runId") long runId,
			@RequestParam("checklistId") long checklistId,
			HttpServletRequest req, HttpSession session, Model model) {

		Long userId = toLong(session.getAttribute("loginedUserId"));
		if (userId == null || userId <= 0) {
			return "redirect:/usr/member/login";
		}

		if (!checklistStartService.isRunOwnedByUser(runId, userId)) {
			model.addAttribute("msg", "접근 권한이 없습니다.");
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
			String v = req.getParameter("q_" + q.getId());

			String responseType = q.getResponseType() == null ? "" : q.getResponseType();
			int weight = q.getWeight() <= 0 ? 1 : q.getWeight();

			String answerValue = null;
			String answerText = null;
			Integer score = 0;

			if ("TEXT".equalsIgnoreCase(responseType)) {
				answerText = (v == null || v.isBlank()) ? null : v;
				score = 0;
			} else if ("YN".equalsIgnoreCase(responseType)) {
				answerValue = (v == null || v.isBlank()) ? null : v;
				if ("Y".equalsIgnoreCase(v)) score = 1;
				else score = 0;
			} else {
				answerValue = (v == null || v.isBlank()) ? null : v;
				int parsed = 0;
				try {
					if (v != null && !v.isBlank()) parsed = Integer.parseInt(v);
				} catch (Exception e) {
					parsed = 0;
				}
				score = parsed;
			}

			int weightedScore = (score == null ? 0 : score) * weight;
			checklistStartService.saveAnswer(runId, q.getId(), answerValue, answerText, weightedScore);
		}

		return "redirect:/usr/checklist/start?runId=" + runId + "&saved=1";
	}

	@PostMapping("/usr/checklist/doSubmit")
	public String doSubmit(@RequestParam("runId") long runId,
			@RequestParam("checklistId") long checklistId,
			HttpServletRequest req, HttpSession session, Model model) {

		Long userId = toLong(session.getAttribute("loginedUserId"));
		if (userId == null || userId <= 0) {
			return "redirect:/usr/member/login";
		}

		if (!checklistStartService.isRunOwnedByUser(runId, userId)) {
			model.addAttribute("msg", "접근 권한이 없습니다.");
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
			String v = req.getParameter("q_" + q.getId());

			String responseType = q.getResponseType() == null ? "" : q.getResponseType();
			int weight = q.getWeight() <= 0 ? 1 : q.getWeight();

			String answerValue = null;
			String answerText = null;
			Integer score = 0;

			if ("TEXT".equalsIgnoreCase(responseType)) {
				answerText = (v == null || v.isBlank()) ? null : v;
				score = 0;
			} else if ("YN".equalsIgnoreCase(responseType)) {
				answerValue = (v == null || v.isBlank()) ? null : v;
				if ("Y".equalsIgnoreCase(v)) score = 1;
				else score = 0;
			} else {
				answerValue = (v == null || v.isBlank()) ? null : v;
				int parsed = 0;
				try {
					if (v != null && !v.isBlank()) parsed = Integer.parseInt(v);
				} catch (Exception e) {
					parsed = 0;
				}
				score = parsed;
			}

			int weightedScore = (score == null ? 0 : score) * weight;
			checklistStartService.saveAnswer(runId, q.getId(), answerValue, answerText, weightedScore);
			totalScore += weightedScore;
		}

		checklistStartService.submitRun(runId, totalScore);
		return "redirect:/usr/checklist/result-centers?runId=" + runId;
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