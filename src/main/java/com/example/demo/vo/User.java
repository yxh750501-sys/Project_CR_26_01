package com.example.demo.vo;

public class User {

    private long   id;
    private String loginId;
    private String loginPw;
    private String name;
    private String email;
    private String role;
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

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getRegDate() { return regDate; }
    public void setRegDate(String regDate) { this.regDate = regDate; }
}
