package com.example.demo.dto;

import java.util.List;

import com.example.demo.vo.DomainStat;

import lombok.Data;

/**
 * 상담 준비 패키지 응답 DTO.
 *
 * <p>체크리스트 결과(runId)를 기준으로
 * 권장 상담 영역·관찰 근거 문항·전문가 질문 리스트를 묶어 반환한다.
 */
@Data
public class ConsultationPrepDto {

    /** 대상 체크리스트 실행 ID */
    private long runId;

    /**
     * 권장 상담 영역 (avgScore ASC 기준 상위 2개).
     * DomainStat.domainLabel 이 서비스 계층에서 주입되어 있다.
     */
    private List<DomainStat> topDomains;

    /**
     * 관찰 근거 문항 (SCALE5 응답 중 낮은 점수 순 최대 3개).
     * EvidenceItem.domainLabel 이 서비스 계층에서 주입되어 있다.
     */
    private List<EvidenceItem> evidenceItems;

    /**
     * 전문가·상담사에게 여쭤볼 질문 리스트 (5~8개).
     * 영역 기반 템플릿 + 공통 보완으로 구성된다.
     * 문구는 권장/참고/상담 권유 톤만 사용한다.
     */
    private List<String> consultationQuestions;
}
