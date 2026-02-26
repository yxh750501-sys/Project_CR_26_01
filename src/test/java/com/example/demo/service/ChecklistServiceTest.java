package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.repository.CenterRepository;
import com.example.demo.repository.ChecklistRepository;
import com.example.demo.vo.DomainStat;

/**
 * ChecklistService 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

    @Mock
    private ChecklistRepository checklistRepository;

    @Mock
    private CenterRepository centerRepository;

    @InjectMocks
    private ChecklistService service;

    // ─────────────────────────────────────────────────
    // pickRecommendedDomains
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("pickRecommendedDomains: null 입력 → 빈 리스트")
    void pickRecommendedDomains_nullInput_returnsEmpty() {
        assertThat(service.pickRecommendedDomains(null)).isEmpty();
    }

    @Test
    @DisplayName("pickRecommendedDomains: 빈 리스트 → 빈 리스트")
    void pickRecommendedDomains_emptyInput_returnsEmpty() {
        assertThat(service.pickRecommendedDomains(Collections.emptyList())).isEmpty();
    }

    @Test
    @DisplayName("pickRecommendedDomains: avgScore 오름차순 TOP-2 반환")
    void pickRecommendedDomains_sortsAscAndPicksTop2() {
        List<DomainStat> stats = new ArrayList<>();
        stats.add(makeStat("COMMUNICATION",    3.5, 35));
        stats.add(makeStat("MOTOR_FINE",       1.0, 10)); // 약점 1위
        stats.add(makeStat("PLAY_SOCIAL",      2.0, 20)); // 약점 2위
        stats.add(makeStat("BEHAVIOR_EMOTION", 4.0, 40));

        List<String> result = service.pickRecommendedDomains(stats);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo("MOTOR_FINE");
        assertThat(result.get(1)).isEqualTo("PLAY_SOCIAL");
    }

    @Test
    @DisplayName("pickRecommendedDomains: 통계가 1개이면 1개만 반환")
    void pickRecommendedDomains_singleStat_returnsOne() {
        List<DomainStat> stats = new ArrayList<>();
        stats.add(makeStat("SENSORY_DAILY", 1.5, 15));

        List<String> result = service.pickRecommendedDomains(stats);

        assertThat(result).hasSize(1).containsExactly("SENSORY_DAILY");
    }

    @Test
    @DisplayName("pickRecommendedDomains: domainCode null 항목 건너뜀")
    void pickRecommendedDomains_skipsNullDomainCode() {
        List<DomainStat> stats = new ArrayList<>();
        stats.add(makeStat(null, 0.5, 5));           // skip
        stats.add(makeStat("COMMUNICATION", 1.0, 10));
        stats.add(makeStat("SENSORY_DAILY", 2.0, 20));

        List<String> result = service.pickRecommendedDomains(stats);

        assertThat(result).hasSize(2).doesNotContainNull();
    }

    @Test
    @DisplayName("pickRecommendedDomains: 원본 리스트를 수정하지 않음 (defensive copy)")
    void pickRecommendedDomains_doesNotMutateOriginalList() {
        List<DomainStat> original = new ArrayList<>();
        original.add(makeStat("Z", 5.0, 50));
        original.add(makeStat("A", 1.0, 10));
        String firstCodeBefore = original.get(0).getDomainCode();

        service.pickRecommendedDomains(original);

        assertThat(original.get(0).getDomainCode()).isEqualTo(firstCodeBefore);
    }

    @Test
    @DisplayName("pickRecommendedDomains: 동점 시 sumScore 낮은 것 우선")
    void pickRecommendedDomains_tieBreakBySumScore() {
        List<DomainStat> stats = new ArrayList<>();
        stats.add(makeStat("B_CODE", 2.0, 30));
        stats.add(makeStat("A_CODE", 2.0, 10)); // 동점이지만 sumScore 낮음 → 우선
        stats.add(makeStat("C_CODE", 3.0, 20));

        List<String> result = service.pickRecommendedDomains(stats);

        assertThat(result.get(0)).isEqualTo("A_CODE");
    }

    // ─────────────────────────────────────────────────
    // calculateRiskLevel
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("calculateRiskLevel: null 입력 → LOW")
    void calculateRiskLevel_null_returnsLow() {
        assertThat(service.calculateRiskLevel(null)).isEqualTo("LOW");
    }

    @Test
    @DisplayName("calculateRiskLevel: 모든 영역 양호 (avg > 2.0) → LOW")
    void calculateRiskLevel_allGood_returnsLow() {
        List<DomainStat> stats = new ArrayList<>();
        stats.add(makeStat("A", 3.5, 35));
        stats.add(makeStat("B", 4.0, 40));
        assertThat(service.calculateRiskLevel(stats)).isEqualTo("LOW");
    }

    @Test
    @DisplayName("calculateRiskLevel: 1개 영역 avg ≤ 2.0 → MODERATE")
    void calculateRiskLevel_oneHighPriority_returnsModerate() {
        List<DomainStat> stats = new ArrayList<>();
        stats.add(makeStat("A", 1.5, 15));
        stats.add(makeStat("B", 3.0, 30));
        assertThat(service.calculateRiskLevel(stats)).isEqualTo("MODERATE");
    }

    @Test
    @DisplayName("calculateRiskLevel: 2개 영역 avg ≤ 2.0 → MODERATE")
    void calculateRiskLevel_twoHighPriority_returnsModerate() {
        List<DomainStat> stats = new ArrayList<>();
        stats.add(makeStat("A", 1.0, 10));
        stats.add(makeStat("B", 2.0, 20));
        stats.add(makeStat("C", 3.5, 35));
        assertThat(service.calculateRiskLevel(stats)).isEqualTo("MODERATE");
    }

    @Test
    @DisplayName("calculateRiskLevel: 3개 이상 영역 avg ≤ 2.0 → HIGH")
    void calculateRiskLevel_threeHighPriority_returnsHigh() {
        List<DomainStat> stats = new ArrayList<>();
        stats.add(makeStat("A", 1.0, 10));
        stats.add(makeStat("B", 2.0, 20));
        stats.add(makeStat("C", 1.5, 15));
        stats.add(makeStat("D", 3.5, 35));
        assertThat(service.calculateRiskLevel(stats)).isEqualTo("HIGH");
    }

    // ─────────────────────────────────────────────────
    // getRecommendationSummary
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("getRecommendationSummary: 빈 도메인 → 양호 메시지")
    void getRecommendationSummary_emptyDomains_returnsPositiveMessage() {
        Map<String, String> labels = Map.of("COMMUNICATION", "의사소통");
        String msg = service.getRecommendationSummary(Collections.emptyList(), labels);
        assertThat(msg).contains("양호");
    }

    @Test
    @DisplayName("getRecommendationSummary: 도메인 있음 → 라벨 포함 권장 메시지")
    void getRecommendationSummary_withDomains_containsLabels() {
        Map<String, String> labels = Map.of(
                "COMMUNICATION", "의사소통",
                "MOTOR_FINE",    "운동·소근육"
        );
        List<String> domains = new ArrayList<>();
        domains.add("COMMUNICATION");
        domains.add("MOTOR_FINE");
        String msg = service.getRecommendationSummary(domains, labels);
        assertThat(msg).contains("의사소통").contains("운동·소근육");
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
