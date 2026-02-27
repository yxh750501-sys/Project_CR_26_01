package com.example.demo.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.ChecklistStartRepository;
import com.example.demo.vo.AnswerForStart;
import com.example.demo.vo.ChecklistForStart;
import com.example.demo.vo.ChecklistQuestionForStart;

import jakarta.servlet.http.HttpSession;

@Service
public class ChecklistStartService {

	private final ChecklistStartRepository checklistStartRepository;

	public ChecklistStartService(ChecklistStartRepository checklistStartRepository) {
		this.checklistStartRepository = checklistStartRepository;
	}

	// ──────────────────── 체크리스트/문항 조회 ────────────────────

	public ChecklistForStart getChecklist(long checklistId) {
		return checklistStartRepository.getChecklistById(checklistId);
	}

	public List<ChecklistQuestionForStart> getQuestions(long checklistId) {
		return checklistStartRepository.getQuestionsByChecklistId(checklistId);
	}

	// ──────────────────── 아이 선택 ────────────────────

	/**
	 * 세션 키가 여러 가지로 혼재하므로 우선순위 순으로 탐색.
	 * childIdParam(URL) > 세션 > DB 첫 번째 아이 순.
	 */
	public Long resolveChildId(HttpSession session, long userId, Long childIdParam) {
		if (childIdParam != null && childIdParam > 0) {
			return childIdParam;
		}

		for (String key : Arrays.asList(
				"selectedChildId",
				"representativeChildId",
				"repChildId",
				"childId",
				"selectedChildProfileId",
				"loginedChildId")) {
			Long v = toLong(session.getAttribute(key));
			if (v != null && v > 0) return v;
		}

		return checklistStartRepository.getFirstChildIdByUserId(userId);
	}

	// ──────────────────── Run 조회 (생성 없음) ────────────────────

	/**
	 * (userId, childId, checklistId) 기준 가장 최신 DRAFT run ID를 반환한다.
	 * run을 생성하지 않는다. DRAFT가 없으면 null을 반환한다.
	 */
	public Long findLatestDraftRunId(long userId, long childId, long checklistId) {
		return checklistStartRepository.getLatestDraftRunId(userId, childId, checklistId);
	}

	/**
	 * DRAFT run 기본 정보를 반환한다 (임시저장 선택 화면 표시용).
	 * lastSavedAt, childName 등이 포함된다.
	 */
	public Map<String, Object> getDraftRunBasicInfo(long runId) {
		return checklistStartRepository.getRunBasicInfo(runId);
	}

	// ──────────────────── Run 생성 ────────────────────

	/**
	 * 무조건 새 DRAFT run을 생성하고 ID를 반환한다.
	 * 기존 DRAFT 존재 여부와 관계없이 항상 신규 생성한다.
	 * (사용처: DRAFT가 없음을 이미 확인한 후 호출)
	 */
	@Transactional
	public long createNewDraftRun(long userId, long childId, long checklistId) {
		checklistStartRepository.createRun(checklistId, childId, userId);

		Long created = checklistStartRepository.getLatestDraftRunId(userId, childId, checklistId);
		if (created == null || created <= 0) {
			created = checklistStartRepository.getLastInsertId();
		}
		if (created == null || created <= 0) {
			throw new IllegalStateException(
					"DRAFT run 생성에 실패했습니다. (userId=" + userId + ", childId=" + childId + ")");
		}
		return created;
	}

	/**
	 * 같은 (userId, childId, checklistId) 조합의 DRAFT run이 있으면 재사용,
	 * 없으면 새로 생성한다.
	 *
	 * <p>안전성:
	 * <ul>
	 *   <li>@Transactional로 단일 트랜잭션 보장</li>
	 *   <li>createRun() 후 LAST_INSERT_ID() 대신 getLatestDraftRunId()로 재확인 →
	 *       극단적 동시 요청에서도 실제 DB에 저장된 ID를 반환</li>
	 * </ul>
	 */
	@Transactional
	public long getOrCreateDraftRun(long userId, long childId, long checklistId) {
		Long existing = checklistStartRepository.getLatestDraftRunId(userId, childId, checklistId);
		if (existing != null && existing > 0) {
			return existing;
		}

		checklistStartRepository.createRun(checklistId, childId, userId);

		// 방어적으로 getLatestDraftRunId()로 재조회해 실제 저장된 ID를 사용한다.
		Long created = checklistStartRepository.getLatestDraftRunId(userId, childId, checklistId);
		if (created == null || created <= 0) {
			created = checklistStartRepository.getLastInsertId();
		}
		if (created == null || created <= 0) {
			throw new IllegalStateException(
					"DRAFT run 생성에 실패했습니다. (userId=" + userId + ", childId=" + childId + ")");
		}
		return created;
	}

