package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.repository.ChecklistResultRepository;
import com.example.demo.vo.DomainStat;

/**
 * ChecklistResultService 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
class ChecklistResultServiceTest {

    @Mock
    private ChecklistResultRepository repo;

    @InjectMocks
    private ChecklistResultService service;

    // ─────────────────────────────────────────────────
    // pickTopDomains
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("pickTopDomains: null 입력 → 빈 리스트")
    void pickTopDomains_nullInput_returnsEmpty() {
        assertThat(service.pickTopDomains(null, 2)).isEmpty();
    }

    @Test
    @DisplayName("pickTopDomains: 빈 리스트 → 빈 리스트")
    void pickTopDomains_emptyStats_returnsEmpty() {
        assertThat(service.pickTopDomains(Collections.emptyList(), 2)).isEmpty();
    }

    @Test
    @DisplayName("pickTopDomains: avgScore 오름차순 정렬 — 낮은 점수가 먼저")
    void pickTopDomains_sortsByAvgScoreAsc() {
        List<DomainStat> stats = new ArrayList<>();
        stats.add(makeStat("COMMUNICATION",     3.5, 35));
        stats.add(makeStat("SENSORY_DAILY",     1.2, 12));
        stats.add(makeStat("BEHAVIOR_EMOTION",  2.0, 20));

        List<String> result = service.pickTopDomains(stats, 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo("SENSORY_DAILY");    // 가장 낮음
        assertThat(result.get(1)).isEqualTo("BEHAVIOR_EMOTION"); // 두 번째 낮음
    }

    @Test
    @DisplayName("pickTopDomains: topN 제한 준수")
    void pickTopDomains_respectsTopNLimit() {
        List<DomainStat> stats = new ArrayList<>();
        stats.add(makeStat("A", 1.0, 5));
        stats.add(makeStat("B", 2.0, 10));
        stats.add(makeStat("C", 3.0, 15));
        stats.add(makeStat("D", 4.0, 20));

        List<String> result = service.pickTopDomains(stats, 3);

        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("A", "B", "C");
    }

    @Test
    @DisplayName("pickTopDomains: domainCode가 null인 항목은 건너뜀")
    void pickTopDomains_skipsNullCode() {
        List<DomainStat> stats = new ArrayList<>();
        stats.add(makeStat(null,    0.0, 0)); // null → skip
        stats.add(makeStat("MOTOR_FINE", 1.5, 15));

        List<String> result = service.pickTopDomains(stats, 2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo("MOTOR_FINE");
    }

    @Test
    @DisplayName("pickTopDomains: 원본 리스트를 수정하지 않음 (defensive copy)")
    void pickTopDomains_doesNotMutateOriginalList() {
        List<DomainStat> original = new ArrayList<>();
        original.add(makeStat("Z", 3.0, 30));
        original.add(makeStat("A", 1.0, 10));
        String firstCodeBefore = original.get(0).getDomainCode();

        service.pickTopDomains(original, 1);

        // 원본 리스트의 첫 번째 항목이 바뀌지 않아야 한다
        assertThat(original.get(0).getDomainCode()).isEqualTo(firstCodeBefore);
    }

    // ─────────────────────────────────────────────────
    // getTherapyTypeCodesByDomains
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("getTherapyTypeCodesByDomains: 빈 도메인 리스트 → DB 호출 없이 빈 리스트")
    void getTherapyTypeCodesByDomains_emptyDomains_returnsEmpty() {
        List<String> result = service.getTherapyTypeCodesByDomains(Collections.emptyList());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getTherapyTypeCodesByDomains: 중복 코드 제거")
    void getTherapyTypeCodesByDomains_deduplicates() {
        when(repo.getTherapyTypeCodesByDomains(List.of("COMMUNICATION")))
                .thenReturn(List.of("SPEECH", "SPEECH", "COGNITION"));

        List<String> result = service.getTherapyTypeCodesByDomains(List.of("COMMUNICATION"));

        assertThat(result).containsExactly("SPEECH", "COGNITION");
    }

    @Test
    @DisplayName("getTherapyTypeCodesByDomains: null/빈 코드 제거")
    void getTherapyTypeCodesByDomains_removesNullAndBlank() {
        // List.of()는 null 원소를 허용하지 않으므로 Arrays.asList() 사용
        when(repo.getTherapyTypeCodesByDomains(List.of("SENSORY_DAILY")))
                .thenReturn(Arrays.asList("SENSORY", null, "", "  "));

        List<String> result = service.getTherapyTypeCodesByDomains(List.of("SENSORY_DAILY"));

        assertThat(result).containsExactly("SENSORY");
    }

    // ─────────────────────────────────────────────────
    // isSubmitted
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("isSubmitted: DB에서 SUBMITTED 반환 → true")
    void isSubmitted_dbReturnsSubmitted_true() {
        when(repo.getRunStatus(1L)).thenReturn("SUBMITTED");
        assertThat(service.isSubmitted(1L)).isTrue();
    }

    @Test
    @DisplayName("isSubmitted: DB에서 DRAFT 반환 → false")
    void isSubmitted_dbReturnsDraft_false() {
        when(repo.getRunStatus(1L)).thenReturn("DRAFT");
        assertThat(service.isSubmitted(1L)).isFalse();
    }

    @Test
    @DisplayName("isSubmitted: DB에서 null 반환 → false")
    void isSubmitted_dbReturnsNull_false() {
        when(repo.getRunStatus(1L)).thenReturn(null);
        assertThat(service.isSubmitted(1L)).isFalse();
    }

    // ─────────────────────────────────────────────────
    // 헬퍼
    // ─────────────────────────────────────────────────

    private DomainStat makeStat(String code, double avg, int sum) {
        DomainStat d = new DomainStat();
        d.setDomainCode(code);
        d.setAvgScore(avg);
        d.setSumScore(sum);
        return d;
    }
}
