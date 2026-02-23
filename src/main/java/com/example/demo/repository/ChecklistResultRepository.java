package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.Center;
import com.example.demo.vo.DomainStat;

@Mapper
public interface ChecklistResultRepository {

	int countRunOwnedByUser(@Param("runId") long runId, @Param("userId") long userId);

	String getRunStatus(@Param("runId") long runId);

	List<DomainStat> getDomainStatsByRunId(@Param("runId") long runId);

	List<String> getTherapyTypeCodesByDomains(@Param("domains") List<String> domains);

	List<Center> getCentersByTherapyTypeCodes(@Param("therapyTypeCodes") List<String> therapyTypeCodes);
}