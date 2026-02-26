package com.example.demo.dto;

import lombok.Data;

/**
 * 내 기록 허브 페이지용 체크리스트 실행 요약 DTO.
 */
@Data
public class RunSummaryDto {

    /** checklist_runs.id */
    private long runId;

    /** children.id */
    private long childId;

    /** children.name */
    private String childName;

    /** checklists.title */
    private String checklistTitle;

    /** total_score (SUBMITTED 시만 유효) */
    private int totalScore;

    /**
     * totalScore 기반 위험 단계 (MyPageService가 주입).
     * SUBMITTED → HIGH / MODERATE / LOW
     * DRAFT     → "" (미산정)
     */
    private String riskLevel;

    /**
     * 표시 날짜: SUBMITTED이면 update_date, DRAFT이면 update_date.
     * SQL에서 DATE_FORMAT으로 YYYY-MM-DD 형식으로 반환.
     */
    private String displayDate;

    /** DRAFT / SUBMITTED */
    private String status;
}
