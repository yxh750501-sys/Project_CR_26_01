package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 애플리케이션 공통 빈 설정.
 */
@Configuration
public class AppConfig {

    /**
     * BCrypt 해싱 인코더.
     * UserService에서 회원가입 시 비밀번호 저장, 로그인 시 검증에 사용.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
