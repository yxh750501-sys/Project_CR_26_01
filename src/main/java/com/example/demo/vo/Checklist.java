package com.example.demo.vo;

import lombok.Data;

@Data
public class Checklist {
	private long id;
	private String code;
	private String title;
	private String description;
	private String regDate;
	private String updateDate;
}
