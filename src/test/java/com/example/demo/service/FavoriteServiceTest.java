package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.repository.FavoriteRepository;
import com.example.demo.vo.Center;

/**
 * FavoriteService 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    // ── toggle ────────────────────────────────────────────────

    @Test
    @DisplayName("toggle: 즐겨찾기 없으면 add 호출 → true 반환")
    void toggle_notFavorited_addsAndReturnsTrue() {
        when(favoriteRepository.exists(1L, 10L)).thenReturn(false);

        boolean result = favoriteService.toggle(1L, 10L);

        assertThat(result).isTrue();
        verify(favoriteRepository).add(1L, 10L);
        verify(favoriteRepository, never()).remove(anyLong(), anyLong());
    }

    @Test
    @DisplayName("toggle: 이미 즐겨찾기면 remove 호출 → false 반환")
    void toggle_alreadyFavorited_removesAndReturnsFalse() {
        when(favoriteRepository.exists(1L, 10L)).thenReturn(true);

        boolean result = favoriteService.toggle(1L, 10L);

        assertThat(result).isFalse();
        verify(favoriteRepository).remove(1L, 10L);
        verify(favoriteRepository, never()).add(anyLong(), anyLong());
    }

    // ── getFavoriteCenterIds ──────────────────────────────────

    @Test
    @DisplayName("getFavoriteCenterIds: 정상 반환 → Set 변환")
    void getFavoriteCenterIds_returnsSetOfIds() {
        when(favoriteRepository.findCenterIdsByMember(1L)).thenReturn(List.of(10L, 20L, 30L));

        Set<Long> result = favoriteService.getFavoriteCenterIds(1L);

        assertThat(result).containsExactlyInAnyOrder(10L, 20L, 30L);
    }

    @Test
    @DisplayName("getFavoriteCenterIds: null/0 memberId → 빈 Set")
    void getFavoriteCenterIds_invalidMember_returnsEmpty() {
        assertThat(favoriteService.getFavoriteCenterIds(null)).isEmpty();
        assertThat(favoriteService.getFavoriteCenterIds(0L)).isEmpty();
        verify(favoriteRepository, never()).findCenterIdsByMember(anyLong());
    }

    // ── getFavoriteCenters ────────────────────────────────────

    @Test
    @DisplayName("getFavoriteCenters: repository 위임 및 결과 반환")
    void getFavoriteCenters_delegatesAndReturns() {
        Center c1 = new Center(); c1.setId(10L); c1.setName("언어발달센터");
        Center c2 = new Center(); c2.setId(20L); c2.setName("감각통합연구소");
        when(favoriteRepository.findFavoriteCentersByMember(1L)).thenReturn(List.of(c1, c2));

        List<Center> result = favoriteService.getFavoriteCenters(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("언어발달센터");
    }

    @Test
    @DisplayName("getFavoriteCenters: null memberId → 빈 리스트")
    void getFavoriteCenters_nullMember_returnsEmpty() {
        assertThat(favoriteService.getFavoriteCenters(null)).isEmpty();
        verify(favoriteRepository, never()).findFavoriteCentersByMember(anyLong());
    }
}
