package com.example.demo.repository;

import org.apache.ibatis.annotations.Mapper;
import com.example.demo.vo.User;

@Mapper
public interface UserMapper {
    User findByLoginId(String loginId);
}
