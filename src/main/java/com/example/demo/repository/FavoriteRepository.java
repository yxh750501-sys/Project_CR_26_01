package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.Center;

@Mapper
public interface FavoriteRepository {

    /** 즐겨찾기 추가. UNIQUE 제약 위반 시 silently 무시(INSERT IGNORE). */
    void add(@Param("memberId") long memberId, @Param("centerId") long centerId);

    /** 즐겨찾기 해제. */
    void remove(@Param("memberId") long memberId, @Param("centerId") long centerId);

    /** 즐겨찾기 여부 확인. */
    boolean exists(@Param("memberId") long memberId, @Param("centerId") long centerId);

    /** 사용자의 즐겨찾기 센터 ID 목록. */
    List<Long> findCenterIdsByMember(@Param("memberId") long memberId);

    /** 사용자의 즐겨찾기 센터 전체 조회 (기관 정보 + 치료타입 코드 포함). */
    List<Center> findFavoriteCentersByMember(@Param("memberId") long memberId);
}
