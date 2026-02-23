package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.Center;
import com.example.demo.vo.CenterService;
import com.example.demo.vo.TherapyType;

@Mapper
public interface CenterRecommendRepository {
	List<Center> getRecommendedCentersByDomains(@Param("domainCodes") List<String> domainCodes);

	List<CenterService> getCenterServicesByCenterId(@Param("centerId") long centerId);

	List<TherapyType> getTherapyTypesByCenterId(@Param("centerId") long centerId);
}