package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.service.ChecklistService;
import com.example.demo.service.ChecklistService.SaveResult;
import com.example.demo.vo.Checklist;
import com.example.demo.vo.ChecklistAnswer;
import com.example.demo.vo.ChecklistQuestion;
import com.example.demo.vo.ChecklistRun;
import com.example.demo.vo.RunRecommendationItem;

@Controller
public class UsrChecklistController {

	private final ChecklistService checklistService;

	public UsrChecklistController(ChecklistService checklistService) {
		this.checklistService = checklistService;
	}

	private long getLoginedUserId(HttpSession session) {
		Object obj = session.getAttribute("loginedUserId");
		if (obj == null) return 0;
		return ((Number) obj).longValue();
	}

	private Long getSelectedChildId(HttpSession session) {
		Object obj = session.getAttribute("selectedChildId");
		if (obj == null) return null;
		return ((Number) obj).longValue();
	}

	@GetMapping("/usr/checklist/start")
	public String showStart(HttpSession session, Model model) {
		Long childId = getSelectedChildId(session);
		if (childId == null) {
			return "redirect:/usr/child/list?needSelect=1";
		}

		List<Checklist> checklists = checklistService.getChecklists();
		model.addAttribute("selectedChildId", childId);
		model.addAttribute("checklists", checklists);

		return "usr/checklist/start";
	}

	@PostMapping("/usr/checklist/doStart")
	public String doStart(HttpSession session,
			@RequestParam("checklistId") long checklistId,
			RedirectAttributes ra) {

		long userId = getLoginedUserId(session);
		Long childId = getSelectedChildId(session);
		if (childId == null) {
			return "redirect:/usr/child/list?needSelect=1";
		}

		Checklist c = checklistService.getChecklistById(checklistId);
		if (c == null) {
			ra.addFlashAttribute("msg", "체크리스트를 찾을 수 없습니다.");
			return "redirect:/usr/checklist/start";
		}

		long runId = checklistService.startRun(checklistId, childId, userId);
		ra.addFlashAttribute("msg", "체크리스트를 시작했습니다: " + c.getTitle());

		return "redirect:/usr/checklist/run?id=" + runId;
	}

	@GetMapping("/usr/checklist/run")
	public String showRun(HttpSession session, Model model,
			@RequestParam("id") long runId,
			RedirectAttributes ra) {

		long userId = getLoginedUserId(session);
		Long childId = getSelectedChildId(session);
		if (childId == null) {
			return "redirect:/usr/child/list?needSelect=1";
		}

		ChecklistRun run = checklistService.getRunByIdAndUserId(runId, userId);
		if (run == null) {
			ra.addFlashAttribute("msg", "실행 정보를 찾을 수 없습니다.");
			return "redirect:/usr/checklist/start";
		}

		Checklist checklist = checklistService.getChecklistById(run.getChecklistId());
		if (checklist == null) {
			ra.addFlashAttribute("msg", "체크리스트 정보를 찾을 수 없습니다.");
			return "redirect:/usr/checklist/start";
		}

		List<ChecklistQuestion> questions = checklistService.getQuestionsByChecklistId(run.getChecklistId());
		Map<Long, ChecklistAnswer> answerMap = checklistService.getAnswerMapByRunId(runId);

		model.addAttribute("selectedChildId", childId);
		model.addAttribute("run", run);
		model.addAttribute("checklist", checklist);
		model.addAttribute("questions", questions);
		model.addAttribute("answerMap", answerMap);

		return "usr/checklist/run";
	}

	@PostMapping("/usr/checklist/doPost")
	public String doPost(HttpSession session,
			@RequestParam("runId") long runId,
			@RequestParam("action") String action,
			RedirectAttributes ra,
			@RequestParam Map<String, String> params) {

		long userId = getLoginedUserId(session);

		ChecklistRun run = checklistService.getRunByIdAndUserId(runId, userId);
		if (run == null) {
			ra.addFlashAttribute("msg", "실행 정보를 찾을 수 없습니다.");
			return "redirect:/usr/checklist/start";
		}

		List<ChecklistQuestion> questions = checklistService.getQuestionsByChecklistId(run.getChecklistId());

		Map<Long, String> answerValueMap = new HashMap<>();
		Map<Long, String> answerTextMap = new HashMap<>();

		for (ChecklistQuestion q : questions) {
			long qid = q.getId();

			String v = params.get("answer_" + qid);
			String t = params.get("text_" + qid);

			answerValueMap.put(qid, v);
			answerTextMap.put(qid, t);
		}

		boolean submit = "submit".equalsIgnoreCase(action);

		SaveResult result = checklistService.saveOrSubmit(runId, userId, questions, answerValueMap, answerTextMap, submit);
		ra.addFlashAttribute("msg", result.msg);

		if (result.ok && result.submitted) {
			return "redirect:/usr/checklist/result?id=" + runId;
		}

		return "redirect:/usr/checklist/run?id=" + runId;
	}

	@GetMapping("/usr/checklist/result")
	public String showResult(HttpSession session, Model model,
			@RequestParam("id") long runId,
			RedirectAttributes ra) {

		long userId = getLoginedUserId(session);

		ChecklistRun run = checklistService.getRunByIdAndUserId(runId, userId);
		if (run == null) {
			ra.addFlashAttribute("msg", "결과 정보를 찾을 수 없습니다.");
			return "redirect:/usr/checklist/start";
		}

		Checklist checklist = checklistService.getChecklistById(run.getChecklistId());
		List<RunRecommendationItem> items = checklistService.getResultItems(runId, userId);

		model.addAttribute("run", run);
		model.addAttribute("checklist", checklist);
		model.addAttribute("items", items);

		return "usr/checklist/result";
	}
}
