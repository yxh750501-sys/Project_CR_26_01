package com.example.demo.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.form.JoinForm;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** 세션 키 — 기존 코드 호환 유지 (SessionConst 와 동일 값) */
    public static final String SESSION_KEY_USER_ID   = "loginedUserId";
    public static final String SESSION_KEY_USER_ROLE = "loginedUserRole";

    @Value("${google.oauth.client-id:}")
    private String googleClientId;

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

    // ── Google OAuth ─────────────────────────────────────────────

    /**
     * Google id_token 검증 → DB 조회/생성 → User 반환.
     * 실패 시 null 반환.
     */
    @Transactional
    public User loginOrRegisterWithGoogle(String credential) {
        GoogleUserInfo info = verifyGoogleToken(credential);
        if (info == null) return null;

        // 1. oauth_sub 로 기존 계정 조회
        User user = userRepository.getUserByOauthSub("google", info.sub);
        if (user != null) return user;

        // 2. 동일 이메일의 기존 계정이 있으면 OAuth 연결
        user = userRepository.getUserByEmail(info.email);
        if (user != null) {
            userRepository.linkOauth(user.getId(), "google", info.sub);
            return user;
        }

        // 3. 신규 사용자 생성 (loginId = "google:" + sub)
        String loginId  = "google:" + info.sub;
        String dummyPw  = passwordEncoder.encode(UUID.randomUUID().toString());
        String name     = (info.name != null && !info.name.isBlank()) ? info.name : info.email;
        userRepository.createOauthUser(loginId, dummyPw, name, info.email, "google", info.sub);
        return userRepository.getUserByOauthSub("google", info.sub);
    }

    /**
     * Google tokeninfo 엔드포인트로 id_token 검증.
     * 성공 시 GoogleUserInfo, 실패 시 null.
     */
    private GoogleUserInfo verifyGoogleToken(String idToken) {
        if (idToken == null || idToken.isBlank()) return null;
        if (googleClientId == null || googleClientId.isBlank()) {
            log.warn("google.oauth.client-id 가 설정되지 않았습니다.");
            return null;
        }
        try {
            String urlStr = "https://oauth2.googleapis.com/tokeninfo?id_token="
                          + URLEncoder.encode(idToken, StandardCharsets.UTF_8.name());
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) return null;

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                JsonNode node = objectMapper.readTree(sb.toString());

                // aud 가 우리 클라이언트 ID 와 일치해야 함
                if (!googleClientId.equals(node.path("aud").asText(""))) return null;

                String sub   = node.path("sub").asText(null);
                String email = node.path("email").asText(null);
                String name  = node.path("name").asText(null);

                if (sub == null || email == null) return null;
                return new GoogleUserInfo(sub, email, name);
            }
        } catch (Exception e) {
            log.warn("Google token 검증 실패: {}", e.getMessage());
            return null;
        }
    }

    /** Google 토큰에서 추출한 사용자 정보 */
    private static class GoogleUserInfo {
        final String sub, email, name;
        GoogleUserInfo(String sub, String email, String name) {
            this.sub = sub; this.email = email; this.name = name;
        }
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
