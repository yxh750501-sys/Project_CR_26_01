package com.example.demo.vo;

import lombok.Data;

@Data
public class ChecklistQuestion {
	private long id;
	private long checklistId;
	private String code;

	private String questionText;
	private String helpText;

	private String responseType;  // YN / SCALE5 / TEXT
	private String optionsJson;

	private int weight;
	private int sortOrder;

	private String regDate;
	private String updateDate;
}
