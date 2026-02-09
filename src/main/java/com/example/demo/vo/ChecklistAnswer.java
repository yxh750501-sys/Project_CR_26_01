package com.example.demo.vo;

import lombok.Data;

@Data
public class ChecklistAnswer {
	private long id;
	private long runId;
	private long questionId;

	private String answerValue; // Y/N, 1~5 등
	private String answerText;  // 주관식 메모
	private Integer score;      // 문항 점수(있으면)

	private String regDate;
	private String updateDate;
}
