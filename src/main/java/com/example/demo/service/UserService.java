package com.example.demo.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final FileStorageService    fileStorageService;

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       FileStorageService fileStorageService) {
        this.userRepository     = userRepository;
        this.passwordEncoder    = passwordEncoder;
        this.fileStorageService = fileStorageService;
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
        String phone    = (form.getPhone() == null || form.getPhone().isBlank()) ? null : form.getPhone().trim();

        userRepository.createUser(
                form.getLoginId(),
                hashedPw,
                form.getName(),
                form.getEmail(),
                phone,
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

    // ── 프로필 수정 ──────────────────────────────────────────────

    /**
     * 이름·전화번호 수정.
     *
     * @param userId 로그인 사용자 PK
     * @param name   변경할 이름
     * @param phone  변경할 전화번호 (null 또는 빈 문자열 가능)
     */
    @Transactional
    public void updateProfile(long userId, String name, String phone) {
        String normalizedPhone = (phone == null || phone.isBlank()) ? null : phone.trim();
        userRepository.updateProfile(userId, name, normalizedPhone);
    }

    /**
     * 비밀번호 변경.
     *
     * @param userId    로그인 사용자 PK
     * @param currentPw 현재 비밀번호 (원문)
     * @param newPw     새 비밀번호 (원문, 컨트롤러에서 길이 검증 후 전달)
     * @return 현재 비밀번호 일치 시 true, 불일치 시 false
     */
    @Transactional
    public boolean changePassword(long userId, String currentPw, String newPw) {
        User user = userRepository.getUserById(userId);
        if (user == null) return false;
        if (!passwordEncoder.matches(currentPw, user.getLoginPw())) return false;

        userRepository.updatePassword(userId, passwordEncoder.encode(newPw));
        return true;
    }

    /**
     * 프로필 이미지 업로드.
     *
     * @param userId 로그인 사용자 PK
     * @param file   업로드 이미지 파일 (jpg, jpeg, png, gif)
     * @return 저장된 파일명
     */
    @Transactional
    public String updateProfileImage(long userId, MultipartFile file) {
        String storedName = fileStorageService.storeProfileImage(file);
        userRepository.updateProfileImage(userId, storedName);
        return storedName;
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
