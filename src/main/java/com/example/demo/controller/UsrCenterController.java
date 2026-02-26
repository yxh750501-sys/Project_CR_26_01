package com.example.demo.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.constant.SessionConst;
import com.example.demo.repository.CenterRepository;
import com.example.demo.service.FavoriteService;
import com.example.demo.vo.Center;
import com.example.demo.vo.ChecklistDomain;
import com.example.demo.vo.TherapyTypeCode;

@Controller
@RequestMapping("/usr/center")
public class UsrCenterController {

    private static final int PAGE_SIZE = 12;

    private final CenterRepository  centerRepository;
    private final FavoriteService   favoriteService;

    public UsrCenterController(CenterRepository centerRepository,
                                FavoriteService favoriteService) {
        this.centerRepository = centerRepository;
        this.favoriteService  = favoriteService;
    }

    // ── 센터 목록 ─────────────────────────────────────────────

    /**
     * GET /usr/center/list
     * 키워드·지역·도메인 필터 + 페이지네이션.
     */
    @GetMapping("/list")
    public String list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String sido,
            @RequestParam(defaultValue = "") String domain,
            @RequestParam(defaultValue = "1") int page,
            HttpSession session, Model model) {

        if (page < 1) page = 1;

        int offset = (page - 1) * PAGE_SIZE;
        int total  = centerRepository.countCentersFiltered(keyword, sido, domain);
        int totalPages = (total == 0) ? 1 : (int) Math.ceil((double) total / PAGE_SIZE);
        if (page > totalPages) page = totalPages;

        List<Center> centers = centerRepository.searchCentersFiltered(
                keyword, sido, domain, offset, PAGE_SIZE);

        Long userId = toLong(session.getAttribute(SessionConst.LOGINED_USER_ID));
        Set<Long> favoriteIds = favoriteService.getFavoriteCenterIds(userId);

        model.addAttribute("centers",          centers);
        model.addAttribute("favoriteIds",       favoriteIds);
        model.addAttribute("keyword",           keyword);
        model.addAttribute("sido",              sido);
        model.addAttribute("domain",            domain);
        model.addAttribute("page",              page);
        model.addAttribute("totalPages",        totalPages);
        model.addAttribute("total",             total);
        model.addAttribute("domainLabelMap",    ChecklistDomain.getLabelMap());
        model.addAttribute("therapyTypeLabelMap", TherapyTypeCode.getLabelMap());
        return "usr/center/list";
    }

    // ── 즐겨찾기 토글 (AJAX) ─────────────────────────────────

    /**
     * POST /usr/center/doToggleFavorite
     * JSON {"favorited": true/false, "centerId": N} 반환.
     * NeedLoginInterceptor 가 /usr/** 를 커버하므로 비로그인 접근 불가.
     */
    @PostMapping("/doToggleFavorite")
    @ResponseBody
    public Map<String, Object> doToggleFavorite(
            @RequestParam long centerId,
            HttpSession session) {

        Map<String, Object> res = new HashMap<>();
        Long userId = toLong(session.getAttribute(SessionConst.LOGINED_USER_ID));
        if (userId == null || userId <= 0) {
            res.put("error", "로그인이 필요합니다.");
            return res;
        }
        boolean favorited = favoriteService.toggle(userId, centerId);
        res.put("favorited", favorited);
        res.put("centerId",  centerId);
        return res;
    }

    // ── 즐겨찾기 목록 ─────────────────────────────────────────

    /**
     * GET /usr/center/favorites
     * 로그인 사용자의 즐겨찾기 센터 전체 목록.
     */
    @GetMapping("/favorites")
    public String favorites(HttpSession session, Model model) {
        Long userId = toLong(session.getAttribute(SessionConst.LOGINED_USER_ID));
        List<Center> centers = favoriteService.getFavoriteCenters(userId);

        // 즐겨찾기 페이지에서는 모든 카드가 favorited 상태 → ID 집합 그대로
        Set<Long> favoriteIds = favoriteService.getFavoriteCenterIds(userId);

        model.addAttribute("centers",            centers);
        model.addAttribute("favoriteIds",         favoriteIds);
        model.addAttribute("domainLabelMap",      ChecklistDomain.getLabelMap());
        model.addAttribute("therapyTypeLabelMap", TherapyTypeCode.getLabelMap());
        return "usr/center/favorites";
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }
}
