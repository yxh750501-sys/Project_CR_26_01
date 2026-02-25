package com.example.demo.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.DomainStat;

@Mapper
public interface ChecklistRepository {

	Map<String, Object> getRunInfoForResult(@Param("memberId") long memberId, @Param("runId") long runId);

	List<DomainStat> getDomainStatsByRunId(@Param("runId") long runId);
}
