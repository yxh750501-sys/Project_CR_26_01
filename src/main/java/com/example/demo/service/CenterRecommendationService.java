package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.demo.repository.CenterMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CenterRecommendationService {

	private final CenterMapper centerMapper;

	public List<Map<String, Object>> buildTherapyBlocksFromRunRecommendations(
			List<Map<String, Object>> runRecommendations,
			String sido,
			String sigungu,
			Double lat,
			Double lng,
			Double radiusKm,
			int therapyTypeLimit,
			int centerLimitPerTherapy
	) {

		List<Map<String, Object>> blocks = new ArrayList<>();
		if (runRecommendations == null || runRecommendations.isEmpty()) return blocks;

		// runRecommendations의 category = 도메인 코드로 사용(COMMUNICATION/SENSORY_DAILY/...)
		Set<String> usedTherapyTypeCodes = new HashSet<>();

		for (Map<String, Object> rr : runRecommendations) {
			String domainCode = s(rr.get("category")).trim();
			if (domainCode.isEmpty()) continue;

			// 도메인별 치료타입 TOP N
			List<Map<String, Object>> therapyTypes = centerMapper.getTherapyTypesByDomain(domainCode);
			if (therapyTypes == null || therapyTypes.isEmpty()) continue;

			int picked = 0;
			for (Map<String, Object> tt : therapyTypes) {
				if (picked >= therapyTypeLimit) break;

				String therapyTypeCode = s(tt.get("therapyTypeCode")).trim();
				if (therapyTypeCode.isEmpty()) continue;

				// 같은 치료타입이 여러 도메인에서 중복 노출되는 걸 막고 싶으면 여기서 중복 제거
				if (usedTherapyTypeCodes.contains(therapyTypeCode)) continue;

				List<Map<String, Object>> centers = centerMapper.findCentersForTherapy(
						therapyTypeCode,
						blankToNull(sido),
						blankToNull(sigungu),
						lat, lng, radiusKm,
						centerLimitPerTherapy
				);

				tt.put("centers", centers);
				tt.put("domainTitle", domainTitle(domainCode));

				blocks.add(tt);
				usedTherapyTypeCodes.add(therapyTypeCode);
				picked++;
			}
		}

		return blocks;
	}

	private String domainTitle(String code) {
		if ("COMMUNICATION".equalsIgnoreCase(code)) return "의사소통(표현/이해)";
		if ("SENSORY_DAILY".equalsIgnoreCase(code)) return "감각·일상(양치/옷/식사/과민)";
		if ("BEHAVIOR_EMOTION".equalsIgnoreCase(code)) return "행동·정서(전환/폭발/위험)";
		if ("MOTOR_FINE".equalsIgnoreCase(code)) return "운동·미세(연필/가위/협응)";
		if ("PLAY_SOCIAL".equalsIgnoreCase(code)) return "놀이·사회(공동주의/또래)";
		return "기능 영역";
	}

	private String s(Object v) {
		return v == null ? "" : String.valueOf(v);
	}

	private String blankToNull(String v) {
		if (v == null) return null;
		String t = v.trim();
		return t.isEmpty() ? null : t;
	}
}
