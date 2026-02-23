package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.service.ChecklistService;
import com.example.demo.vo.Center;
import com.example.demo.vo.DomainStat;

import jakarta.servlet.http.HttpSession;

@Controller
public class UsrChecklistController {

	private final ChecklistService checklistService;

	public UsrChecklistController(ChecklistService checklistService) {
		this.checklistService = checklistService;
	}

	@RequestMapping("/usr/checklist/result")
	public String showResult(@RequestParam("runId") int runId, HttpSession session, Model model) {
		Object loginedUserIdObj = session.getAttribute("loginedUserId");
		if (loginedUserIdObj == null) {
			return "redirect:/usr/member/login";
		}
		int loginedUserId = (int) loginedUserIdObj;

		Map<String, Object> runInfo = checklistService.getRunInfoForResult(loginedUserId, runId);
		if (runInfo == null) {
			model.addAttribute("msg", "런 정보가 없거나 접근 권한이 없습니다.");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		List<DomainStat> domainStats = checklistService.getDomainStatsByRunId(runId);
		List<String> recommendedDomains = checklistService.pickRecommendedDomains(domainStats);
		List<Center> centers = checklistService.getRecommendedCentersByDomains(recommendedDomains);

		model.addAttribute("runInfo", runInfo);
		model.addAttribute("domainStats", domainStats);
		model.addAttribute("recommendedDomains", recommendedDomains);
		model.addAttribute("centers", centers);

		return "usr/checklist/result";
	}
}