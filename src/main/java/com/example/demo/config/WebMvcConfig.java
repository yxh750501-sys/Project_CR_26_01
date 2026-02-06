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

        registry.addInterceptor(beforeActionInterceptor)
                .addPathPatterns("/**");

        registry.addInterceptor(needLoginInterceptor)
                .addPathPatterns("/usr/**")
                .excludePathPatterns(
                        "/usr/member/login",
                        "/usr/member/doLogin",
                        "/usr/member/join",
                        "/usr/member/doJoin"
                );

        registry.addInterceptor(needLogoutInterceptor)
                .addPathPatterns(
                        "/usr/member/login",
                        "/usr/member/doLogin",
                        "/usr/member/join",
                        "/usr/member/doJoin"
                );
    }
}
