package com.example.demo.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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

	/** 업로드 루트 디렉토리 (application.properties: app.upload.dir) */
	@Value("${app.upload.dir}")
	private String uploadDir;

	// ── 정적 리소스 핸들러 ────────────────────────────────────────

	/**
	 * 업로드 파일을 정적 리소스로 노출한다.
	 * - /uploads/profile/** → ${uploadDir}/profile/  (프로필 이미지)
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String profileLocation = Paths.get(uploadDir, "profile").toUri().toString();
		if (!profileLocation.endsWith("/")) {
			profileLocation = profileLocation + "/";
		}
		registry.addResourceHandler("/uploads/profile/**")
				.addResourceLocations(profileLocation);
	}

	// ── 인터셉터 ──────────────────────────────────────────────────

	/**
	 * 인터셉터 등록.
	 *
	 * <p>홈("/")은 /usr/** 패턴 밖이므로 NeedLoginInterceptor 미적용.
	 * /usr/home 은 "/" 로 리다이렉트되므로 별도 제외 불필요.
	 */
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
				"/usr/member/doJoin",
				// /usr/home → "/" 리다이렉트 경유, 비로그인도 허용
				"/usr/home",
				// 게시판 목록/상세/파일 다운로드 비로그인 접근 허용
				"/usr/program/list",
				"/usr/program/detail",
				"/usr/free/list",
				"/usr/free/detail",
				"/usr/post/list",
				"/usr/post/detail",
				"/usr/file/download"
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
