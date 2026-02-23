package com.example.demo.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RunRecommendationMapper {

	/* 해당 runId가 현재 로그인 사용자(userId) 소유인지 확인 + 추천 생성 여부 확인 */
	int countRunRecommendationsForUser(@Param("runId") long runId, @Param("userId") long userId);

	Map<String, Object> getRunInfoForUser(@Param("runId") long runId, @Param("userId") long userId);

	/* runId 기준으로 문항/응답을 한 번에 가져옴(추천 생성용) */
	List<Map<String, Object>> getAnsweredRowsForRun(@Param("runId") long runId);

	/* 추천 템플릿 id 조회 */
	Long getRecommendationIdByCode(@Param("code") String code);

	/* 재생성 대비 삭제(증거 → 추천 순서) */
	int deleteEvidenceByRunId(@Param("runId") long runId);
	int deleteRunRecommendationsByRunId(@Param("runId") long runId);

	/* 추천 결과 저장 */
	int insertRunRecommendation(@Param("runId") long runId,
			@Param("recommendationId") long recommendationId,
			@Param("reasonText") String reasonText);

	Long getLastInsertId();

	int insertEvidence(@Param("runRecommendationId") long runRecommendationId,
			@Param("questionId") Long questionId,
			@Param("evidenceText") String evidenceText);

	/* 결과 화면 조회 */
	List<Map<String, Object>> getRunRecommendationsForUser(@Param("runId") long runId, @Param("userId") long userId);

	List<Map<String, Object>> getRunRecommendationEvidenceForUser(@Param("runId") long runId, @Param("userId") long userId);

	/* 히스토리 화면: runId별 추천 제목 요약 */
	List<Map<String, Object>> getRecoSummariesByRunIdsForUser(@Param("userId") long userId, @Param("runIds") List<Long> runIds);
}
