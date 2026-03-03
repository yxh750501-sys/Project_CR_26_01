package com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.constant.SessionConst;
import com.example.demo.form.JoinForm;
import com.example.demo.service.UserService;
import com.example.demo.vo.User;

@Controller
@RequestMapping("/usr/member")
public class UsrMemberController {

    private static final Logger log = LoggerFactory.getLogger(UsrMemberController.class);

    private final UserService userService;

    @Value("${google.oauth.client-id:}")
    private String googleClientId;

    public UsrMemberController(UserService userService) {
        this.userService = userService;
    }

    // ── 로그인 ───────────────────────────────────────────────────

    @GetMapping("/login")
    public String showLogin(Model model) {
        if (googleClientId != null && !googleClientId.isBlank()) {
            model.addAttribute("googleClientId", googleClientId);
        }
        return "usr/member/login";
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam("loginId") String loginId,
                          @RequestParam("loginPw") String loginPw,
                          HttpServletRequest req,
                          HttpSession session) {

        User user = userService.login(loginId, loginPw);

        if (user == null) {
            req.setAttribute("msg", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "usr/member/login";
        }

        session.setAttribute(SessionConst.LOGINED_USER_ID,   user.getId());
        session.setAttribute(SessionConst.LOGINED_USER_ROLE, user.getRole());

        // 로그인 성공 후 홈으로 이동
        return "redirect:/";
    }

    // ── Google 로그인 ─────────────────────────────────────────────

    /**
     * POST /usr/member/doGoogleLogin
     * Google Identity Services 가 프론트에서 발급한 id_token(credential) 을 받아
     * 서버에서 검증 후 세션을 생성한다.
     */
    @PostMapping("/doGoogleLogin")
    public String doGoogleLogin(@RequestParam("credential") String credential,
                                HttpSession session,
                                RedirectAttributes ra) {
        User user = userService.loginOrRegisterWithGoogle(credential);
        if (user == null) {
            ra.addFlashAttribute("googleError",
                    "Google 로그인에 실패했습니다. 잠시 후 다시 시도해 주세요.");
            return "redirect:/usr/member/login";
        }
        session.setAttribute(SessionConst.LOGINED_USER_ID,   user.getId());
        session.setAttribute(SessionConst.LOGINED_USER_ROLE, user.getRole());
        return "redirect:/";
    }

    // ── 회원가입 ─────────────────────────────────────────────────

    /**
     * GET /usr/member/join — 회원가입 폼 표시.
     * Spring form taglib 을 위해 빈 JoinForm 을 모델에 추가한다.
     */
    @GetMapping("/join")
    public String showJoin(Model model) {
        model.addAttribute("joinForm", new JoinForm());
        return "usr/member/join";
    }

    /**
     * POST /usr/member/doJoin — 회원가입 처리.
     *
     * <p>처리 순서:
     * <ol>
     *   <li>{@code @Valid} 어노테이션 검증 (NotBlank, Size, Email, Pattern)
     *   <li>비밀번호 확인 일치 검사
     *   <li>loginId / email 중복 검사 (SELECT 기반 사전 검사)
     *   <li>검증 오류가 있으면 회원가입 폼으로 돌아감 (입력값 유지)
     *   <li>회원 등록 (BCrypt 해싱)
     *   <li>DB UNIQUE 제약 위반 시(레이스 컨디션) {@link DataIntegrityViolationException} 을
     *       잡아 필드 에러로 변환 — 500 방지
     * </ol>
     */
    @PostMapping("/doJoin")
    public String doJoin(@Valid @ModelAttribute("joinForm") JoinForm form,
                         BindingResult bindingResult,
                         Model model) {

        // 1) 비밀번호 확인 일치 검사 (기본 검증 통과 후에만 비교)
        if (!bindingResult.hasFieldErrors("loginPw")
                && !bindingResult.hasFieldErrors("loginPwConfirm")) {
            if (!form.getLoginPw().equals(form.getLoginPwConfirm())) {
                bindingResult.rejectValue("loginPwConfirm", "mismatch",
                        "비밀번호가 일치하지 않습니다.");
            }
        }

        // 2) loginId 중복 사전 검사
        if (!bindingResult.hasFieldErrors("loginId")) {
            if (userService.existsByLoginId(form.getLoginId())) {
                bindingResult.rejectValue("loginId", "duplicate",
                        "이미 사용 중인 아이디입니다.");
            }
        }

        // 3) email 중복 사전 검사
        if (!bindingResult.hasFieldErrors("email")) {
            if (userService.existsByEmail(form.getEmail())) {
                bindingResult.rejectValue("email", "duplicate",
                        "이미 사용 중인 이메일입니다.");
            }
        }

        // 4) 검증 오류 → 폼으로 복귀 (입력값 유지: @ModelAttribute 가 모델에 joinForm 포함)
        if (bindingResult.hasErrors()) {
            return "usr/member/join";
        }

        // 5) 회원 등록 — DB UNIQUE 제약 위반(레이스 컨디션) 방어
        try {
            userService.join(form);
        } catch (DataIntegrityViolationException ex) {
            log.warn("회원가입 중 DB 제약 위반 (레이스 컨디션 의심): loginId={}, email={} — {}",
                     form.getLoginId(), form.getEmail(), ex.getMessage());
            addDuplicateFieldError(bindingResult, ex);
            return "usr/member/join";
        }

        return "redirect:/usr/member/login?joined=1";
    }

    // ── 로그아웃 ─────────────────────────────────────────────────

    @PostMapping("/doLogout")
    public String doLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────

    /**
     * DataIntegrityViolationException 메시지를 분석해 어느 필드의 중복인지 판별한다.
     *
     * <p>MySQL UNIQUE 제약 위반 메시지 예시:
     * <pre>
     *   Duplicate entry 'user01' for key 'users.login_id'
     *   Duplicate entry 'test@test.com' for key 'users.uq_users_email'
     * </pre>
     */
    private void addDuplicateFieldError(BindingResult bindingResult,
                                        DataIntegrityViolationException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        if (msg.contains("login_id")) {
            bindingResult.rejectValue("loginId", "duplicate.db",
                    "이미 사용 중인 아이디입니다.");
        } else if (msg.contains("email") || msg.contains("uq_users_email")) {
            bindingResult.rejectValue("email", "duplicate.db",
                    "이미 사용 중인 이메일입니다.");
        } else {
            // 어느 필드인지 특정 불가 → 전역 오류
            bindingResult.reject("duplicate.db",
                    "이미 가입된 정보가 있습니다. 아이디 또는 이메일을 확인해 주세요.");
        }
    }
}
