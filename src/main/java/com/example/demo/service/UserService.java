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

    public void join(String loginId, String loginPw, String role) {
        if (loginId == null || loginId.isBlank()) throw new IllegalArgumentException("아이디를 입력하세요.");
        if (loginPw == null || loginPw.length() < 4) throw new IllegalArgumentException("비밀번호는 4자 이상.");

        if (userMapper.countByLoginId(loginId) > 0) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        User u = new User();
        u.setLoginId(loginId.trim());
        u.setLoginPw(encoder.encode(loginPw));
        u.setRole((role == null || role.isBlank()) ? "GUARDIAN" : role);

        userMapper.insert(u);
    }

    public User login(String loginId, String loginPw) {
        User u = userMapper.findByLoginId(loginId);
        if (u == null) return null;

        if (!encoder.matches(loginPw, u.getLoginPw())) return null;

        return u;
    }
}
