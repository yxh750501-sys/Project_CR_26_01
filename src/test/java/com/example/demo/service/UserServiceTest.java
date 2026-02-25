package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.form.JoinForm;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.User;

/**
 * UserService 단위 테스트.
 *
 * <p>DB, Spring Context 불필요 — Mockito 만 사용.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository        userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // ─────────────────────────────────────────────────
    // existsByLoginId
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("existsByLoginId: 레포지토리가 true 반환 → true")
    void existsByLoginId_whenRepoReturnsTrue_returnsTrue() {
        when(userRepository.existsByLoginId("dupUser")).thenReturn(true);
        assertThat(userService.existsByLoginId("dupUser")).isTrue();
    }

    @Test
    @DisplayName("existsByLoginId: 레포지토리가 false 반환 → false")
    void existsByLoginId_whenRepoReturnsFalse_returnsFalse() {
        when(userRepository.existsByLoginId("newUser")).thenReturn(false);
        assertThat(userService.existsByLoginId("newUser")).isFalse();
    }

    @Test
    @DisplayName("existsByLoginId: null 또는 공백 입력 → DB 호출 없이 false")
    void existsByLoginId_blankInput_returnsFalseWithoutDbCall() {
        assertThat(userService.existsByLoginId(null)).isFalse();
        assertThat(userService.existsByLoginId("  ")).isFalse();
        // userRepository 는 한 번도 호출되지 않아야 한다
        verify(userRepository, org.mockito.Mockito.never()).existsByLoginId(org.mockito.ArgumentMatchers.any());
    }

    // ─────────────────────────────────────────────────
    // existsByEmail
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("existsByEmail: 레포지토리가 true 반환 → true")
    void existsByEmail_whenRepoReturnsTrue_returnsTrue() {
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);
        assertThat(userService.existsByEmail("dup@test.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail: 레포지토리가 false 반환 → false")
    void existsByEmail_whenRepoReturnsFalse_returnsFalse() {
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        assertThat(userService.existsByEmail("new@test.com")).isFalse();
    }

    // ─────────────────────────────────────────────────
    // join
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("join: 비밀번호를 BCrypt 해싱해서 저장한다")
    void join_hashesPasswordWithBCrypt() {
        JoinForm form = makeJoinForm("user1", "password123", "홍길동", "user1@test.com", null);

        when(passwordEncoder.encode("password123")).thenReturn("$2a$HASHED");
        when(userRepository.getLastInsertId()).thenReturn(1L);

        long newId = userService.join(form);

        assertThat(newId).isEqualTo(1L);
        // BCrypt 인코더가 정확히 1번 호출되어야 한다
        verify(passwordEncoder).encode("password123");
        // 해시된 값이 저장되어야 한다 (평문 아님)
        verify(userRepository).createUser("user1", "$2a$HASHED", "홍길동", "user1@test.com", "GUARDIAN");
    }

    @Test
    @DisplayName("join: role이 null이면 GUARDIAN을 기본값으로 사용한다")
    void join_usesDefaultRole_whenRoleIsNull() {
        JoinForm form = makeJoinForm("user2", "pass12345", "김영희", "user2@test.com", null);

        when(passwordEncoder.encode("pass12345")).thenReturn("$2a$HASHED2");
        when(userRepository.getLastInsertId()).thenReturn(2L);

        userService.join(form);

        verify(userRepository).createUser("user2", "$2a$HASHED2", "김영희", "user2@test.com", "GUARDIAN");
    }

    @Test
    @DisplayName("join: role이 THERAPIST로 지정되면 그대로 저장한다")
    void join_usesGivenRole_whenRoleIsTherapist() {
        JoinForm form = makeJoinForm("therapist1", "pass12345", "이치료", "t@test.com", "THERAPIST");

        when(passwordEncoder.encode("pass12345")).thenReturn("$2a$HASHED3");
        when(userRepository.getLastInsertId()).thenReturn(3L);

        userService.join(form);

        verify(userRepository).createUser("therapist1", "$2a$HASHED3", "이치료", "t@test.com", "THERAPIST");
    }

    // ─────────────────────────────────────────────────
    // login
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("login: 올바른 자격증명 → User 반환")
    void login_correctCredentials_returnsUser() {
        User stored = new User();
        stored.setId(1L);
        stored.setLoginId("user1");
        stored.setLoginPw("$2a$HASHED");

        when(userRepository.getUserByLoginId("user1")).thenReturn(stored);
        when(passwordEncoder.matches("plain123!", "$2a$HASHED")).thenReturn(true);

        User result = userService.login("user1", "plain123!");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("login: 비밀번호 불일치 → null 반환")
    void login_wrongPassword_returnsNull() {
        User stored = new User();
        stored.setLoginPw("$2a$HASHED");

        when(userRepository.getUserByLoginId("user1")).thenReturn(stored);
        when(passwordEncoder.matches("wrongPw", "$2a$HASHED")).thenReturn(false);

        User result = userService.login("user1", "wrongPw");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("login: 존재하지 않는 loginId → null 반환")
    void login_unknownLoginId_returnsNull() {
        when(userRepository.getUserByLoginId("unknown")).thenReturn(null);

        User result = userService.login("unknown", "anyPw");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("login: loginId가 null 또는 공백 → null 반환 (DB 미호출)")
    void login_blankLoginId_returnsNullWithoutDbCall() {
        assertThat(userService.login(null, "pw")).isNull();
        assertThat(userService.login("  ", "pw")).isNull();
        verify(userRepository, org.mockito.Mockito.never())
                .getUserByLoginId(org.mockito.ArgumentMatchers.any());
    }

    // ─────────────────────────────────────────────────
    // 헬퍼
    // ─────────────────────────────────────────────────

    private JoinForm makeJoinForm(String loginId, String loginPw,
                                  String name, String email, String role) {
        JoinForm f = new JoinForm();
        f.setLoginId(loginId);
        f.setLoginPw(loginPw);
        f.setLoginPwConfirm(loginPw);
        f.setName(name);
        f.setEmail(email);
        f.setRole(role);
        return f;
    }
}
