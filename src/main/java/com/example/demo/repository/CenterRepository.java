package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.Center;
import com.example.demo.vo.CenterService;
import com.example.demo.vo.TherapyType;

@Mapper
public interface CenterRepository {
	Center getCenterById(int id);

	List<Center> getCenters();

	List<Center> searchCenters(@Param("region") String region, @Param("keyword") String keyword);

	int insertCenter(Center center);

	int updateCenter(Center center);

	int deleteCenter(int id);

	List<CenterService> getCenterServicesByCenterId(@Param("centerId") int centerId);

	int insertCenterService(CenterService centerService);

	int updateCenterService(CenterService centerService);

	int deleteCenterService(@Param("id") int id);

	int deleteCenterServicesByCenterId(@Param("centerId") int centerId);

	List<TherapyType> getTherapyTypes();

	List<TherapyType> getTherapyTypesByCenterId(@Param("centerId") int centerId);

	List<Center> getRecommendedCentersByDomain(@Param("domainCode") String domainCode);

	List<Center> getRecommendedCentersByDomains(@Param("list") List<String> domainCodes);

	/** 필터 조합에 해당하는 센터 총 수 (페이지네이션 계산용). */
	int countCentersFiltered(@Param("keyword") String keyword,
	                          @Param("sido") String sido,
	                          @Param("domain") String domain);

	/** 필터 + 페이지네이션 센터 목록 (therapyTypeCodes 포함 평탄 결과). */
	List<Center> searchCentersFiltered(@Param("keyword") String keyword,
	                                    @Param("sido") String sido,
	                                    @Param("domain") String domain,
	                                    @Param("offset") int offset,
	                                    @Param("size") int size);
}