	/**
	 * (userId, childId, checklistId) 기준의 모든 DRAFT run을 폐기하고
	 * 새 DRAFT run을 생성하여 그 ID를 반환한다.
	 *
	 * <p>처리 순서:
	 * <ol>
	 *   <li>해당 DRAFT run의 answers 전체 삭제 (FK 제약 위반 방지)</li>
	 *   <li>DRAFT run들을 DISCARDED 상태로 변경</li>
	 *   <li>새 DRAFT run 생성</li>
	 * </ol>
	 */
	@Transactional
	public long discardAllDraftsAndCreateNew(long userId, long childId, long checklistId) {
		// 1) 기존 DRAFT run의 answers 삭제
		checklistStartRepository.deleteAnswersByDraftRuns(userId, childId, checklistId);
		// 2) 기존 DRAFT run → DISCARDED
		checklistStartRepository.discardDraftRuns(userId, childId, checklistId);
		// 3) 새 DRAFT run 생성
		checklistStartRepository.createRun(checklistId, childId, userId);

		// getLatestDraftRunId는 STATUS='DRAFT'만 조회하므로
		// 방금 DISCARDED 처리 후 새로 만든 run의 ID만 반환된다.
		Long created = checklistStartRepository.getLatestDraftRunId(userId, childId, checklistId);
		if (created == null || created <= 0) {
			created = checklistStartRepository.getLastInsertId();
		}
		if (created == null || created <= 0) {
			throw new IllegalStateException(
					"새 DRAFT run 생성에 실패했습니다. (userId=" + userId + ", childId=" + childId + ")");
		}
		return created;
	}

	// ──────────────────── Run 상태/소유권 확인 ────────────────────

	public boolean isRunOwnedByUser(long runId, long userId) {
		return checklistStartRepository.countRunOwnedByUser(runId, userId) > 0;
	}

	public String getRunStatus(long runId, long userId) {
		return checklistStartRepository.getRunStatusByIdAndUserId(runId, userId);
	}

	/**
	 * form에서 넘어온 checklistId가 run의 실제 checklistId와 일치하는지 검증.
	 * 불일치 시 form 위변조로 간주.
	 */
	public boolean isChecklistIdMatchingRun(long runId, long checklistId) {
		Long actual = checklistStartRepository.getChecklistIdByRunId(runId);
		return actual != null && actual == checklistId;
	}

	// ──────────────────── 답변 조회 ────────────────────

	/**
	 * JSP에서 숫자 키 타입(Integer/Long) 불일치가 자주 발생하므로
	 * 키를 String(questionId 문자열)으로 통일해서 반환한다.
	 *
	 * <p>방어: MyBatis는 보통 emptyList를 반환하지만, null 반환 및
	 * 개별 항목 null을 명시적으로 처리한다.
	 */
	public Map<String, AnswerForStart> getAnswersMap(long runId) {
		List<AnswerForStart> list = checklistStartRepository.getAnswersByRunId(runId);
		Map<String, AnswerForStart> map = new HashMap<>();
		if (list == null) return map;
		for (AnswerForStart a : list) {
			if (a != null && a.getQuestionId() > 0) {
				map.put(String.valueOf(a.getQuestionId()), a);
			}
		}
		return map;
	}

	// ──────────────────── 답변 저장 / 제출 ────────────────────

	@Transactional
	public void saveAnswer(long runId, long questionId,
			String answerValue, String answerText, Integer score) {
		checklistStartRepository.upsertAnswer(runId, questionId, answerValue, answerText, score);
	}

	/**
	 * DRAFT → SUBMITTED 전환.
	 * Mapper SQL에서 STATUS='DRAFT' 조건을 걸므로 이미 제출된 run은 영향받지 않는다.
	 *
	 * @return 실제로 상태가 변경된 rows 수 (0이면 이미 SUBMITTED 또는 존재하지 않음)
	 */
	@Transactional
	public int submitRun(long runId, int totalScore) {
		return checklistStartRepository.submitRun(runId, totalScore);
	}

	// ──────────────────── 유틸 ────────────────────

	private Long toLong(Object v) {
		if (v == null) return null;
		if (v instanceof Number) return ((Number) v).longValue();
		try {
			return Long.parseLong(v.toString());
		} catch (Exception e) {
			return null;
		}
	}
}
