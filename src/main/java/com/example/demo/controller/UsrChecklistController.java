package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.service.ChecklistService;
import com.example.demo.vo.Checklist;
import com.example.demo.vo.ChecklistAnswer;
import com.example.demo.vo.ChecklistQuestion;
import com.example.demo.vo.ChecklistRun;
import com.example.demo.vo.RunRecommendationDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UsrChecklistController {

	private final ChecklistService checklistService;

	@GetMapping("/usr/checklist/start")
	public String showStart(HttpSession session, Model model) {
		List<Checklist> checklists = checklistService.getChecklists();
		Object selectedChildId = session.getAttribute("selectedChildId");

		model.addAttribute("checklists", checklists);
		model.addAttribute("selectedChildId", selectedChildId);

		return "usr/checklist/start";
	}

	@PostMapping("/usr/checklist/doStart")
	public String doStart(@RequestParam("checklistId") long checklistId, HttpSession session) {
		Long userId = (Long) session.getAttribute("loginedUserId");
		Long childId = (Long) session.getAttribute("selectedChildId");

		if (userId == null || childId == null) {
			return "redirect:/usr/child/list";
		}

		long runId = checklistService.createRun(checklistId, childId, userId);
		return "redirect:/usr/checklist/run?runId=" + runId;
	}

	@GetMapping("/usr/checklist/run")
	public String showRun(@RequestParam("runId") long runId, HttpSession session, Model model) {
		Long userId = (Long) session.getAttribute("loginedUserId");
		if (userId == null) return "redirect:/usr/member/login";

		ChecklistRun run = checklistService.getRun(runId);
		if (run == null || run.getUserId() != userId) {
			return "redirect:/usr/checklist/start";
		}

		List<ChecklistQuestion> questions = checklistService.getQuestionsByChecklistId(run.getChecklistId());
		Map<Long, ChecklistAnswer> answersMap = checklistService.getAnswersMapByRunId(runId);

		model.addAttribute("run", run);
		model.addAttribute("questions", questions);
		model.addAttribute("answersMap", answersMap);

		return "usr/checklist/run";
	}

	@PostMapping("/usr/checklist/doSubmit")
	public String doSubmit(@RequestParam("runId") long runId, HttpSession session, HttpServletRequest req) {
		Long userId = (Long) session.getAttribute("loginedUserId");
		if (userId == null) return "redirect:/usr/member/login";

		ChecklistRun run = checklistService.getRun(runId);
		if (run == null || run.getUserId() != userId) {
			return "redirect:/usr/checklist/start";
		}

		List<ChecklistQuestion> questions = checklistService.getQuestionsByChecklistId(run.getChecklistId());

		// 1) 답변 저장 + 점수 합산
		int totalScore = checklistService.saveAnswersAndCalcTotalScore(runId, questions, req);

		// 2) run 제출 처리
		checklistService.submitRun(runId, totalScore);

		// 3) 추천 + 근거 생성 후 DB 저장
		checklistService.generateAndSaveRecommendations(runId, questions);

		return "redirect:/usr/checklist/result?runId=" + runId;
	}

	@GetMapping("/usr/checklist/result")
	public String showResult(@RequestParam("runId") long runId, HttpSession session, Model model) {
		Long userId = (Long) session.getAttribute("loginedUserId");
		if (userId == null) return "redirect:/usr/member/login";

		ChecklistRun run = checklistService.getRun(runId);
		if (run == null || run.getUserId() != userId) {
			return "redirect:/usr/checklist/start";
		}

		List<ChecklistQuestion> questions = checklistService.getQuestionsByChecklistId(run.getChecklistId());
		Map<Long, ChecklistAnswer> answersMap = checklistService.getAnswersMapByRunId(runId);

		// 저장된 추천/근거
		List<RunRecommendationDto> runRecs = checklistService.getRunRecommendationsWithEvidence(runId);

		model.addAttribute("run", run);
		model.addAttribute("questions", questions);
		model.addAttribute("answersMap", answersMap);
		model.addAttribute("runRecs", runRecs);

		return "usr/checklist/result";
	}

	@GetMapping("/usr/checklist/history")
	public String showHistory(HttpSession session, Model model) {
		Long userId = (Long) session.getAttribute("loginedUserId");
		Long childId = (Long) session.getAttribute("selectedChildId");

		if (userId == null) return "redirect:/usr/member/login";
		if (childId == null) return "redirect:/usr/child/list";

		List<ChecklistRun> runs = checklistService.getSubmittedRunsByUserAndChild(userId, childId);
		model.addAttribute("runs", runs);

		return "usr/checklist/history";
	}
}
