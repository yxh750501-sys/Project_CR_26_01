package com.example.demo.interceptor;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.service.CenterRecommendationService;
import com.example.demo.service.RunRecommendationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BeforeActionInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(BeforeActionInterceptor.class);

    private final ObjectProvider<RunRecommendationService> runRecommendationServiceProvider;
    private final ObjectProvider<CenterRecommendationService> centerRecommendationServiceProvider;

    // ──────────────────────────────────────────────────────────────────────
    // preHandle: 로그인 여부를 request attribute로 노출 (JSP용)
    // ──────────────────────────────────────────────────────────────────────
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler)
            throws Exception {
        HttpSession session = req.getSession();

        boolean isLogined = session.getAttribute("loginedUserId") != null;
        req.setAttribute("isLogined", isLogined);

        Object role = session.getAttribute("loginedUserRole");
        req.setAttribute("loginedUserRole", role);

        return true;
    }

    // ──────────────────────────────────────────────────────────────────────
    // postHandle: 결과 페이지에서만 추천/센터 데이터를 model에 주입한다.
    //
    // ⚠️ 중요: postHandle에서 발생한 예외는 @ControllerAdvice 가 처리하지 않는다.
    //            따라서 모든 로직을 try-catch 로 감싸 추천 데이터 실패가
    //            결과 페이지 500으로 이어지지 않도록 방어한다.
    // ──────────────────────────────────────────────────────────────────────
    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse resp, Object handler,
                           ModelAndView mv) throws Exception {
        if (mv == null) return;

        HttpSession session = req.getSession(false);
        if (session == null) return;

        long userId = toL(session.getAttribute("loginedUserId"), 0);
        if (userId <= 0) return;

        String uri = req.getRequestURI();
        if (uri == null) uri = "";

        // 결과 페이지에서만 추천/센터 주입
        if (!uri.startsWith("/usr/checklist/result")) return;

        long runId = toL(req.getParameter("runId"), 0);
        if (runId <= 0) return;

        try {
            RunRecommendationService runSvc = runRecommendationServiceProvider.getIfAvailable();
            CenterRecommendationService centerSvc = centerRecommendationServiceProvider.getIfAvailable();

            // 빈이 없으면(스캔 실패 등) 패스 — 결과 페이지 기본 표시는 유지
            if (runSvc == null || centerSvc == null) return;

            // 추천 생성 + 조회
            runSvc.ensureGenerated(runId, userId);
            List<Map<String, Object>> runRecommendations =
                    runSvc.getRunRecommendationsWithEvidence(runId, userId);
            mv.addObject("runRecommendations", runRecommendations);

            // 지역/반경 필터(없으면 전국)
            String sido    = trimOrEmpty(req.getParameter("sido"));
            String sigungu = trimOrEmpty(req.getParameter("sigungu"));

            Double lat      = toD(req.getParameter("lat"));
            Double lng      = toD(req.getParameter("lng"));
            Double radiusKm = toD(req.getParameter("radiusKm"));

            if (lat != null && lng != null && radiusKm == null) radiusKm = 10.0;

            List<Map<String, Object>> therapyCenterBlocks =
                    centerSvc.buildTherapyBlocksFromRunRecommendations(
                            runRecommendations, sido, sigungu, lat, lng, radiusKm, 2, 7);

            mv.addObject("therapyCenterBlocks", therapyCenterBlocks);
            mv.addObject("filterSido",     sido);
            mv.addObject("filterSigungu",  sigungu);
            mv.addObject("filterLat",      lat);
            mv.addObject("filterLng",      lng);
            mv.addObject("filterRadiusKm", radiusKm);

        } catch (Exception e) {
            // 추천 데이터 로드 실패는 경고만 남기고, 결과 페이지는 정상 표시
            log.warn("추천 데이터 로드 실패 — runId={}, userId={}: {}",
                     runId, userId, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // 내부 유틸
    // ──────────────────────────────────────────────────────────────────────

    private long toL(Object v, long def) {
        try {
            if (v == null) return def;
            return Long.parseLong(String.valueOf(v).trim());
        } catch (Exception e) {
            return def;
        }
    }

    private Double toD(String v) {
        try {
            if (v == null) return null;
            String t = v.trim();
            if (t.isEmpty()) return null;
            return Double.parseDouble(t);
        } catch (Exception e) {
            return null;
        }
    }

    private String trimOrEmpty(String v) {
        return v == null ? "" : v.trim();
    }
}
