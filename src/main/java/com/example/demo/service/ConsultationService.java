package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.demo.dto.ConsultationPrepDto;
import com.example.demo.dto.EvidenceItem;
import com.example.demo.repository.ChecklistResultRepository;
import com.example.demo.vo.ChecklistDomain;
import com.example.demo.vo.DomainStat;

/**
 * 상담 준비 패키지 서비스.
 *
 * <p>체크리스트 결과(runId) 기준으로
 * 권장 상담 영역·관찰 근거 문항·전문가 질문 리스트를 구성한다.
 *
 * <p>원칙: 진단·처방·질병명 표현 금지.
 * 문구는 반드시 '권장 / 참고 / 상담 권유' 톤을 유지한다.
 */
@Service
public class ConsultationService {

    // ── 영역별 전문가 질문 템플릿 ─────────────────────────────────────

    private static final Map<String, List<String>> DOMAIN_QUESTIONS;
    static {
        DOMAIN_QUESTIONS = new LinkedHashMap<>();
        DOMAIN_QUESTIONS.put("COMMUNICATION", List.of(
                "이 연령대에서 기대되는 의사소통 발달 수준을 참고할 수 있을까요?",
                "언어 이해와 표현 중 먼저 살펴보면 좋을 부분이 있다면 안내해 주실 수 있나요?",
                "가정에서 언어 발달을 지원하기 위해 권장하는 활동이나 방법이 있을까요?"
        ));
        DOMAIN_QUESTIONS.put("SENSORY_DAILY", List.of(
                "감각 처리 방식이 일상생활에 미치는 영향을 어떻게 참고하면 좋을까요?",
                "일상 루틴 구성 시 감각 특성을 반영하는 방법을 안내해 주실 수 있나요?",
                "감각통합 분야 전문 상담이 도움이 될 수 있는지 여쭤봐도 될까요?"
        ));
        DOMAIN_QUESTIONS.put("BEHAVIOR_EMOTION", List.of(
                "반복적으로 나타나는 행동 패턴에 대해 전문가 관찰을 요청할 수 있을까요?",
                "감정 조절 지원을 위한 가정 내 전략을 권장해 주실 수 있나요?",
                "현재 관찰된 행동 특성이 이 나이에서 일반적인 범위인지 참고하고 싶습니다."
        ));
        DOMAIN_QUESTIONS.put("MOTOR_FINE", List.of(
                "소근육 발달을 위해 권장되는 일상 활동이나 놀이를 안내해 주실 수 있나요?",
                "조작 활동에서 어려움을 보이는 부분을 작업치료 관점에서 살펴볼 수 있을까요?",
                "가정에서 소근육 발달을 지원하는 방법을 권장해 주시면 좋겠습니다."
        ));
        DOMAIN_QUESTIONS.put("PLAY_SOCIAL", List.of(
                "또래와의 상호작용 발달 수준을 전문가 관점에서 참고할 수 있을까요?",
                "사회성 향상을 위한 소그룹 활동이나 프로그램을 권장해 주실 수 있나요?",
                "놀이 상황에서 관찰된 특성에 대해 전문 상담을 받아보면 어떨까요?"
        ));
    }

    /** 영역별 질문이 부족할 때 보완하는 공통 질문 풀 */
    private static final List<String> COMMON_QUESTIONS = List.of(
            "현재 관찰된 발달 특성에 대해 전문가 상담을 받기 위한 첫 단계를 안내해 주실 수 있나요?",
            "가정에서 아이를 더 잘 지원하기 위해 보호자가 참고할 수 있는 자료를 추천해 주실 수 있나요?",
            "이 연령대에서 기대되는 전반적인 발달 수준을 참고할 수 있을까요?",
            "정기적인 발달 확인이나 전문 상담을 권장하는 주기가 있을까요?",
            "아이의 강점을 더 발전시킬 수 있는 활동이나 환경을 안내해 주실 수 있나요?"
    );

