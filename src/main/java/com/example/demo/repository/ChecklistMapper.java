package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.Checklist;
import com.example.demo.vo.ChecklistAnswer;
import com.example.demo.vo.ChecklistQuestion;
import com.example.demo.vo.ChecklistRun;
import com.example.demo.vo.RecommendationRule;
import com.example.demo.vo.RunRecommendationEvidence;
import com.example.demo.vo.RunRecommendationItem;

@Mapper
public interface ChecklistMapper {

	List<Checklist> getChecklists();

	Checklist getChecklistById(@Param("id") long id);

	List<ChecklistQuestion> getQuestionsByChecklistId(@Param("checklistId") long checklistId);

	int insertRun(@Param("checklistId") long checklistId,
			@Param("childId") long childId,
			@Param("userId") long userId);

	long getLastInsertId();

	ChecklistRun getRunByIdAndUserId(@Param("id") long id, @Param("userId") long userId);

	List<ChecklistAnswer> getAnswersByRunId(@Param("runId") long runId);

	int upsertAnswer(@Param("runId") long runId,
			@Param("questionId") long questionId,
			@Param("answerValue") String answerValue,
			@Param("answerText") String answerText,
			@Param("score") Integer score);

	int updateRunTotalScore(@Param("runId") long runId,
			@Param("userId") long userId,
			@Param("totalScore") int totalScore);

	int submitRun(@Param("runId") long runId,
			@Param("userId") long userId,
			@Param("totalScore") int totalScore);

	List<RecommendationRule> getRecommendationRulesByChecklistId(@Param("checklistId") long checklistId);

	int deleteRunRecommendationsByRunId(@Param("runId") long runId);

	int insertRunRecommendation(@Param("runId") long runId,
			@Param("recommendationId") long recommendationId,
			@Param("reasonText") String reasonText);

	int insertRunRecommendationEvidence(@Param("runRecommendationId") long runRecommendationId,
			@Param("questionId") Long questionId,
			@Param("evidenceText") String evidenceText);

	List<RunRecommendationItem> getRunRecommendationItems(@Param("runId") long runId);

	List<RunRecommendationEvidence> getRunRecommendationEvidences(@Param("runId") long runId);
}
