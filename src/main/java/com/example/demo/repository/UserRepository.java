package com.example.demo.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.User;

@Mapper
public interface UserRepository {

	User getUserById(@Param("id") long id);

	User getUserByLoginId(@Param("loginId") String loginId);

	int createUser(@Param("loginId") String loginId, @Param("loginPw") String loginPw, @Param("role") String role);

	long getLastInsertId();
}