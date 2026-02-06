package com.example.demo.repository;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.example.demo.vo.Child;

@Mapper
public interface ChildMapper {
    List<Child> findAllByUserId(long userId);
    Child findByIdAndUserId(long id, long userId);
    int insert(Child child);
    int update(Child child);
    int deleteByIdAndUserId(long id, long userId);
}
