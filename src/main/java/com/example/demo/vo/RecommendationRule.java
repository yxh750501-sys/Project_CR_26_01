package com.example.demo.vo;

import lombok.Data;

@Data
public class RecommendationRule {
	private long id;
	private long recommendationId;
	private long checklistId;

	private String ruleType;   // TOTAL_SCORE_RANGE / ANSWER_MATCH
	private String paramsJson; // JSON 텍스트

	private String regDate;
	private String updateDate;
}
