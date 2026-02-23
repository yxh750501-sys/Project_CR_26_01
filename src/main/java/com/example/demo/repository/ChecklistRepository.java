package com.example.demo.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.DomainStat;

@Mapper
public interface ChecklistRepository {

	Map<String, Object> getRunInfoForResult(@Param("memberId") int memberId, @Param("runId") int runId);

	List<DomainStat> getDomainStatsByRunId(@Param("runId") int runId);
}