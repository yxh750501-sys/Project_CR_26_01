package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.Child;

@Mapper
public interface ChildMapper {

	List<Child> getChildrenByUserId(@Param("userId") long userId);

	Child getChildByIdAndUserId(@Param("id") long id, @Param("userId") long userId);

	int writeChild(@Param("userId") long userId,
			@Param("name") String name,
			@Param("birthDate") String birthDate,
			@Param("gender") String gender,
			@Param("note") String note);

	int modifyChild(@Param("id") long id,
			@Param("userId") long userId,
			@Param("name") String name,
			@Param("birthDate") String birthDate,
			@Param("gender") String gender,
			@Param("note") String note);

	int deleteChild(@Param("id") long id, @Param("userId") long userId);
}
