package com.example.demo.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.User;

@Mapper
public interface UserMapper {

	User getUserByLoginId(@Param("loginId") String loginId);

	int insertUser(User user);
}
