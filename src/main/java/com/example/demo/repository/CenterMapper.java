package com.example.demo.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CenterMapper {

	List<Map<String, Object>> getTherapyTypesByDomain(@Param("domainCode") String domainCode);

	List<Map<String, Object>> findCentersForTherapy(
			@Param("therapyTypeCode") String therapyTypeCode,
			@Param("sido") String sido,
			@Param("sigungu") String sigungu,
			@Param("lat") Double lat,
			@Param("lng") Double lng,
			@Param("radiusKm") Double radiusKm,
			@Param("limit") int limit
	);
}
