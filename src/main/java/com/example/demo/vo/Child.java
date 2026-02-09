package com.example.demo.vo;

import lombok.Data;

@Data
public class Child {
	private long id;
	private long userId;

	private String name;
	private String birthDate; // YYYY-MM-DD
	private String gender;    // M/F/U
	private String note;

	private String regDate;
	private String updateDate;
}
