package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.demo.interceptor.BeforeActionInterceptor;
import com.example.demo.interceptor.NeedLoginInterceptor;
import com.example.demo.interceptor.NeedLogoutInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final BeforeActionInterceptor beforeActionInterceptor;
	private final NeedLoginInterceptor needLoginInterceptor;
	private final NeedLogoutInterceptor needLogoutInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		registry.addInterceptor(beforeActionInterceptor)
			.addPathPatterns("/usr/**");

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
