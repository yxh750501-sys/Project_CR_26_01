package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.*;

@Mapper
public interface ChecklistMapper {

	List<Checklist> getChecklists();

	void insertRun(ChecklistRun run);

	ChecklistRun getRunById(@Param("runId") long runId);

	List<ChecklistQuestion> getQuestionsByChecklistId(@Param("checklistId") long checklistId);

	List<ChecklistAnswer> getAnswersByRunId(@Param("runId") long runId);

	void upsertAnswer(@Param("runId") long runId,
			@Param("questionId") long questionId,
			@Param("answerValue") String answerValue,
			@Param("answerText") String answerText,
			@Param("score") Integer score);

	void submitRun(@Param("runId") long runId, @Param("totalScore") int totalScore);

	List<Recommendation> getAllRecommendations();

	void deleteRunRecommendations(@Param("runId") long runId);

	void insertRunRecommendation(RunRecommendationParam param);

	void insertEvidence(@Param("runRecommendationId") long runRecommendationId,
			@Param("questionId") Long questionId,
			@Param("evidenceText") String evidenceText);

	List<RunRecommendationDto> getRunRecommendations(@Param("runId") long runId);

	List<RunRecommendationEvidenceDto> getEvidencesByRunRecommendationId(@Param("runRecommendationId") long runRecommendationId);

	List<ChecklistRun> getSubmittedRunsByUserAndChild(@Param("userId") long userId, @Param("childId") long childId);
}
