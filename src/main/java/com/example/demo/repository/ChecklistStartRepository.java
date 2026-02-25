package com.example.demo.repository;

import java.util.List;

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
}
