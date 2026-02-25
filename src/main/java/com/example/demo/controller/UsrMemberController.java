package com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.constant.SessionConst;
import com.example.demo.form.JoinForm;
import com.example.demo.service.UserService;
import com.example.demo.vo.User;

@Controller
@RequestMapping("/usr/member")
public class UsrMemberController {

    private final UserService userService;

    public UsrMemberController(UserService userService) {
        this.userService = userService;
    }

    // ── 로그인 ───────────────────────────────────────────────────

    @GetMapping("/login")
    public String showLogin() {
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

        return "redirect:/usr/member/me";
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
     *   <li>@Valid 어노테이션 검증 (NotBlank, Size, Email)
     *   <li>비밀번호 확인 일치 검사
     *   <li>loginId / email 중복 검사
     *   <li>검증 오류가 있으면 회원가입 폼으로 돌아감 (입력값 유지)
     *   <li>모든 검증 통과 시 회원가입 처리 후 로그인 페이지로 redirect
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

        // 2) loginId 중복 검사
        if (!bindingResult.hasFieldErrors("loginId")) {
            if (userService.existsByLoginId(form.getLoginId())) {
                bindingResult.rejectValue("loginId", "duplicate",
                        "이미 사용 중인 아이디입니다.");
            }
        }

        // 3) email 중복 검사
        if (!bindingResult.hasFieldErrors("email")) {
            if (userService.existsByEmail(form.getEmail())) {
                bindingResult.rejectValue("email", "duplicate",
                        "이미 사용 중인 이메일입니다.");
            }
        }

        // 4) 오류가 있으면 회원가입 폼으로 돌아감
        if (bindingResult.hasErrors()) {
            // @ModelAttribute 가 이미 "joinForm" 으로 모델에 추가됨
            return "usr/member/join";
        }

        // 5) 회원가입 처리 (BCrypt 해싱 포함)
        userService.join(form);

        return "redirect:/usr/member/login?joined=1";
    }

    // ── 마이페이지 ───────────────────────────────────────────────

    @GetMapping("/me")
    public String showMe(HttpServletRequest req, HttpSession session) {
        Object v = session.getAttribute(SessionConst.LOGINED_USER_ID);
        if (v == null) return "redirect:/usr/member/login";

        long userId = (v instanceof Number) ? ((Number) v).longValue()
                                            : Long.parseLong(String.valueOf(v));
        req.setAttribute("loginedUserId", userId);
        return "usr/member/me";
    }

    // ── 로그아웃 ─────────────────────────────────────────────────

    @PostMapping("/doLogout")
    public String doLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/usr/member/login";
    }
}
