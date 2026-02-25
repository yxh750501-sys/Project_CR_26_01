package com.example.demo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.demo.repository.ChecklistResultRepository;
import com.example.demo.vo.Center;
import com.example.demo.vo.ChecklistDomain;
import com.example.demo.vo.DomainStat;

@Service
public class ChecklistResultService {

	private final ChecklistResultRepository checklistResultRepository;

	public ChecklistResultService(ChecklistResultRepository checklistResultRepository) {
		this.checklistResultRepository = checklistResultRepository;
	}

	public boolean isOwned(long runId, long userId) {
		return checklistResultRepository.countRunOwnedByUser(runId, userId) > 0;
	}

	public boolean isSubmitted(long runId) {
		String status = checklistResultRepository.getRunStatus(runId);
		return status != null && "SUBMITTED".equalsIgnoreCase(status);
	}

	public List<DomainStat> getDomainStats(long runId) {
		List<DomainStat> list = checklistResultRepository.getDomainStatsByRunId(runId);
		for (DomainStat d : list) {
			d.setDomainLabel(ChecklistDomain.labelOf(d.getDomainCode()));
		}
		return list;
	}

	/**
	 * 낮은 avgScore = 약점 영역 = 우선 추천 대상.
	 *
	 * <p>defensive copy: 호출자가 넘긴 리스트를 수정하지 않는다.
	 * (List.of() 등 불변 리스트를 전달해도 UnsupportedOperationException이 발생하지 않음)
	 */
	public List<String> pickTopDomains(List<DomainStat> stats, int topN) {
		if (stats == null || stats.isEmpty()) return List.of();

		List<DomainStat> sorted = new ArrayList<>(stats); // defensive copy
		sorted.sort(Comparator
				.comparingDouble(DomainStat::getAvgScore)
				.thenComparingInt(DomainStat::getSumScore));

		List<String> res = new ArrayList<>();
		for (DomainStat s : sorted) {
			if (s.getDomainCode() == null) continue;
			res.add(s.getDomainCode());
			if (res.size() >= topN) break;
		}
		return res;
	}

	public List<String> getTherapyTypeCodesByDomains(List<String> domains) {
		if (domains == null || domains.isEmpty()) return List.of();

		List<String> codes = checklistResultRepository.getTherapyTypeCodesByDomains(domains);

		// 중복 제거 + 순서 유지
		Set<String> set = new LinkedHashSet<>();
		for (String c : codes) {
			if (c != null && !c.isBlank()) set.add(c);
		}
		return new ArrayList<>(set);
	}

	public List<Center> getCentersByTherapyTypeCodes(List<String> therapyTypeCodes) {
		if (therapyTypeCodes == null || therapyTypeCodes.isEmpty()) return List.of();
		return checklistResultRepository.getCentersByTherapyTypeCodes(therapyTypeCodes);
	}
}
