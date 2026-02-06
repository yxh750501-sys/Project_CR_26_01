package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.demo.interceptor.BeforeActionInterceptor;
import com.example.demo.interceptor.NeedLoginInterceptor;
import com.example.demo.interceptor.NeedLogoutInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final BeforeActionInterceptor beforeActionInterceptor;
    private final NeedLoginInterceptor needLoginInterceptor;
    private final NeedLogoutInterceptor needLogoutInterceptor;

    public WebMvcConfig(BeforeActionInterceptor beforeActionInterceptor,
                        NeedLoginInterceptor needLoginInterceptor,
                        NeedLogoutInterceptor needLogoutInterceptor) {
        this.beforeActionInterceptor = beforeActionInterceptor;
        this.needLoginInterceptor = needLoginInterceptor;
        this.needLogoutInterceptor = needLogoutInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 1) 공통 처리: 전체
        registry.addInterceptor(beforeActionInterceptor)
                .addPathPatterns("/**");

        // 2) 로그인 필요: /usr/** 전부(단, 로그인/회원가입 관련은 제외)
        registry.addInterceptor(needLoginInterceptor)
                .addPathPatterns("/usr/**")
                .excludePathPatterns(
                        "/usr/member/login",
                        "/usr/member/doLogin",
                        "/usr/member/join",
                        "/usr/member/doJoin"
                );

        // 3) 로그아웃 필요: 로그인/회원가입 화면/처리
        registry.addInterceptor(needLogoutInterceptor)
                .addPathPatterns(
                        "/usr/member/login",
                        "/usr/member/doLogin",
                        "/usr/member/join",
                        "/usr/member/doJoin"
                );
    }
}
