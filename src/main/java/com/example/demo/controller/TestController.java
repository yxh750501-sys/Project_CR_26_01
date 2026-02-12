package com.example.demo.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@GetMapping("/test/user")
	public Map<String, Object> testUser(HttpSession session) {
		Object loginedUserId = session.getAttribute("loginedUserId");
		Object loginedUserRole = session.getAttribute("loginedUserRole");
		Object selectedChildId = session.getAttribute("selectedChildId");

		boolean isLogined = loginedUserId != null;

		Map<String, Object> rs = new LinkedHashMap<>();
		rs.put("isLogined", isLogined);
		rs.put("loginedUserId", loginedUserId);
		rs.put("loginedUserRole", loginedUserRole);
		rs.put("selectedChildId", selectedChildId);

		return rs;
	}
}
