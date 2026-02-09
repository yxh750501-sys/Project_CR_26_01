package com.example.demo.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RunRecommendationItem {
	private long runRecommendationId;
	private long runId;

	private long recommendationId;
	private String recommendationCode;
	private String recommendationTitle;
	private String recommendationDescription;
	private String recommendationCategory;

	private String reasonText;

	private List<RunRecommendationEvidence> evidences = new ArrayList<>();
}
