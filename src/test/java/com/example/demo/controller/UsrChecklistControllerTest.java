package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import com.example.demo.config.GlobalExceptionHandler;
import com.example.demo.service.ChecklistResultService;
import com.example.demo.service.ChecklistService;
import com.example.demo.service.ConsultationService;
import com.example.demo.service.FavoriteService;

/**
 * UsrChecklistController 스모크 테스트.
 *
 * <p>standaloneSetup 방식을 사용하므로 Spring Context, DB 불필요.
 * 인터셉터 없이 컨트롤러 + GlobalExceptionHandler 조합만 테스트한다.
 *
 * <p>검증 목적:
 * - 어떤 시나리오에서도 500이 나지 않음을 확인
 * - 잘못된 요청은 안내 뷰(usr/common/js) 또는 redirect로 떨어짐을 확인
 */
@ExtendWith(MockitoExtension.class)
class UsrChecklistControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChecklistService checklistService;

    @Mock
    private ChecklistResultService checklistResultService;

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private ConsultationService consultationService;

    @BeforeEach
    void setUp() {
        UsrChecklistController controller =
                new UsrChecklistController(checklistService, checklistResultService,
                        favoriteService, consultationService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ─────────────────────────────────────────────────
    // 시나리오 1: runId 파라미터 누락
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("runId 없이 접근하면 500이 아닌 안내 뷰 반환")
    void showResult_missingRunId_handledByAdvice_not500() throws Exception {
        mockMvc.perform(get("/usr/checklist/result"))
               .andExpect(status().isOk())
               .andExpect(view().name("usr/common/js"))
               .andExpect(model().attributeExists("msg"));
    }

    // ─────────────────────────────────────────────────
    // 시나리오 2: 세션 없음 (비로그인)
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("세션 없으면 로그인 페이지로 redirect")
    void showResult_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/usr/checklist/result").param("runId", "1"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/usr/member/login"));
    }

    // ─────────────────────────────────────────────────
    // 시나리오 3: 소유권 없음
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("소유권 없는 runId 접근 → 안내 뷰, msg 포함")
    void showResult_notOwned_showsErrorView() throws Exception {
        when(checklistResultService.isOwned(1L, 42L)).thenReturn(false);

        mockMvc.perform(get("/usr/checklist/result")
                .param("runId", "1")
                .sessionAttr("loginedUserId", 42L))
               .andExpect(status().isOk())
               .andExpect(view().name("usr/common/js"))
               .andExpect(model().attributeExists("msg"));
    }

    // ─────────────────────────────────────────────────
    // 시나리오 4: 아직 제출 안 된 run
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("SUBMITTED 아닌 run → 안내 뷰")
    void showResult_notSubmitted_showsGuideView() throws Exception {
        when(checklistResultService.isOwned(1L, 42L)).thenReturn(true);
        when(checklistResultService.isSubmitted(1L)).thenReturn(false);

        mockMvc.perform(get("/usr/checklist/result")
                .param("runId", "1")
                .sessionAttr("loginedUserId", 42L))
               .andExpect(status().isOk())
               .andExpect(view().name("usr/common/js"))
               .andExpect(model().attributeExists("msg"));
    }

    // ─────────────────────────────────────────────────
    // 시나리오 5: runInfo null (DB에 해당 데이터 없음) → 500 방지 핵심 케이스
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("runInfo null 반환 시에도 500이 아닌 안내 뷰 반환")
    void showResult_runInfoNull_showsErrorView_not500() throws Exception {
        when(checklistResultService.isOwned(1L, 42L)).thenReturn(true);
        when(checklistResultService.isSubmitted(1L)).thenReturn(true);
        when(checklistService.getRunInfoForResult(42L, 1L)).thenReturn(null);

        mockMvc.perform(get("/usr/checklist/result")
                .param("runId", "1")
                .sessionAttr("loginedUserId", 42L))
               .andExpect(status().isOk())
               .andExpect(view().name("usr/common/js"))
               .andExpect(model().attributeExists("msg"));
    }

    // ─────────────────────────────────────────────────
    // 시나리오 6: 정상 흐름
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("정상 흐름 — 결과 뷰 반환 + riskLevel/recommendationSummary 모델 포함")
    void showResult_normalFlow_showsResultView() throws Exception {
        when(checklistResultService.isOwned(1L, 42L)).thenReturn(true);
        when(checklistResultService.isSubmitted(1L)).thenReturn(true);
        when(checklistService.getRunInfoForResult(42L, 1L))
                .thenReturn(Map.of("runId", 1L, "childName", "테스트아이"));
        when(checklistService.getDomainStatsByRunId(1L)).thenReturn(List.of());
        when(checklistService.pickRecommendedDomains(any())).thenReturn(List.of());
        when(checklistService.getRecommendedCentersByDomains(any())).thenReturn(List.of());
        when(checklistService.calculateRiskLevel(any())).thenReturn("LOW");
        when(checklistService.getRecommendationSummary(any(), any()))
                .thenReturn("모든 영역에서 양호한 수행을 보이고 있습니다.");
        when(favoriteService.getFavoriteCenterIds(42L)).thenReturn(Set.of());

        mockMvc.perform(get("/usr/checklist/result")
                .param("runId", "1")
                .sessionAttr("loginedUserId", 42L))
               .andExpect(status().isOk())
               .andExpect(view().name("usr/checklist/result"))
               .andExpect(model().attributeExists(
                       "runInfo", "domainStats", "runId",
                       "riskLevel", "recommendationSummary",
                       "favoriteCenterIds", "consultationPrep"));
    }

    // ─────────────────────────────────────────────────
    // 시나리오 7: 서비스 내부 예외 → GlobalExceptionHandler 처리
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("서비스에서 RuntimeException 발생 시 GlobalExceptionHandler가 안내 뷰 반환")
    void showResult_serviceThrows_handledByAdvice() throws Exception {
        when(checklistResultService.isOwned(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("DB 연결 오류"));

        mockMvc.perform(get("/usr/checklist/result")
                .param("runId", "1")
                .sessionAttr("loginedUserId", 42L))
               .andExpect(status().isOk())
               .andExpect(view().name("usr/common/js"))
               .andExpect(model().attributeExists("msg"));
    }
}
