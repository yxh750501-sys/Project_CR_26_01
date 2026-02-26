package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.RunSummaryDto;
import com.example.demo.repository.ChecklistRepository;
import com.example.demo.vo.Center;

/**
 * MyPageService 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @Mock
    private ChecklistRepository checklistRepository;

    @Mock
    private FavoriteService favoriteService;

    @InjectMocks
    private MyPageService myPageService;

    // ── calculateSimpleRiskLevel ──────────────────────────────────────

    @Test
    @DisplayName("totalScore 0 이하 → 빈 문자열(미산정)")
    void calculateSimpleRiskLevel_zeroOrNegative_returnsEmpty() {
        assertThat(myPageService.calculateSimpleRiskLevel(0)).isEmpty();
        assertThat(myPageService.calculateSimpleRiskLevel(-1)).isEmpty();
    }

    @Test
    @DisplayName("totalScore 1~60 → HIGH")
    void calculateSimpleRiskLevel_1to60_returnsHigh() {
        assertThat(myPageService.calculateSimpleRiskLevel(1)).isEqualTo("HIGH");
        assertThat(myPageService.calculateSimpleRiskLevel(60)).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("totalScore 61~90 → MODERATE")
    void calculateSimpleRiskLevel_61to90_returnsModerate() {
        assertThat(myPageService.calculateSimpleRiskLevel(61)).isEqualTo("MODERATE");
        assertThat(myPageService.calculateSimpleRiskLevel(90)).isEqualTo("MODERATE");
    }

    @Test
    @DisplayName("totalScore 91 이상 → LOW")
    void calculateSimpleRiskLevel_91plus_returnsLow() {
        assertThat(myPageService.calculateSimpleRiskLevel(91)).isEqualTo("LOW");
        assertThat(myPageService.calculateSimpleRiskLevel(150)).isEqualTo("LOW");
    }

    // ── getRecentSubmittedRuns ────────────────────────────────────────

    @Test
    @DisplayName("SUBMITTED 목록 반환 시 riskLevel 주입")
    void getRecentSubmittedRuns_populatesRiskLevel() {
        RunSummaryDto dto = new RunSummaryDto();
        dto.setTotalScore(55); // 55 ≤ 60 → HIGH

        when(checklistRepository.getRecentSubmittedRuns(1L, 0L, MyPageService.RECENT_SUBMITTED_LIMIT))
                .thenReturn(List.of(dto));

        List<RunSummaryDto> result = myPageService.getRecentSubmittedRuns(1L, 0L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRiskLevel()).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("repository가 null 반환 → 빈 리스트")
    void getRecentSubmittedRuns_repositoryReturnsNull_returnsEmpty() {
        when(checklistRepository.getRecentSubmittedRuns(1L, 0L, MyPageService.RECENT_SUBMITTED_LIMIT))
                .thenReturn(null);

        assertThat(myPageService.getRecentSubmittedRuns(1L, 0L)).isEmpty();
    }

    // ── getDraftRuns ──────────────────────────────────────────────────

    @Test
    @DisplayName("DRAFT 목록 repository 위임 및 반환")
    void getDraftRuns_delegatesToRepository() {
        RunSummaryDto dto = new RunSummaryDto();
        dto.setStatus("DRAFT");

        when(checklistRepository.getDraftRuns(1L, 0L, MyPageService.DRAFT_LIMIT))
                .thenReturn(List.of(dto));

        List<RunSummaryDto> result = myPageService.getDraftRuns(1L, 0L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("DRAFT");
    }

    @Test
    @DisplayName("repository가 null 반환 → 빈 리스트")
    void getDraftRuns_repositoryReturnsNull_returnsEmpty() {
        when(checklistRepository.getDraftRuns(1L, 0L, MyPageService.DRAFT_LIMIT))
                .thenReturn(null);

        assertThat(myPageService.getDraftRuns(1L, 0L)).isEmpty();
    }

    // ── getFavoriteCenters ────────────────────────────────────────────

    @Test
    @DisplayName("즐겨찾기 5개 → FAVORITE_CENTER_LIMIT(4)개로 제한")
    void getFavoriteCenters_moreThanLimit_returnsLimitOnly() {
        when(favoriteService.getFavoriteCenters(1L))
                .thenReturn(List.of(center(1), center(2), center(3), center(4), center(5)));

        List<Center> result = myPageService.getFavoriteCenters(1L);

        assertThat(result).hasSize(MyPageService.FAVORITE_CENTER_LIMIT);
    }

    @Test
    @DisplayName("즐겨찾기 4개 이하 → 전부 반환")
    void getFavoriteCenters_withinLimit_returnsAll() {
        when(favoriteService.getFavoriteCenters(1L))
                .thenReturn(List.of(center(1), center(2)));

        List<Center> result = myPageService.getFavoriteCenters(1L);

        assertThat(result).hasSize(2);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Center center(long id) {
        Center c = new Center();
        c.setId(id);
        c.setName("센터" + id);
        return c;
    }
}
