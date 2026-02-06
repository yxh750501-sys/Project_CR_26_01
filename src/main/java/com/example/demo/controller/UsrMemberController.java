package com.example.demo.controller;

import com.example.demo.service.UserService;
import com.example.demo.vo.User;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usr/member")
public class UsrMemberController {

    private final UserService userService;

    public UsrMemberController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/join")
    public String showJoin() {
        return "usr/member/join";
    }

    @PostMapping("/doJoin")
    public String doJoin(@RequestParam String loginId,
                         @RequestParam String loginPw,
                         @RequestParam(defaultValue = "GUARDIAN") String role) {
        userService.join(loginId, loginPw, role);
        return "redirect:/usr/member/login";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "usr/member/login";
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam String loginId,
                          @RequestParam String loginPw,
                          HttpSession session) {

        User u = userService.login(loginId, loginPw);
        if (u == null) {
            return "redirect:/usr/member/login?error=1";
        }

        session.setAttribute("loginedUserId", u.getId());
        session.setAttribute("loginedUserRole", u.getRole());

        return "redirect:/usr/member/me";
    }

    @GetMapping("/me")
    public String me() {
        return "usr/member/me";
    }

    @PostMapping("/doLogout")
    public String doLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/usr/member/login";
    }
}
