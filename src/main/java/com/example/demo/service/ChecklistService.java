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
import com.example.demo.vo.DomainStat;

@Service
public class ChecklistService {

	private final ChecklistRepository checklistRepository;
	private final CenterRepository centerRepository;

	private static final double AVG_SCORE_THRESHOLD = 1.5;
	private static final int TOP_N_DOMAINS = 2;

	public ChecklistService(ChecklistRepository checklistRepository, CenterRepository centerRepository) {
		this.checklistRepository = checklistRepository;
		this.centerRepository = centerRepository;
	}

	public Map<String, Object> getRunInfoForResult(int memberId, int runId) {
		return checklistRepository.getRunInfoForResult(memberId, runId);
	}

	public List<DomainStat> getDomainStatsByRunId(int runId) {
		return checklistRepository.getDomainStatsByRunId(runId);
	}

	public List<String> pickRecommendedDomains(List<DomainStat> stats) {
		if (stats == null || stats.isEmpty()) {
			return Collections.emptyList();
		}

		stats.sort(Comparator.comparingDouble(DomainStat::getAvgScore).reversed());

		List<String> picked = new ArrayList<>();

		for (DomainStat s : stats) {
			if (s.getAvgScore() >= AVG_SCORE_THRESHOLD) {
				picked.add(s.getDomainCode());
				if (picked.size() >= TOP_N_DOMAINS) {
					break;
				}
			}
		}

		if (picked.isEmpty()) {
			picked.add(stats.get(0).getDomainCode());
			if (stats.size() >= 2) {
				picked.add(stats.get(1).getDomainCode());
			}
		}

		return picked;
	}

	public List<Center> getRecommendedCentersByDomains(List<String> domainCodes) {
		if (domainCodes == null || domainCodes.isEmpty()) {
			return Collections.emptyList();
		}
		return centerRepository.getRecommendedCentersByDomains(domainCodes);
	}
}