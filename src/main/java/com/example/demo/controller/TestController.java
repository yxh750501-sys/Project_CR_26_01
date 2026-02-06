package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repository.UserMapper;
import com.example.demo.vo.User;

@RestController
public class TestController {

    private final UserMapper userMapper;

    public TestController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/test/user")
    public User testUser() {
        return userMapper.findByLoginId("test1");
    }
}
