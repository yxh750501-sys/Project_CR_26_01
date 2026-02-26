package com.example.demo.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 폼 DTO.
 *
 * <p>Controller에서 {@code @Valid @ModelAttribute("joinForm")} 으로 바인딩된다.
 * loginPwConfirm 일치 여부는 컨트롤러에서 추가 검증한다.
 */
public class JoinForm {

    @NotBlank(message = "아이디를 입력해 주세요.")
    @Size(min = 4, max = 20, message = "아이디는 4~20자로 입력해 주세요.")
    private String loginId;

    @NotBlank(message = "이름을 입력해 주세요.")
    @Size(max = 50, message = "이름은 50자 이내로 입력해 주세요.")
    private String name;

    @NotBlank(message = "이메일을 입력해 주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자로 입력해 주세요.")
    private String loginPw;

    @NotBlank(message = "비밀번호 확인을 입력해 주세요.")
    private String loginPwConfirm;

    /** 역할(GUARDIAN / THERAPIST). 미입력 시 서비스에서 GUARDIAN 기본 적용 */
    private String role;

    // ── getters / setters ──────────────────────────────────────

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLoginPw() { return loginPw; }
    public void setLoginPw(String loginPw) { this.loginPw = loginPw; }

    public String getLoginPwConfirm() { return loginPwConfirm; }
    public void setLoginPwConfirm(String loginPwConfirm) { this.loginPwConfirm = loginPwConfirm; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
