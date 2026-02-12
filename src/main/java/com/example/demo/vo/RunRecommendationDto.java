package com.example.demo.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RunRecommendationDto {
	private long id;
	private long runId;
	private long recommendationId;

	private String recommendationCode;
	private String recommendationTitle;
	private String category;
	private String description;

	private String reasonText;
	private LocalDateTime regDate;

	private List<RunRecommendationEvidenceDto> evidences = new ArrayList<>();
}
