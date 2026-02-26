package com.example.demo.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.dto.RunSummaryDto;
import com.example.demo.vo.DomainStat;

@Mapper
public interface ChecklistRepository {

	Map<String, Object> getRunInfoForResult(@Param("memberId") long memberId, @Param("runId") long runId);

	List<DomainStat> getDomainStatsByRunId(@Param("runId") long runId);

	/**
	 * 사용자의 최근 SUBMITTED 실행 목록.
	 *
	 * @param userId  로그인 사용자 ID
	 * @param childId 0이면 전체 아이, &gt;0이면 해당 아이만
	 * @param limit   최대 건수
	 */
	List<RunSummaryDto> getRecentSubmittedRuns(
			@Param("userId") long userId,
			@Param("childId") long childId,
			@Param("limit") int limit);

	/**
	 * 사용자의 최근 DRAFT 실행 목록.
	 *
	 * @param userId  로그인 사용자 ID
	 * @param childId 0이면 전체 아이, &gt;0이면 해당 아이만
	 * @param limit   최대 건수
	 */
	List<RunSummaryDto> getDraftRuns(
			@Param("userId") long userId,
			@Param("childId") long childId,
			@Param("limit") int limit);
}
