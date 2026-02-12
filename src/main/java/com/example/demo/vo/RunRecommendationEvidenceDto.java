package com.example.demo.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RunRecommendationEvidenceDto {
	private long id;
	private long runRecommendationId;
	private Long questionId;
	private String questionCode;
	private String evidenceText;
	private LocalDateTime regDate;
}
