package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.dto.EvidenceItem;
import com.example.demo.vo.Center;
import com.example.demo.vo.DomainStat;

@Mapper
public interface ChecklistResultRepository {

	int countRunOwnedByUser(@Param("runId") long runId, @Param("userId") long userId);

	String getRunStatus(@Param("runId") long runId);

	List<DomainStat> getDomainStatsByRunId(@Param("runId") long runId);

	List<String> getTherapyTypeCodesByDomains(@Param("domains") List<String> domains);

	List<Center> getCentersByTherapyTypeCodes(@Param("therapyTypeCodes") List<String> therapyTypeCodes);

	/**
	 * SCALE5 응답 중 낮은 점수 순으로 최대 {@code limit}건 반환.
	 * score=5(모름)는 제외하여 실질적 어려움 문항만 포함한다.
	 *
	 * @param runId 체크리스트 실행 ID
	 * @param limit 최대 반환 건수
	 */
	List<EvidenceItem> getLowestScoringAnswers(
			@Param("runId") long runId,
			@Param("limit") int limit);
}