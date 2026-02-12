package com.example.demo.vo;

import lombok.Data;

@Data
public class ChecklistRun {
	private long id;
	private long checklistId;
	private long childId;
	private long userId;
	private String status; // DRAFT / SUBMITTED
	private int totalScore;
	private String submittedDate;
	private String regDate;
	private String updateDate;
}
