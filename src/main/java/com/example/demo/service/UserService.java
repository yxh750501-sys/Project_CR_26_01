package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.UserRepository;
import com.example.demo.vo.User;

import jakarta.servlet.http.HttpSession;

@Service
public class UserService {

	public static final String SESSION_KEY_USER_ID = "loginedUserId";
	public static final String SESSION_KEY_USER_ROLE = "loginedUserRole";

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User getUserById(long id) {
		if (id <= 0) {
			return null;
		}
		return userRepository.getUserById(id);
	}

	public User getUserByLoginId(String loginId) {
		if (loginId == null || loginId.isBlank()) {
			return null;
		}
		return userRepository.getUserByLoginId(loginId);
	}

	@Transactional
	public long join(String loginId, String loginPw, String role) {
		if (loginId == null || loginId.isBlank()) {
			throw new IllegalArgumentException("loginId is required");
		}
		if (loginPw == null || loginPw.isBlank()) {
			throw new IllegalArgumentException("loginPw is required");
		}

		String normalizedRole = (role == null || role.isBlank()) ? "GUARDIAN" : role;

		User existing = userRepository.getUserByLoginId(loginId);
		if (existing != null) {
			throw new IllegalStateException("이미 존재하는 로그인 아이디입니다.");
		}

		userRepository.createUser(loginId, loginPw, normalizedRole);
		return userRepository.getLastInsertId();
	}

	public User login(String loginId, String loginPw) {
		if (loginId == null || loginId.isBlank()) {
			return null;
		}
		if (loginPw == null || loginPw.isBlank()) {
			return null;
		}

		User user = userRepository.getUserByLoginId(loginId);
		if (user == null) {
			return null;
		}

		String savedPw = user.getLoginPw();
		if (savedPw == null || !savedPw.equals(loginPw)) {
			return null;
		}

		return user;
	}

	public void setLoginSession(User user, HttpSession session) {
		if (user == null || session == null) {
			return;
		}
		session.setAttribute(SESSION_KEY_USER_ID, user.getId());
		session.setAttribute(SESSION_KEY_USER_ROLE, user.getRole());
	}

	public void logout(HttpSession session) {
		if (session == null) {
			return;
		}
		session.removeAttribute(SESSION_KEY_USER_ID);
		session.removeAttribute(SESSION_KEY_USER_ROLE);
	}

	public Long getLoginedUserId(HttpSession session) {
		if (session == null) return null;
		return toLong(session.getAttribute(SESSION_KEY_USER_ID));
	}

	public String getLoginedUserRole(HttpSession session) {
		if (session == null) return null;
		Object v = session.getAttribute(SESSION_KEY_USER_ROLE);
		return v == null ? null : v.toString();
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