package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.constant.SessionConst;
import com.example.demo.service.ChecklistResultService;
import com.example.demo.service.ChecklistService;
import com.example.demo.service.FavoriteService;
import com.example.demo.vo.Center;
import com.example.demo.vo.ChecklistDomain;
import com.example.demo.vo.DomainStat;
import com.example.demo.vo.TherapyTypeCode;

import jakarta.servlet.http.HttpSession;

@Controller
public class UsrChecklistController {

	private final ChecklistService       checklistService;
	private final ChecklistResultService checklistResultService;
	private final FavoriteService        favoriteService;

	public UsrChecklistController(ChecklistService checklistService,
			ChecklistResultService checklistResultService,
			FavoriteService favoriteService) {
		this.checklistService       = checklistService;
		this.checklistResultService = checklistResultService;
		this.favoriteService        = favoriteService;
	}

	@RequestMapping("/usr/checklist/result")
	public String showResult(@RequestParam("runId") long runId, HttpSession session, Model model) {

		Long userId = toLong(session.getAttribute(SessionConst.LOGINED_USER_ID));
		if (userId == null || userId <= 0) {
			return "redirect:/usr/member/login";
		}

		if (!checklistResultService.isOwned(runId, userId)) {
			model.addAttribute("msg", "접근 권한이 없습니다.");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		if (!checklistResultService.isSubmitted(runId)) {
			model.addAttribute("msg", "아직 제출되지 않은 체크리스트입니다. 제출 후 결과를 확인해 주세요.");
			model.addAttribute("redirectUrl", "/usr/checklist/start");
			return "usr/common/js";
		}

		// run 상세 조회 — DB에 해당 run이 없으면 null 반환 → 안내 페이지로
		Map<String, Object> runInfo = checklistService.getRunInfoForResult(userId, runId);
		if (runInfo == null) {
			model.addAttribute("msg", "결과 정보를 찾을 수 없습니다. 올바른 주소로 접근했는지 확인해 주세요.");
			model.addAttribute("historyBack", true);
			return "usr/common/js";
		}

		List<DomainStat> domainStats = checklistService.getDomainStatsByRunId(runId);
		List<String> recommendedDomains = checklistService.pickRecommendedDomains(domainStats);
		List<Center> centers = checklistService.getRecommendedCentersByDomains(recommendedDomains);

		Map<String, String> domainLabelMap = ChecklistDomain.getLabelMap();
		String riskLevel = checklistService.calculateRiskLevel(domainStats);
		String recommendationSummary = checklistService.getRecommendationSummary(
				recommendedDomains, domainLabelMap);
		Set<Long> favoriteCenterIds = favoriteService.getFavoriteCenterIds(userId);

		model.addAttribute("runInfo", runInfo);
		model.addAttribute("domainStats", domainStats);
		model.addAttribute("recommendedDomains", recommendedDomains);
		model.addAttribute("centers", centers);
		model.addAttribute("runId", runId);
		model.addAttribute("domainLabelMap", domainLabelMap);
		model.addAttribute("therapyTypeLabelMap", TherapyTypeCode.getLabelMap());
		model.addAttribute("riskLevel", riskLevel);
		model.addAttribute("recommendationSummary", recommendationSummary);
		model.addAttribute("favoriteCenterIds", favoriteCenterIds);

		return "usr/checklist/result";
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
