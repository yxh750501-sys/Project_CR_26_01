package com.example.demo.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.repository.UserMapper;
import com.example.demo.vo.User;

@Service
public class UserService {

	private final UserMapper userMapper;
	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	public UserService(UserMapper userMapper) {
		this.userMapper = userMapper;
	}

	public User getUserByLoginId(String loginId) {
		return userMapper.getUserByLoginId(loginId);
	}

	public long join(String loginId, String loginPw, String role) {
		User exists = userMapper.getUserByLoginId(loginId);
		if (exists != null) {
			return -1;
		}

		User user = new User();
		user.setLoginId(loginId);
		user.setLoginPw(encoder.encode(loginPw));
		user.setRole(role == null || role.isBlank() ? "GUARDIAN" : role);

		userMapper.insertUser(user);
		return user.getId();
	}

	public User login(String loginId, String loginPw) {
		User user = userMapper.getUserByLoginId(loginId);

		// ★ 여기서 null 체크 안 하면, DB에 계정이 없을 때 바로 500(NPE) 터진다
		if (user == null) return null;
		if (user.getLoginPw() == null || user.getLoginPw().isBlank()) return null;

		boolean ok = encoder.matches(loginPw, user.getLoginPw());
		return ok ? user : null;
	}
}
