package com.example.demo.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.AnswerForStart;
import com.example.demo.vo.ChecklistForStart;
import com.example.demo.vo.ChecklistQuestionForStart;

@Mapper
public interface ChecklistStartRepository {

	ChecklistForStart getChecklistById(@Param("checklistId") long checklistId);

	List<ChecklistQuestionForStart> getQuestionsByChecklistId(@Param("checklistId") long checklistId);

	/** DRAFT 상태 run 중 가장 최근 id */
	Long getLatestDraftRunId(@Param("userId") long userId, @Param("childId") long childId,
			@Param("checklistId") long checklistId);

	/** DRAFT run 기본 정보 조회 (선택 화면 표시용 — lastSavedAt 포함) */
	Map<String, Object> getRunBasicInfo(@Param("runId") long runId);

	/** 새 DRAFT run 생성 */
	int createRun(@Param("checklistId") long checklistId, @Param("childId") long childId,
			@Param("userId") long userId);

	long getLastInsertId();

	/** run이 실제로 속한 checklist_id 반환 (form 위변조 검증용) */
	Long getChecklistIdByRunId(@Param("runId") long runId);

	/** (run_id, question_id) UNIQUE 기반 UPSERT */
	int upsertAnswer(@Param("runId") long runId, @Param("questionId") long questionId,
			@Param("answerValue") String answerValue, @Param("answerText") String answerText,
			@Param("score") Integer score);

	/** DRAFT → SUBMITTED 전환. STATUS='DRAFT'인 경우에만 UPDATE */
	int submitRun(@Param("runId") long runId, @Param("totalScore") int totalScore);

	Long getFirstChildIdByUserId(@Param("userId") long userId);

	int countRunOwnedByUser(@Param("runId") long runId, @Param("userId") long userId);

	String getRunStatusByIdAndUserId(@Param("runId") long runId, @Param("userId") long userId);

	List<AnswerForStart> getAnswersByRunId(@Param("runId") long runId);

	/**
	 * (userId, childId, checklistId) 기준 DRAFT 상태인 모든 run의 answers를 삭제한다.
	 * discardDraftRuns() 호출 전에 먼저 실행해야 FK 제약 위반이 없다.
	 */
	int deleteAnswersByDraftRuns(
			@Param("userId") long userId,
			@Param("childId") long childId,
			@Param("checklistId") long checklistId);

	/**
	 * (userId, childId, checklistId) 기준 모든 DRAFT run을 DISCARDED 상태로 변경한다.
	 * 이후 getLatestDraftRunId()는 해당 run들을 반환하지 않는다.
	 */
	int discardDraftRuns(
			@Param("userId") long userId,
			@Param("childId") long childId,
			@Param("checklistId") long checklistId);
}
