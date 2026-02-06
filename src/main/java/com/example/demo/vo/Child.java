package com.example.demo.vo;

import java.time.LocalDate;
import lombok.Data;

@Data
public class Child {
    private long id;
    private long userId;

    private String name;
    private LocalDate birthDate; // DATE
    private String gender;       // M/F/U
    private String note;

    private String regDate;
    private String updateDate;
}
