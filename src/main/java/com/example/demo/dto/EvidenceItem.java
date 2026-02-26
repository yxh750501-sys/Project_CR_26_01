package com.example.demo.dto;

import lombok.Data;

/**
 * 상담 준비 패키지 — 관찰 근거 문항.
 *
 * <p>체크리스트 응답 중 낮은 점수(어려움)를 보인 문항을 담는다.
 * 질병·장애 판단이 아닌 관찰 사실 전달 목적으로만 사용한다.
 */
@Data
public class EvidenceItem {

    /** 문항 텍스트 (checklist_questions.question_text) */
    private String questionText;

    /** 보호자가 선택한 응답 값 문자열 ("1"~"5") */
    private String answerValue;

    /** 가중치 적용 후 저장된 점수 (checklist_answers.score) */
    private int score;

    /** 도메인 코드 (COMMUNICATION 등) */
    private String domainCode;

    /** 도메인 표시 라벨 — 서비스 계층에서 주입 */
    private String domainLabel;
}
