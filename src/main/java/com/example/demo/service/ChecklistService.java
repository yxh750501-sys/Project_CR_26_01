package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.repository.CenterRepository;
import com.example.demo.repository.ChecklistRepository;
import com.example.demo.vo.Center;
import com.example.demo.vo.ChecklistDomain;
import com.example.demo.vo.DomainStat;

@Service
public class ChecklistService {

	private final ChecklistRepository checklistRepository;
	private final CenterRepository centerRepository;

	private static final int TOP_N_DOMAINS = 2;

	public ChecklistService(ChecklistRepository checklistRepository, CenterRepository centerRepository) {
		this.checklistRepository = checklistRepository;
		this.centerRepository = centerRepository;
	}

	public Map<String, Object> getRunInfoForResult(long memberId, long runId) {
		return checklistRepository.getRunInfoForResult(memberId, runId);
	}

	public List<DomainStat> getDomainStatsByRunId(long runId) {
		List<DomainStat> list = checklistRepository.getDomainStatsByRunId(runId);
		for (DomainStat d : list) {
			d.setDomainLabel(ChecklistDomain.labelOf(d.getDomainCode()));
		}
		return list;
	}

	/**
	 * 낮은 avgScore = 약점 영역 = 우선 추천 대상.
	 *
	 * <p>defensive copy: 호출자가 넘긴 리스트를 수정하지 않는다.
	 */
	public List<String> pickRecommendedDomains(List<DomainStat> stats) {
		if (stats == null || stats.isEmpty()) {
			return Collections.emptyList();
		}

		List<DomainStat> sorted = new ArrayList<>(stats); // defensive copy
		sorted.sort(Comparator.comparingDouble(DomainStat::getAvgScore)
				.thenComparingInt(DomainStat::getSumScore));

		List<String> picked = new ArrayList<>();
		for (DomainStat s : sorted) {
			if (s.getDomainCode() == null) continue;
			picked.add(s.getDomainCode());
			if (picked.size() >= TOP_N_DOMAINS) break;
		}
		return picked;
	}

	public List<Center> getRecommendedCentersByDomains(List<String> domainCodes) {
		if (domainCodes == null || domainCodes.isEmpty()) {
			return Collections.emptyList();
		}
		return centerRepository.getRecommendedCentersByDomains(domainCodes);
	}

	/**
	 * 위험도 계산.
	 *
	 * <p>avgScore &le; 2.0 인 도메인(우선 지원 권장) 수 기준:
	 * <ul>
	 *   <li>3개 이상 → "HIGH"</li>
	 *   <li>1~2개   → "MODERATE"</li>
	 *   <li>0개     → "LOW"</li>
	 * </ul>
	 */
	public String calculateRiskLevel(List<DomainStat> stats) {
		if (stats == null || stats.isEmpty()) return "LOW";
		long highPriorityCnt = stats.stream()
				.filter(s -> s.getAvgScore() > 0 && s.getAvgScore() <= 2.0)
				.count();
		if (highPriorityCnt >= 3) return "HIGH";
		if (highPriorityCnt >= 1) return "MODERATE";
		return "LOW";
	}

	/**
	 * 권장 다음 단계 한 줄 요약 메시지.
	 *
	 * @param recommendedDomains pickRecommendedDomains() 결과
	 * @param labelMap           ChecklistDomain.getLabelMap() 결과
	 */
	public String getRecommendationSummary(List<String> recommendedDomains,
	                                        Map<String, String> labelMap) {
		if (recommendedDomains == null || recommendedDomains.isEmpty()) {
			return "모든 영역에서 양호한 수행을 보이고 있습니다.";
		}
		List<String> labels = new ArrayList<>();
		for (String d : recommendedDomains) {
			labels.add(labelMap.getOrDefault(d, d));
		}
		return String.join(" · ", labels) + " 영역 전문 기관 방문을 권장합니다.";
	}
}
