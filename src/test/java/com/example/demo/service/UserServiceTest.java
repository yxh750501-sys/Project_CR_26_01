package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

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

    @Mock
    private FileStorageService    fileStorageService;

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
        JoinForm form = makeJoinForm("user1", "password123", "홍길동", "user1@test.com", null, null);

        when(passwordEncoder.encode("password123")).thenReturn("$2a$HASHED");
        when(userRepository.getLastInsertId()).thenReturn(1L);

        long newId = userService.join(form);

        assertThat(newId).isEqualTo(1L);
        verify(passwordEncoder).encode("password123");
        // memberType 미설정 시 GUARDIAN 기본값, phone/displayRole/orgName null 전달
        verify(userRepository).createUser(
                "user1", "$2a$HASHED", "홍길동", "user1@test.com",
                null, "GUARDIAN", "GUARDIAN", null, null);
    }

    @Test
    @DisplayName("join: role이 null이면 GUARDIAN을 기본값으로 사용한다")
    void join_usesDefaultRole_whenRoleIsNull() {
        JoinForm form = makeJoinForm("user2", "pass12345", "김영희", "user2@test.com", null, null);

        when(passwordEncoder.encode("pass12345")).thenReturn("$2a$HASHED2");
        when(userRepository.getLastInsertId()).thenReturn(2L);

        userService.join(form);

        verify(userRepository).createUser(
                "user2", "$2a$HASHED2", "김영희", "user2@test.com",
                null, "GUARDIAN", "GUARDIAN", null, null);
    }

    @Test
    @DisplayName("join: role이 THERAPIST로 지정되면 그대로 저장한다")
    void join_usesGivenRole_whenRoleIsTherapist() {
        JoinForm form = makeJoinForm("therapist1", "pass12345", "이치료", "t@test.com", "THERAPIST", null);

        when(passwordEncoder.encode("pass12345")).thenReturn("$2a$HASHED3");
        when(userRepository.getLastInsertId()).thenReturn(3L);

        userService.join(form);

        verify(userRepository).createUser(
                "therapist1", "$2a$HASHED3", "이치료", "t@test.com",
                null, "THERAPIST", "GUARDIAN", null, null);
    }

    @Test
    @DisplayName("join: memberType=GENERAL, displayRole/orgName 설정 시 그대로 저장한다")
    void join_withMemberTypeGeneral_savesDisplayRoleAndOrgName() {
        JoinForm form = makeJoinForm("center1", "pass12345", "박센터", "center@test.com", null, null);
        form.setMemberType("GENERAL");
        form.setDisplayRole("센터");
        form.setOrgName("행복발달센터");

        when(passwordEncoder.encode("pass12345")).thenReturn("$2a$HASHED4");
        when(userRepository.getLastInsertId()).thenReturn(4L);

        userService.join(form);

        verify(userRepository).createUser(
                "center1", "$2a$HASHED4", "박센터", "center@test.com",
                null, "GUARDIAN", "GENERAL", "센터", "행복발달센터");
    }

    @Test
    @DisplayName("join: phone 설정 시 저장된다")
    void join_withPhone_savesPhone() {
        JoinForm form = makeJoinForm("user5", "pass12345", "최전화", "user5@test.com", null, "010-1234-5678");

        when(passwordEncoder.encode("pass12345")).thenReturn("$2a$HASHED5");
        when(userRepository.getLastInsertId()).thenReturn(5L);

        userService.join(form);

        verify(userRepository).createUser(
                "user5", "$2a$HASHED5", "최전화", "user5@test.com",
                "010-1234-5678", "GUARDIAN", "GUARDIAN", null, null);
    }

    @Test
    @DisplayName("join: phone이 빈 문자열이면 null로 저장된다")
    void join_withBlankPhone_savesNull() {
        JoinForm form = makeJoinForm("user6", "pass12345", "공백전화", "user6@test.com", null, "  ");

        when(passwordEncoder.encode("pass12345")).thenReturn("$2a$HASHED6");
        when(userRepository.getLastInsertId()).thenReturn(6L);

        userService.join(form);

        verify(userRepository).createUser(
                "user6", "$2a$HASHED6", "공백전화", "user6@test.com",
                null, "GUARDIAN", "GUARDIAN", null, null);
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
    // updateProfile
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("updateProfile: 이름과 전화번호를 정상 저장한다")
    void updateProfile_savesNameAndPhone() {
        userService.updateProfile(1L, "홍길동", "010-9999-1234");

        verify(userRepository).updateProfile(1L, "홍길동", "010-9999-1234");
    }

    @Test
    @DisplayName("updateProfile: 전화번호가 빈 문자열이면 null로 저장한다")
    void updateProfile_blankPhone_savesNull() {
        userService.updateProfile(1L, "홍길동", "");

        verify(userRepository).updateProfile(1L, "홍길동", null);
    }

    // ─────────────────────────────────────────────────
    // changePassword
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("changePassword: 현재 비밀번호 일치 → 변경 성공 (true 반환)")
    void changePassword_correctCurrentPw_changesAndReturnsTrue() {
        User user = new User();
        user.setId(1L);
        user.setLoginPw("$2a$OLDHASH");

        when(userRepository.getUserById(1L)).thenReturn(user);
        when(passwordEncoder.matches("oldPw", "$2a$OLDHASH")).thenReturn(true);
        when(passwordEncoder.encode("newPw1234")).thenReturn("$2a$NEWHASH");

        boolean result = userService.changePassword(1L, "oldPw", "newPw1234");

        assertThat(result).isTrue();
        verify(userRepository).updatePassword(1L, "$2a$NEWHASH");
    }

    @Test
    @DisplayName("changePassword: 현재 비밀번호 불일치 → false 반환, updatePassword 미호출")
    void changePassword_wrongCurrentPw_returnsFalse() {
        User user = new User();
        user.setId(1L);
        user.setLoginPw("$2a$OLDHASH");

        when(userRepository.getUserById(1L)).thenReturn(user);
        when(passwordEncoder.matches("wrongPw", "$2a$OLDHASH")).thenReturn(false);

        boolean result = userService.changePassword(1L, "wrongPw", "newPw1234");

        assertThat(result).isFalse();
        verify(userRepository, never()).updatePassword(anyLong(), anyString());
    }

    // ─────────────────────────────────────────────────
    // updateProfileImage
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("updateProfileImage: 저장 성공 시 파일명을 DB에 저장한다")
    void updateProfileImage_success_savesStoredName() {
        MultipartFile mockFile = org.mockito.Mockito.mock(MultipartFile.class);
        when(fileStorageService.storeProfileImage(mockFile)).thenReturn("uuid.jpg");

        String result = userService.updateProfileImage(1L, mockFile);

        assertThat(result).isEqualTo("uuid.jpg");
        verify(userRepository).updateProfileImage(1L, "uuid.jpg");
    }

    // ─────────────────────────────────────────────────
    // 헬퍼
    // ─────────────────────────────────────────────────

    private JoinForm makeJoinForm(String loginId, String loginPw,
                                  String name, String email, String role, String phone) {
        JoinForm f = new JoinForm();
        f.setLoginId(loginId);
        f.setLoginPw(loginPw);
        f.setLoginPwConfirm(loginPw);
        f.setName(name);
        f.setEmail(email);
        f.setRole(role);
        f.setPhone(phone);
        return f;
    }
}
