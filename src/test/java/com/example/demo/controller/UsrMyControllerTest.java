package com.example.demo.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.demo.config.GlobalExceptionHandler;
import com.example.demo.service.ChildService;
import com.example.demo.service.MyPageService;
import com.example.demo.vo.Child;

/**
 * UsrMyController 스모크 테스트.
 *
 * <p>standaloneSetup 방식 — Spring Context, DB 불필요.
 * 인터셉터 없이 컨트롤러 + GlobalExceptionHandler 조합만 검증한다.
 *
 * <p>검증 목적:
 * <ul>
 *   <li>세션 없으면 로그인 redirect</li>
 *   <li>정상 흐름 → usr/my/my 뷰 + 5개 모델 속성</li>
 *   <li>서비스 예외 → GlobalExceptionHandler가 안내 뷰 반환 (500 없음)</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class UsrMyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MyPageService myPageService;

    @Mock
    private ChildService childService;

    @BeforeEach
    void setUp() {
        UsrMyController controller = new UsrMyController(myPageService, childService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ─────────────────────────────────────────────────
    // 시나리오 1: 세션 없음 (비로그인)
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("세션 없으면 로그인 페이지로 redirect")
    void myPage_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/usr/my"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/usr/member/login"));
    }

    // ─────────────────────────────────────────────────
    // 시나리오 2: 정상 흐름
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("정상 흐름 — my 뷰 반환 + 5개 모델 속성 포함")
    void myPage_normalFlow_showsMyView() throws Exception {
        Child child = new Child();
        child.setId(1L);
        child.setName("테스트아이");

        when(childService.getChildrenByUserId(42L)).thenReturn(List.of(child));
        when(myPageService.getRecentSubmittedRuns(42L, 0L)).thenReturn(List.of());
        when(myPageService.getDraftRuns(42L, 0L)).thenReturn(List.of());
        when(myPageService.getFavoriteCenters(42L)).thenReturn(List.of());

        mockMvc.perform(get("/usr/my")
                .sessionAttr("loginedUserId", 42L))
               .andExpect(status().isOk())
               .andExpect(view().name("usr/my/my"))
               .andExpect(model().attributeExists(
                       "children", "selectedChildId",
                       "submittedRuns", "draftRuns", "favoriteCenters"));
    }

    // ─────────────────────────────────────────────────
    // 시나리오 3: 서비스 예외 → GlobalExceptionHandler 처리
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("서비스 RuntimeException → GlobalExceptionHandler가 안내 뷰 반환")
    void myPage_serviceThrows_handledByAdvice() throws Exception {
        when(childService.getChildrenByUserId(42L))
                .thenThrow(new RuntimeException("DB 연결 오류"));

        mockMvc.perform(get("/usr/my")
                .sessionAttr("loginedUserId", 42L))
               .andExpect(status().isOk())
               .andExpect(view().name("usr/common/js"))
               .andExpect(model().attributeExists("msg"));
    }
}
