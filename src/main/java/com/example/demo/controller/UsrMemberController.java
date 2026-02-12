package com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.service.UserService;
import com.example.demo.vo.User;

@Controller
@RequestMapping("/usr/member")
public class UsrMemberController {

	private final UserService userService;

	public UsrMemberController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/login")
	public String showLogin(HttpServletRequest req) {
		return "usr/member/login";
	}

	@PostMapping("/doLogin")
	public String doLogin(@RequestParam("loginId") String loginId,
						  @RequestParam("loginPw") String loginPw,
						  HttpServletRequest req,
						  HttpSession session) {

		User user = userService.login(loginId, loginPw);

		// ★ 실패를 예외로 처리하지 말고 화면으로 돌려보내야 500이 안 남
		if (user == null) {
			req.setAttribute("msg", "아이디 또는 비밀번호가 올바르지 않습니다.");
			return "usr/member/login";
		}

		session.setAttribute("loginedUserId", user.getId());
		session.setAttribute("loginedUserRole", user.getRole());

		return "redirect:/usr/member/me";
	}

	@GetMapping("/join")
	public String showJoin() {
		return "usr/member/join";
	}

	@PostMapping("/doJoin")
	public String doJoin(@RequestParam("loginId") String loginId,
						 @RequestParam("loginPw") String loginPw,
						 @RequestParam(value = "role", required = false) String role,
						 HttpServletRequest req) {

		long newId = userService.join(loginId, loginPw, role);

		if (newId <= 0) {
			req.setAttribute("msg", "이미 사용 중인 아이디입니다.");
			return "usr/member/join";
		}

		req.setAttribute("msg", "회원가입 완료. 로그인 해주세요.");
		return "usr/member/login";
	}

	@GetMapping("/me")
	public String showMe(HttpServletRequest req, HttpSession session) {
		Object v = session.getAttribute("loginedUserId");
		if (v == null) return "redirect:/usr/member/login";

		long userId = (v instanceof Number) ? ((Number) v).longValue() : Long.parseLong(String.valueOf(v));
		// 필요하면 userId로 사용자 조회해서 화면에 뿌려도 됨
		req.setAttribute("loginedUserId", userId);
		return "usr/member/me";
	}

	@PostMapping("/doLogout")
	public String doLogout(HttpSession session) {
		session.invalidate();
		return "redirect:/usr/member/login";
	}
}
