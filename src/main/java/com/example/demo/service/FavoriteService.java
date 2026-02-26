package com.example.demo.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.FavoriteRepository;
import com.example.demo.vo.Center;

/**
 * 즐겨찾기 서비스.
 *
 * <p>모든 public 메서드는 null/비정상 memberId에 대해 방어적으로 처리한다.
 */
@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * 즐겨찾기 토글.
     *
     * <ul>
     *   <li>즐겨찾기 없으면 추가 → {@code true} 반환</li>
     *   <li>이미 즐겨찾기면 해제 → {@code false} 반환</li>
     * </ul>
     *
     * <p>add 는 INSERT IGNORE 이므로 레이스 컨디션에도 안전하다.
     *
     * @return 토글 후 즐겨찾기 상태 (true = 추가됨)
     */
    @Transactional
    public boolean toggle(long memberId, long centerId) {
        if (favoriteRepository.exists(memberId, centerId)) {
            favoriteRepository.remove(memberId, centerId);
            return false;
        }
        favoriteRepository.add(memberId, centerId);
        return true;
    }

    /**
     * 해당 센터가 즐겨찾기인지 확인.
     */
    public boolean isFavorite(long memberId, long centerId) {
        if (memberId <= 0) return false;
        return favoriteRepository.exists(memberId, centerId);
    }

    /**
     * 사용자의 즐겨찾기 센터 ID 집합 반환 (JSP EL contains() 호출용).
     */
    public Set<Long> getFavoriteCenterIds(Long memberId) {
        if (memberId == null || memberId <= 0) return Collections.emptySet();
        List<Long> ids = favoriteRepository.findCenterIdsByMember(memberId);
        return ids == null ? Collections.emptySet() : new HashSet<>(ids);
    }

    /**
     * 사용자의 즐겨찾기 센터 목록 반환 (즐겨찾기 페이지용).
     */
    public List<Center> getFavoriteCenters(Long memberId) {
        if (memberId == null || memberId <= 0) return Collections.emptyList();
        List<Center> result = favoriteRepository.findFavoriteCentersByMember(memberId);
        return result == null ? Collections.emptyList() : result;
    }
}
