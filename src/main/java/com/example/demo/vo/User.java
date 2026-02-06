package com.example.demo.vo;

import lombok.Data;

@Data
public class User {
    private long id;
    private String loginId;
    private String loginPw;
    private String role;
}
