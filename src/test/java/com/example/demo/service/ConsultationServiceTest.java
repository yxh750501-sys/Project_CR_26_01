package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.ConsultationPrepDto;
import com.example.demo.dto.EvidenceItem;
import com.example.demo.repository.ChecklistResultRepository;
import com.example.demo.vo.DomainStat;

/**
 * ConsultationService 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    @Mock
    private ChecklistResultService    checklistResultService;

    @Mock
    private ChecklistResultRepository checklistResultRepository;

    @InjectMocks
    private ConsultationService consultationService;

    // ── 소유권 검증 ────────────────────────────────────────────────────

    @Test
    @DisplayName("소유권 불일치 → IllegalArgumentException 발생")
    void getConsultationPrep_notOwned_throwsIllegalArgument() {
        when(checklistResultService.isOwned(99L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> consultationService.getConsultationPrep(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("권한");

        // 소유권 실패 시 이후 DB 조회는 수행하지 않아야 한다
        verify(checklistResultService, never()).getDomainStats(99L);
        verify(checklistResultRepository, never()).getLowestScoringAnswers(99L, 3);
    }

    // ── NPE 방어 (빈 데이터) ───────────────────────────────────────────

    @Test
    @DisplayName("도메인 통계·근거 문항 모두 빈 데이터 → NPE 없이 유효한 DTO 반환")
    void getConsultationPrep_emptyData_returnsValidDtoWithoutNpe() {
        when(checklistResultService.isOwned(1L, 1L)).thenReturn(true);
        when(checklistResultService.getDomainStats(1L)).thenReturn(Collections.emptyList());
        when(checklistResultService.pickTopDomains(Collections.emptyList(), 2))
                .thenReturn(Collections.emptyList());
        when(checklistResultRepository.getLowestScoringAnswers(1L, 3)).thenReturn(null);

        ConsultationPrepDto prep = consultationService.getConsultationPrep(1L, 1L);

        assertThat(prep).isNotNull();
        assertThat(prep.getTopDomains()).isEmpty();
        assertThat(prep.getEvidenceItems()).isEmpty();
        // 도메인이 없으면 공통 질문으로 채워진다 (5개 이상 보장)
        assertThat(prep.getConsultationQuestions()).hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("repository가 null 반환해도 NPE 없이 빈 evidenceItems 처리")
    void getConsultationPrep_repositoryReturnsNull_noNpe() {
        when(checklistResultService.isOwned(2L, 1L)).thenReturn(true);
        when(checklistResultService.getDomainStats(2L)).thenReturn(Collections.emptyList());
        when(checklistResultService.pickTopDomains(Collections.emptyList(), 2))
                .thenReturn(Collections.emptyList());
        when(checklistResultRepository.getLowestScoringAnswers(2L, 3)).thenReturn(null);

        ConsultationPrepDto prep = consultationService.getConsultationPrep(2L, 1L);

        assertThat(prep.getEvidenceItems()).isNotNull().isEmpty();
    }

    // ── 질문 리스트 생성 ──────────────────────────────────────────────

    @Test
    @DisplayName("Top2 도메인 존재 → 5~8개 질문 반환, '진단' 단어 없음")
    void generateQuestions_withTopDomains_returnsBetween5and8_noDiagnosis() {
        List<DomainStat> topDomains = List.of(
                domainStat("COMMUNICATION"),
                domainStat("SENSORY_DAILY")
        );

        List<String> questions = consultationService.generateQuestions(topDomains, List.of());

        assertThat(questions).hasSizeBetween(5, 8);
        assertThat(questions).noneMatch(q -> q.contains("진단"));
        assertThat(questions).noneMatch(q -> q.contains("처방"));
    }

    @Test
    @DisplayName("도메인 없음 → 공통 질문 fallback으로 5개 이상 반환")
    void generateQuestions_emptyDomains_fallbackCommonQuestions() {
        List<String> questions = consultationService.generateQuestions(
                Collections.emptyList(), Collections.emptyList());

        assertThat(questions).hasSizeGreaterThanOrEqualTo(5);
        assertThat(questions).noneMatch(q -> q.contains("진단"));
    }

    @Test
    @DisplayName("알 수 없는 도메인 코드 → 공통 질문으로 보완하여 5개 이상 반환")
    void generateQuestions_unknownDomainCode_usesCommonFallback() {
        List<DomainStat> unknown = List.of(domainStat("UNKNOWN_DOMAIN"));

        List<String> questions = consultationService.generateQuestions(unknown, List.of());

        assertThat(questions).hasSizeGreaterThanOrEqualTo(5);
    }

    // ── 정상 흐름 ────────────────────────────────────────────────────

    @Test
    @DisplayName("정상 흐름 — runId·topDomains·evidenceItems·consultationQuestions 모두 설정")
    void getConsultationPrep_normalFlow_allFieldsSet() {
        DomainStat comm = domainStat("COMMUNICATION");
        DomainStat sens = domainStat("SENSORY_DAILY");
        List<DomainStat> allStats = List.of(comm, sens);

        EvidenceItem ev = new EvidenceItem();
        ev.setQuestionText("아이가 간단한 지시를 따를 수 있나요?");
        ev.setAnswerValue("1");
        ev.setScore(1);
        ev.setDomainCode("COMMUNICATION");

        when(checklistResultService.isOwned(10L, 5L)).thenReturn(true);
        when(checklistResultService.getDomainStats(10L)).thenReturn(allStats);
        when(checklistResultService.pickTopDomains(allStats, 2))
                .thenReturn(List.of("COMMUNICATION", "SENSORY_DAILY"));
        when(checklistResultRepository.getLowestScoringAnswers(10L, 3)).thenReturn(List.of(ev));

        ConsultationPrepDto prep = consultationService.getConsultationPrep(10L, 5L);

        assertThat(prep.getRunId()).isEqualTo(10L);
        assertThat(prep.getTopDomains()).hasSize(2);
        assertThat(prep.getEvidenceItems()).hasSize(1);
        assertThat(prep.getEvidenceItems().get(0).getDomainLabel()).isNotBlank();
        assertThat(prep.getConsultationQuestions()).hasSizeBetween(5, 8);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private DomainStat domainStat(String code) {
        DomainStat s = new DomainStat();
        s.setDomainCode(code);
        s.setDomainLabel(code);
        s.setAvgScore(2.0);
        return s;
    }
}
