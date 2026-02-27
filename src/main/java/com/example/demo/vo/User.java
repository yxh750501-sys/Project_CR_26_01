package com.example.demo.vo;

public class User {

    private long   id;
    private String loginId;
    private String loginPw;
    private String name;
    private String email;
    private String phone;
    private String profileImage;
    private String role;
    /** 회원 유형: GUARDIAN(보호자) / GENERAL(일반회원) */
    private String memberType;
    /** 표시 역할: 치료사 / 센터 / 기관 / 기타 (선택) */
    private String displayRole;
    /** 소속 기관명 (선택) */
    private String orgName;
    private String regDate;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }

    public String getLoginPw() { return loginPw; }
    public void setLoginPw(String loginPw) { this.loginPw = loginPw; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMemberType() { return memberType; }
    public void setMemberType(String memberType) { this.memberType = memberType; }

    public String getDisplayRole() { return displayRole; }
    public void setDisplayRole(String displayRole) { this.displayRole = displayRole; }

    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }

    public String getRegDate() { return regDate; }
    public void setRegDate(String regDate) { this.regDate = regDate; }
}
