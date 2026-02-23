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
}