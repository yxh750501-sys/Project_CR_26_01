package com.example.demo.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.RunSummaryDto;
import com.example.demo.repository.ChecklistRepository;
import com.example.demo.vo.Center;

/**
 * 내 기록 허브 페이지(/usr/my) 전용 서비스.
 */
@Service
public class MyPageService {

    static final int RECENT_SUBMITTED_LIMIT = 3;
    static final int DRAFT_LIMIT            = 3;
    static final int FAVORITE_CENTER_LIMIT  = 4;

    private final ChecklistRepository checklistRepository;
    private final FavoriteService     favoriteService;

    public MyPageService(ChecklistRepository checklistRepository,
                         FavoriteService favoriteService) {
        this.checklistRepository = checklistRepository;
        this.favoriteService     = favoriteService;
    }

    /**
     * 최근 SUBMITTED 실행 목록 (최대 3건).
     * riskLevel을 totalScore 기반으로 산정하여 주입한다.
     *
     * @param userId  로그인 사용자 ID
     * @param childId 0이면 전체, 양수이면 해당 아이만
     */
    public List<RunSummaryDto> getRecentSubmittedRuns(long userId, long childId) {
        List<RunSummaryDto> runs =
                checklistRepository.getRecentSubmittedRuns(userId, childId, RECENT_SUBMITTED_LIMIT);
        if (runs == null) return Collections.emptyList();
        for (RunSummaryDto run : runs) {
            run.setRiskLevel(calculateSimpleRiskLevel(run.getTotalScore()));
        }
        return runs;
    }

    /**
     * 최근 DRAFT 실행 목록 (최대 3건).
     *
     * @param userId  로그인 사용자 ID
     * @param childId 0이면 전체, 양수이면 해당 아이만
     */
    public List<RunSummaryDto> getDraftRuns(long userId, long childId) {
        List<RunSummaryDto> runs =
                checklistRepository.getDraftRuns(userId, childId, DRAFT_LIMIT);
        return runs == null ? Collections.emptyList() : runs;
    }

    /**
     * 즐겨찾기 센터 목록 (최대 4개를 허브에 표시).
     *
     * @param userId 로그인 사용자 ID
     */
    public List<Center> getFavoriteCenters(long userId) {
        List<Center> all = favoriteService.getFavoriteCenters((long) userId);
        if (all == null || all.isEmpty()) return Collections.emptyList();
        return all.size() > FAVORITE_CENTER_LIMIT ? all.subList(0, FAVORITE_CENTER_LIMIT) : all;
    }

    /**
     * totalScore로 위험 단계를 간략 산정한다.
     *
     * <p>기준: 30문항 SCALE5(가중치 1 기준) 총점 범위 30~150.
     * 낮을수록 취약 영역이 많음을 의미한다.
     *
     * <ul>
     *   <li>0 이하   → "" (미산정)</li>
     *   <li>≤ 60    → HIGH</li>
     *   <li>≤ 90    → MODERATE</li>
     *   <li>&gt; 90 → LOW</li>
     * </ul>
     */
    String calculateSimpleRiskLevel(int totalScore) {
        if (totalScore <= 0) return "";
        if (totalScore <= 60) return "HIGH";
        if (totalScore <= 90) return "MODERATE";
        return "LOW";
    }
}
