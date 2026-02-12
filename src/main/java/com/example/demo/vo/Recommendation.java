package com.example.demo.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Recommendation {
	private long id;
	private String code;
	private String title;
	private String description;
	private String category;
	private LocalDateTime regDate;
	private LocalDateTime updateDate;
}
