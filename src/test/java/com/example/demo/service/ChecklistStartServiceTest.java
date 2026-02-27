package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.repository.ChecklistStartRepository;
import com.example.demo.vo.AnswerForStart;

import jakarta.servlet.http.HttpSession;

/**
 * ChecklistStartService 단위 테스트.
 * DB 불필요 — 모든 의존성을 Mockito로 대체.
 */
@ExtendWith(MockitoExtension.class)
class ChecklistStartServiceTest {

    @Mock
    private ChecklistStartRepository repo;

    @Mock
    private HttpSession mockSession;

    @InjectMocks
    private ChecklistStartService service;

    // ─────────────────────────────────────────────────
    // getAnswersMap
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("getAnswersMap: Repository가 null을 반환하면 빈 Map 반환")
    void getAnswersMap_whenRepoReturnsNull_returnsEmptyMap() {
        when(repo.getAnswersByRunId(1L)).thenReturn(null);

        Map<String, AnswerForStart> result = service.getAnswersMap(1L);

        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("getAnswersMap: 빈 리스트이면 빈 Map 반환")
    void getAnswersMap_whenEmpty_returnsEmptyMap() {
        when(repo.getAnswersByRunId(1L)).thenReturn(Collections.emptyList());

        Map<String, AnswerForStart> result = service.getAnswersMap(1L);

        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("getAnswersMap: 답변이 있으면 questionId(String) 키로 매핑")
    void getAnswersMap_whenAnswersExist_mapsCorrectly() {
        AnswerForStart a1 = makeAnswer(10L, "3");
        AnswerForStart a2 = makeAnswer(20L, "1");
        when(repo.getAnswersByRunId(99L)).thenReturn(List.of(a1, a2));

        Map<String, AnswerForStart> result = service.getAnswersMap(99L);

        assertThat(result).hasSize(2);
        assertThat(result.get("10")).isSameAs(a1);
        assertThat(result.get("20")).isSameAs(a2);
    }

    @Test
    @DisplayName("getAnswersMap: questionId가 0인 항목은 무시")
    void getAnswersMap_whenQuestionIdZero_skipsEntry() {
        AnswerForStart a = makeAnswer(0L, "5");
        when(repo.getAnswersByRunId(1L)).thenReturn(List.of(a));

        Map<String, AnswerForStart> result = service.getAnswersMap(1L);

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────────
    // getOrCreateDraftRun
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("getOrCreateDraftRun: 기존 DRAFT가 있으면 신규 생성 없이 기존 ID 반환")
    void getOrCreateDraftRun_existingDraft_returnsExistingId() {
        when(repo.getLatestDraftRunId(1L, 2L, 3L)).thenReturn(99L);

        long result = service.getOrCreateDraftRun(1L, 2L, 3L);

        assertThat(result).isEqualTo(99L);
        verify(repo, never()).createRun(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("getOrCreateDraftRun: DRAFT 없으면 createRun 후 새 ID 반환")
    void getOrCreateDraftRun_noDraft_createsAndReturnsNewId() {
        // 첫 번째 호출 null(없음), 두 번째 호출 100(생성 후)
        when(repo.getLatestDraftRunId(1L, 2L, 3L))
                .thenReturn(null)
                .thenReturn(100L);
        when(repo.createRun(3L, 2L, 1L)).thenReturn(1);

        long result = service.getOrCreateDraftRun(1L, 2L, 3L);

        assertThat(result).isEqualTo(100L);
        verify(repo).createRun(3L, 2L, 1L);
    }

    @Test
    @DisplayName("getOrCreateDraftRun: createRun 후에도 ID를 조회 못 하면 예외")
    void getOrCreateDraftRun_createFailure_throwsIllegalState() {
        when(repo.getLatestDraftRunId(anyLong(), anyLong(), anyLong())).thenReturn(null);
        when(repo.createRun(anyLong(), anyLong(), anyLong())).thenReturn(0);
        when(repo.getLastInsertId()).thenReturn(0L);

        assertThatThrownBy(() -> service.getOrCreateDraftRun(1L, 2L, 3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DRAFT run 생성에 실패");
    }

    // ─────────────────────────────────────────────────
    // findLatestDraftRunId
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("findLatestDraftRunId: DRAFT가 있으면 해당 ID 반환")
    void findLatestDraftRunId_whenExists_returnsId() {
        when(repo.getLatestDraftRunId(1L, 2L, 3L)).thenReturn(55L);

        Long result = service.findLatestDraftRunId(1L, 2L, 3L);

        assertThat(result).isEqualTo(55L);
    }

    @Test
    @DisplayName("findLatestDraftRunId: DRAFT 없으면 null 반환")
    void findLatestDraftRunId_whenNone_returnsNull() {
        when(repo.getLatestDraftRunId(1L, 2L, 3L)).thenReturn(null);

        Long result = service.findLatestDraftRunId(1L, 2L, 3L);

        assertThat(result).isNull();
    }

    // ─────────────────────────────────────────────────
    // discardAllDraftsAndCreateNew
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("discardAllDraftsAndCreateNew: answers 삭제 → DISCARDED → 새 run 생성 순서로 호출하고 새 ID 반환")
    void discardAllDraftsAndCreateNew_success() {
        when(repo.deleteAnswersByDraftRuns(1L, 2L, 3L)).thenReturn(5);
        when(repo.discardDraftRuns(1L, 2L, 3L)).thenReturn(1);
        when(repo.createRun(3L, 2L, 1L)).thenReturn(1);
        when(repo.getLatestDraftRunId(1L, 2L, 3L)).thenReturn(200L);

        long result = service.discardAllDraftsAndCreateNew(1L, 2L, 3L);

        assertThat(result).isEqualTo(200L);
        verify(repo).deleteAnswersByDraftRuns(1L, 2L, 3L);
        verify(repo).discardDraftRuns(1L, 2L, 3L);
        verify(repo).createRun(3L, 2L, 1L);
    }

    @Test
    @DisplayName("discardAllDraftsAndCreateNew: 새 run ID 조회 실패 시 예외")
    void discardAllDraftsAndCreateNew_createFailure_throwsIllegalState() {
        when(repo.getLatestDraftRunId(anyLong(), anyLong(), anyLong())).thenReturn(null);
        when(repo.getLastInsertId()).thenReturn(0L);

        assertThatThrownBy(() -> service.discardAllDraftsAndCreateNew(1L, 2L, 3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("새 DRAFT run 생성에 실패");
    }

    // ─────────────────────────────────────────────────
    // createNewDraftRun
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("createNewDraftRun: createRun 후 새 ID 반환")
    void createNewDraftRun_success() {
        when(repo.createRun(3L, 2L, 1L)).thenReturn(1);
        when(repo.getLatestDraftRunId(1L, 2L, 3L)).thenReturn(77L);

        long result = service.createNewDraftRun(1L, 2L, 3L);

        assertThat(result).isEqualTo(77L);
        verify(repo).createRun(3L, 2L, 1L);
    }

    @Test
    @DisplayName("createNewDraftRun: ID 조회 실패 시 예외")
    void createNewDraftRun_failure_throwsIllegalState() {
        when(repo.getLatestDraftRunId(anyLong(), anyLong(), anyLong())).thenReturn(null);
        when(repo.getLastInsertId()).thenReturn(0L);

        assertThatThrownBy(() -> service.createNewDraftRun(1L, 2L, 3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DRAFT run 생성에 실패");
    }

    // ─────────────────────────────────────────────────
    // resolveChildId
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("resolveChildId: URL 파라미터가 있으면 그 값 우선 반환")
    void resolveChildId_paramProvided_returnsParam() {
        Long result = service.resolveChildId(mockSession, 1L, 7L);
        assertThat(result).isEqualTo(7L);
    }

    @Test
    @DisplayName("resolveChildId: URL 파라미터 없고 세션에 selectedChildId 있으면 세션 값 반환")
    void resolveChildId_noParam_sessionHasSelectedChildId() {
        when(mockSession.getAttribute("selectedChildId")).thenReturn(3L);

        Long result = service.resolveChildId(mockSession, 1L, null);

        assertThat(result).isEqualTo(3L);
    }

    @Test
    @DisplayName("resolveChildId: 세션도 없으면 DB 첫 번째 아이 ID 반환")
    void resolveChildId_noParamNoSession_returnsFirstChildFromDb() {
        // 모든 세션 키에 대해 null 반환
        when(mockSession.getAttribute(org.mockito.ArgumentMatchers.anyString())).thenReturn(null);
        when(repo.getFirstChildIdByUserId(1L)).thenReturn(5L);

        Long result = service.resolveChildId(mockSession, 1L, null);

        assertThat(result).isEqualTo(5L);
    }

    // ─────────────────────────────────────────────────
    // 헬퍼
    // ─────────────────────────────────────────────────

    private AnswerForStart makeAnswer(long questionId, String value) {
        AnswerForStart a = new AnswerForStart();
        a.setQuestionId(questionId);
        a.setAnswerValue(value);
        return a;
    }
}
