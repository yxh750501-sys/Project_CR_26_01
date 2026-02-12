package com.example.demo.vo;

import lombok.Data;

@Data
public class RunRecommendationParam {
	private long id;
	private long runId;
	private long recommendationId;
	private String reasonText;
}
