package com.example.demo.vo;

import lombok.Data;

@Data
public class RunRecommendationEvidence {
	private long id;
	private long runRecommendationId;
	private Long questionId;     // NULL 가능
	private String evidenceText;

	private String regDate;
	private String updateDate;
}
