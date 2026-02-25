package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.service.ChecklistResultService;
import com.example.demo.vo.Center;
import com.example.demo.vo.ChecklistDomain;
import com.example.demo.vo.DomainStat;
import com.example.demo.vo.TherapyTypeCode;

import jakarta.servlet.http.HttpSession;

@Controller
public class UsrChecklistCentersController {

	private final ChecklistResultService checklistResultService;

	public UsrChecklistCentersController(ChecklistResultService checklistResultService) {
		this.checklistResultService = checklistResultService;
	}

	@RequestMapping("/usr/checklist/result-centers")
	public String show(@RequestParam("runId") long runId, HttpSession session, Model model) {

		Long userId = toLong(session.getAttribute("loginedUserId"));
		if (userId == null || userId <= 0) {
			return "redirect:/usr/member/login";
		}

		if (runId <= 0) {
			model.addAttribute("msg", "runId가 올바르지 않습니다.");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		if (!checklistResultService.isOwned(runId, userId)) {
			model.addAttribute("msg", "접근 권한이 없습니다.");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		if (!checklistResultService.isSubmitted(runId)) {
			model.addAttribute("msg", "아직 제출되지 않은 체크리스트입니다. 제출 후 결과를 확인해 주세요.");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		List<DomainStat> domainStats = checklistResultService.getDomainStats(runId);
		List<String> topDomains = checklistResultService.pickTopDomains(domainStats, 2);
		List<String> therapyTypeCodes = checklistResultService.getTherapyTypeCodesByDomains(topDomains);
		List<Center> centers = checklistResultService.getCentersByTherapyTypeCodes(therapyTypeCodes);

		model.addAttribute("runId", runId);
		model.addAttribute("domainStats", domainStats);
		model.addAttribute("topDomains", topDomains);
		model.addAttribute("therapyTypeCodes", therapyTypeCodes);
		model.addAttribute("centers", centers);
		model.addAttribute("domainLabelMap", ChecklistDomain.getLabelMap());
		model.addAttribute("therapyTypeLabelMap", TherapyTypeCode.getLabelMap());

		return "usr/checklist/resultCenters";
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