    // ─────────────────────────────────────────────────────────────────

    private final ChecklistResultService    checklistResultService;
    private final ChecklistResultRepository checklistResultRepository;

    public ConsultationService(ChecklistResultService checklistResultService,
                                ChecklistResultRepository checklistResultRepository) {
        this.checklistResultService    = checklistResultService;
        this.checklistResultRepository = checklistResultRepository;
    }

    /**
     * 상담 준비 패키지를 생성하여 반환한다.
     *
     * @param runId    체크리스트 실행 ID
     * @param memberId 로그인 사용자 ID (소유권 검증에 사용)
     * @throws IllegalArgumentException 소유권 불일치 시
     */
    public ConsultationPrepDto getConsultationPrep(long runId, long memberId) {
        // 1. 소유권 검증
        if (!checklistResultService.isOwned(runId, memberId)) {
            throw new IllegalArgumentException("결과 확인 권한이 없습니다.");
        }

        // 2. 도메인 점수 조회 → Top2 (avgScore ASC = 약점 우선)
        List<DomainStat> allStats = checklistResultService.getDomainStats(runId);
        if (allStats == null) allStats = Collections.emptyList();

        List<String> topCodes = checklistResultService.pickTopDomains(allStats, 2);
        List<DomainStat> topDomains = buildTopDomains(allStats, topCodes);

        // 3. 관찰 근거 문항 조회 (SCALE5 낮은 점수 순 최대 3건)
        List<EvidenceItem> evidenceItems = checklistResultRepository.getLowestScoringAnswers(runId, 3);
        if (evidenceItems == null) evidenceItems = Collections.emptyList();
        for (EvidenceItem item : evidenceItems) {
            item.setDomainLabel(ChecklistDomain.labelOf(item.getDomainCode()));
        }

        // 4. 질문 리스트 생성 (5~8개)
        List<String> questions = generateQuestions(topDomains, evidenceItems);

        // 5. DTO 조립
        ConsultationPrepDto prep = new ConsultationPrepDto();
        prep.setRunId(runId);
        prep.setTopDomains(topDomains);
        prep.setEvidenceItems(evidenceItems);
        prep.setConsultationQuestions(questions);
        return prep;
    }

    /**
     * allStats 중 topCodes 순서에 맞는 DomainStat 목록을 반환한다.
     */
    private List<DomainStat> buildTopDomains(List<DomainStat> allStats, List<String> topCodes) {
        List<DomainStat> result = new ArrayList<>();
        for (String code : topCodes) {
            for (DomainStat s : allStats) {
                if (code.equals(s.getDomainCode())) {
                    result.add(s);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 영역 기반 템플릿 + 공통 보완으로 5~8개 질문을 생성한다.
     *
     * <p>생성 원칙:
     * <ol>
     *   <li>Top2 영역 각 3개 질문 → 최대 6개</li>
     *   <li>5개 미만이면 공통 질문으로 보완</li>
     *   <li>최대 8개로 제한</li>
     * </ol>
     * <p>모든 문구는 권장/참고/상담 권유 톤을 유지한다.
     */
    List<String> generateQuestions(List<DomainStat> topDomains, List<EvidenceItem> evidenceItems) {
        Set<String> questionSet = new LinkedHashSet<>();

        // 영역별 질문 (최대 6개)
        for (DomainStat domain : topDomains) {
            List<String> dqs = DOMAIN_QUESTIONS.getOrDefault(
                    domain.getDomainCode(), Collections.emptyList());
            for (String q : dqs) {
                questionSet.add(q);
                if (questionSet.size() >= 6) break;
            }
            if (questionSet.size() >= 6) break;
        }

        // 공통 질문으로 5개 미만 보완
        for (String cq : COMMON_QUESTIONS) {
            if (questionSet.size() >= 5) break;
            questionSet.add(cq);
        }

        List<String> result = new ArrayList<>(questionSet);
        return result.size() > 8 ? result.subList(0, 8) : result;
    }
}
