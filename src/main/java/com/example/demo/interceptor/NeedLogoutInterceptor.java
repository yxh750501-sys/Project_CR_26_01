package com.example.demo.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class NeedLogoutInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        HttpSession session = req.getSession();

        boolean isLogined = session.getAttribute("loginedUserId") != null;
        if (!isLogined) return true;

        resp.sendRedirect("/usr/member/me");
        return false;
    }
}
