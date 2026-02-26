package com.example.demo.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.form.JoinForm;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.User;

import jakarta.servlet.http.HttpSession;

@Service
public class UserService {

    /** 세션 키 — 기존 코드 호환 유지 (SessionConst 와 동일 값) */
    public static final String SESSION_KEY_USER_ID   = "loginedUserId";
    public static final String SESSION_KEY_USER_ROLE = "loginedUserRole";

    private final UserRepository        userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── 조회 ────────────────────────────────────────────────────

    public User getUserById(long id) {
        if (id <= 0) return null;
        return userRepository.getUserById(id);
    }

    public User getUserByLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) return null;
        return userRepository.getUserByLoginId(loginId);
    }

    // ── 중복 검사 ────────────────────────────────────────────────

    /** loginId 중복 여부 확인 */
    public boolean existsByLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) return false;
        return userRepository.existsByLoginId(loginId);
    }

    /** email 중복 여부 확인 */
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return userRepository.existsByEmail(email);
    }

    // ── 회원가입 ─────────────────────────────────────────────────

    /**
     * 회원가입.
     *
     * <p>컨트롤러에서 @Valid 검증 + 중복 검사 후 호출된다.
     * 비밀번호를 BCrypt 해싱하여 저장한다.
     *
     * @return 신규 사용자 PK
     */
    @Transactional
    public long join(JoinForm form) {
        String normalizedRole = (form.getRole() == null || form.getRole().isBlank())
                ? "GUARDIAN" : form.getRole();
        String normalizedMemberType = (form.getMemberType() == null || form.getMemberType().isBlank())
                ? "GUARDIAN" : form.getMemberType();
        String hashedPw = passwordEncoder.encode(form.getLoginPw());

        userRepository.createUser(
                form.getLoginId(),
                hashedPw,
                form.getName(),
                form.getEmail(),
                normalizedRole,
                normalizedMemberType,
                form.getDisplayRole(),
                form.getOrgName()
        );
        return userRepository.getLastInsertId();
    }

    // ── 로그인 ───────────────────────────────────────────────────

    /**
     * 로그인 검증.
     * BCrypt {@code matches} 로 입력 비밀번호와 저장된 해시를 비교한다.
     *
     * @return 인증 성공 시 User, 실패 시 null
     */
    public User login(String loginId, String loginPw) {
        if (loginId == null || loginId.isBlank()) return null;
        if (loginPw == null || loginPw.isBlank()) return null;

        User user = userRepository.getUserByLoginId(loginId);
        if (user == null) return null;

        String savedHash = user.getLoginPw();
        if (savedHash == null || !passwordEncoder.matches(loginPw, savedHash)) return null;

        return user;
    }

    // ── 세션 헬퍼 ────────────────────────────────────────────────

    public void setLoginSession(User user, HttpSession session) {
        if (user == null || session == null) return;
        session.setAttribute(SESSION_KEY_USER_ID,   user.getId());
        session.setAttribute(SESSION_KEY_USER_ROLE, user.getRole());
    }

    public void logout(HttpSession session) {
        if (session == null) return;
        session.removeAttribute(SESSION_KEY_USER_ID);
        session.removeAttribute(SESSION_KEY_USER_ROLE);
    }

    public Long getLoginedUserId(HttpSession session) {
        if (session == null) return null;
        return toLong(session.getAttribute(SESSION_KEY_USER_ID));
    }

    public String getLoginedUserRole(HttpSession session) {
        if (session == null) return null;
        Object v = session.getAttribute(SESSION_KEY_USER_ROLE);
        return v == null ? null : v.toString();
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }
}
