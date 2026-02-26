package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.demo.config.GlobalExceptionHandler;
import com.example.demo.form.JoinForm;
import com.example.demo.service.UserService;

/**
 * UsrMemberController 스모크 테스트.
 *
 * <p>standaloneSetup 방식 — Spring Context, DB 불필요.
 * 인터셉터 없이 컨트롤러 + GlobalExceptionHandler 조합만 테스트한다.
 *
 * <p>검증 목적:
 * - 정상 가입 → redirect
 * - 이메일 중복 → 폼 복귀 + 필드 오류
 * - 비밀번호 확인 불일치 → 폼 복귀 + 필드 오류
 * - loginId 중복 → 폼 복귀 + 필드 오류
 * - @Valid 필수값 누락 → 폼 복귀 + 필드 오류
 */
@ExtendWith(MockitoExtension.class)
class UsrMemberControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UsrMemberController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── GET /usr/member/join ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /join → 가입 폼 뷰 반환, joinForm 모델 포함")
    void showJoin_returnsJoinViewWithEmptyForm() throws Exception {
        mockMvc.perform(get("/usr/member/join"))
               .andExpect(status().isOk())
               .andExpect(view().name("usr/member/join"))
               .andExpect(model().attributeExists("joinForm"));
    }

    // ── 시나리오 1: 정상 가입 ─────────────────────────────────────────────

    @Test
    @DisplayName("정상 가입 → /usr/member/login?joined=1 redirect, join() 1회 호출")
    void doJoin_normalFlow_redirectsToLogin() throws Exception {
        when(userService.existsByLoginId("user01")).thenReturn(false);
        when(userService.existsByEmail("user01@test.com")).thenReturn(false);
        when(userService.join(any(JoinForm.class))).thenReturn(1L);

        mockMvc.perform(post("/usr/member/doJoin")
                .param("loginId",        "user01")
                .param("name",           "홍길동")
                .param("email",          "user01@test.com")
                .param("loginPw",        "password123")
                .param("loginPwConfirm", "password123")
                .param("role",           "GUARDIAN"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/usr/member/login?joined=1"));

        verify(userService).join(any(JoinForm.class));
    }

    // ── 시나리오 2: 이메일 중복 ──────────────────────────────────────────

    @Test
    @DisplayName("이메일 중복 → 가입 폼 복귀, email 필드 오류 포함, join() 미호출")
    void doJoin_duplicateEmail_returnsJoinFormWithEmailError() throws Exception {
        when(userService.existsByLoginId("user02")).thenReturn(false);
        when(userService.existsByEmail("dup@test.com")).thenReturn(true);

        mockMvc.perform(post("/usr/member/doJoin")
                .param("loginId",        "user02")
                .param("name",           "김중복")
                .param("email",          "dup@test.com")
                .param("loginPw",        "password123")
                .param("loginPwConfirm", "password123")
                .param("role",           "GUARDIAN"))
               .andExpect(status().isOk())
               .andExpect(view().name("usr/member/join"))
               .andExpect(model().attributeHasFieldErrors("joinForm", "email"));

        verify(userService, never()).join(any());
    }

    // ── 시나리오 3: 비밀번호 확인 불일치 ─────────────────────────────────

    @Test
    @DisplayName("비밀번호 확인 불일치 → 가입 폼 복귀, loginPwConfirm 필드 오류 포함, join() 미호출")
    void doJoin_passwordMismatch_returnsJoinFormWithPwConfirmError() throws Exception {
        // existsByLoginId/existsByEmail 은 stubbing 없이 default(false) 반환.
        // STRICT_STUBS 는 미사용 stub 이 있을 때만 실패하므로 문제없음.
        mockMvc.perform(post("/usr/member/doJoin")
                .param("loginId",        "user03")
                .param("name",           "이불일치")
                .param("email",          "user03@test.com")
                .param("loginPw",        "password123")
                .param("loginPwConfirm", "differentPw!")
                .param("role",           "GUARDIAN"))
               .andExpect(status().isOk())
               .andExpect(view().name("usr/member/join"))
               .andExpect(model().attributeHasFieldErrors("joinForm", "loginPwConfirm"));

        verify(userService, never()).join(any());
    }

    // ── 시나리오 4: loginId 중복 ─────────────────────────────────────────

    @Test
    @DisplayName("loginId 중복 → 가입 폼 복귀, loginId 필드 오류 포함, join() 미호출")
    void doJoin_duplicateLoginId_returnsJoinFormWithLoginIdError() throws Exception {
        when(userService.existsByLoginId("dupUser")).thenReturn(true);
        // existsByEmail 은 stubbing 없이 default(false) 반환

        mockMvc.perform(post("/usr/member/doJoin")
                .param("loginId",        "dupUser")
                .param("name",           "중복사용자")
                .param("email",          "nodup@test.com")
                .param("loginPw",        "password123")
                .param("loginPwConfirm", "password123")
                .param("role",           "GUARDIAN"))
               .andExpect(status().isOk())
               .andExpect(view().name("usr/member/join"))
               .andExpect(model().attributeHasFieldErrors("joinForm", "loginId"));

        verify(userService, never()).join(any());
    }

    // ── 시나리오 5: @Valid 필수값 누락 ───────────────────────────────────

    @Test
    @DisplayName("loginId 빈값 (@Valid 실패) → 가입 폼 복귀, loginId 필드 오류 포함, join() 미호출")
    void doJoin_blankLoginId_validationFails_returnsJoinForm() throws Exception {
        mockMvc.perform(post("/usr/member/doJoin")
                .param("loginId",        "")       // @NotBlank 위반
                .param("name",           "테스트")
                .param("email",          "test@test.com")
                .param("loginPw",        "password123")
                .param("loginPwConfirm", "password123"))
               .andExpect(status().isOk())
               .andExpect(view().name("usr/member/join"))
               .andExpect(model().attributeHasFieldErrors("joinForm", "loginId"));

        verify(userService, never()).join(any());
    }

    // ── 시나리오 6: 비밀번호 최대 길이 초과 (@Size max=64 위반) ─────────

    @Test
    @DisplayName("비밀번호 65자 입력 (@Size max=64 위반) → 가입 폼 복귀, loginPw 필드 오류 포함")
    void doJoin_passwordTooLong_validationFails_returnsJoinForm() throws Exception {
        String tooLong = "a".repeat(65);

        mockMvc.perform(post("/usr/member/doJoin")
                .param("loginId",        "user04")
                .param("name",           "길이초과")
                .param("email",          "user04@test.com")
                .param("loginPw",        tooLong)
                .param("loginPwConfirm", tooLong))
               .andExpect(status().isOk())
               .andExpect(view().name("usr/member/join"))
               .andExpect(model().attributeHasFieldErrors("joinForm", "loginPw"));

        verify(userService, never()).join(any());
    }
}
