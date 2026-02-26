package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.form.PostForm;
import com.example.demo.repository.PostFileRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.vo.Post;
import com.example.demo.vo.PostFile;

/**
 * PostService 단위 테스트.
 *
 * <p>DB, Spring Context 불필요 — Mockito 만 사용.
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock PostRepository     postRepository;
    @Mock PostFileRepository postFileRepository;
    @Mock FileStorageService fileStorageService;

    @InjectMocks PostService postService;

    // ── create() ────────────────────────────────────────────────

    @Test
    @DisplayName("create: 게시글 등록 후 첨부 파일 저장 → postId 반환")
    void create_normalFlow_insertsPostAndFiles() {
        PostForm form = makeForm("프로그램 제목", "내용입니다.");
        MockMultipartFile file = new MockMultipartFile(
                "files", "test.pdf", "application/pdf", "data".getBytes());

        PostFile storedFile = new PostFile();
        storedFile.setPostId(10L);
        storedFile.setOrigName("test.pdf");
        storedFile.setStoredName("uuid.pdf");
        storedFile.setFileSize(4L);

        when(postRepository.getLastInsertId()).thenReturn(10L);
        when(fileStorageService.store(any(MultipartFile.class), eq(10L))).thenReturn(storedFile);

        long postId = postService.create(1L, "PROGRAM", form, new MultipartFile[]{file});

        assertThat(postId).isEqualTo(10L);
        verify(postRepository).insertPost(any(Post.class));
        verify(fileStorageService).store(any(MultipartFile.class), eq(10L));
        verify(postFileRepository).insertFile(storedFile);
    }

    @Test
    @DisplayName("create: 첨부 파일 없으면 파일 저장 미호출")
    void create_noFiles_skipFileStorage() {
        PostForm form = makeForm("제목", "내용");
        when(postRepository.getLastInsertId()).thenReturn(5L);

        postService.create(1L, "FREE", form, null);

        verify(postRepository).insertPost(any(Post.class));
        verify(fileStorageService, never()).store(any(), anyLong());
        verify(postFileRepository, never()).insertFile(any());
    }

    // ── update() ────────────────────────────────────────────────

    @Test
    @DisplayName("update: 소유자 불일치 → IllegalArgumentException")
    void update_notOwner_throwsException() {
        Post existing = makePost(99L, 999L); // memberId=999
        when(postRepository.findById(99L)).thenReturn(existing);

        PostForm form = makeForm("수정 제목", "수정 내용");

        assertThatThrownBy(() -> postService.update(1L, 99L, form, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수정 권한");

        verify(postRepository, never()).updateById(any());
    }

    @Test
    @DisplayName("update: 소유자 일치 → 게시글 수정 처리")
    void update_owner_updatesPost() {
        Post existing = makePost(10L, 1L); // memberId=1
        when(postRepository.findById(10L)).thenReturn(existing);
        when(postFileRepository.findByPostId(10L)).thenReturn(Collections.emptyList());

        PostForm form = makeForm("수정된 제목", "수정된 내용");
        postService.update(1L, 10L, form, null);

        verify(postRepository).updateById(any(Post.class));
    }

    @Test
    @DisplayName("update: 파일 5개 초과 → IllegalArgumentException")
    void update_tooManyFiles_throwsException() {
        Post existing = makePost(10L, 1L);
        when(postRepository.findById(10L)).thenReturn(existing);

        // 기존 파일 4개
        List<PostFile> existingFiles = List.of(
                new PostFile(), new PostFile(), new PostFile(), new PostFile());
        when(postFileRepository.findByPostId(10L)).thenReturn(existingFiles);

        // 새 파일 2개 → 합계 6개 초과
        MultipartFile[] newFiles = {
                new MockMultipartFile("f1", "a.pdf", "application/pdf", "d".getBytes()),
                new MockMultipartFile("f2", "b.pdf", "application/pdf", "d".getBytes())
        };
        PostForm form = makeForm("제목", "내용");

        assertThatThrownBy(() -> postService.update(1L, 10L, form, newFiles))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최대 " + PostService.MAX_FILES_PER_POST);

        verify(postRepository, never()).updateById(any());
    }

    // ── delete() ────────────────────────────────────────────────

    @Test
    @DisplayName("delete: 소유자 불일치 → IllegalArgumentException")
    void delete_notOwner_throwsException() {
        Post post = makePost(50L, 999L);
        when(postRepository.findById(50L)).thenReturn(post);

        assertThatThrownBy(() -> postService.delete(1L, 50L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("삭제 권한");

        verify(postRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete: 소유자 일치 → 파일 삭제 후 게시글 삭제")
    void delete_owner_deletesFilesAndPost() {
        Post post = makePost(50L, 1L);
        when(postRepository.findById(50L)).thenReturn(post);

        PostFile pf = new PostFile();
        pf.setStoredName("uuid.pdf");
        when(postFileRepository.findByPostId(50L)).thenReturn(List.of(pf));

        postService.delete(1L, 50L);

        verify(fileStorageService).deleteFile("uuid.pdf");
        verify(postFileRepository).deleteByPostId(50L);
        verify(postRepository).deleteById(50L);
    }

    @Test
    @DisplayName("delete: 존재하지 않는 게시글 → IllegalArgumentException")
    void delete_notFound_throwsException() {
        when(postRepository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> postService.delete(1L, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는");
    }

    // ── getList() ────────────────────────────────────────────────

    @Test
    @DisplayName("getList: 총 0건이면 totalPages=1 반환")
    void getList_emptyBoard_totalPagesIsOne() {
        when(postRepository.countByBoard("FREE")).thenReturn(0);
        when(postRepository.findPageByBoard(eq("FREE"), eq(0), eq(10)))
                .thenReturn(Collections.emptyList());

        var result = postService.getList("FREE", 1, 10);

        assertThat(result.get("totalPages")).isEqualTo(1);
        assertThat(result.get("total")).isEqualTo(0);
    }

    // ── 헬퍼 ────────────────────────────────────────────────────

    private PostForm makeForm(String title, String body) {
        PostForm f = new PostForm();
        f.setTitle(title);
        f.setBody(body);
        return f;
    }

    private Post makePost(long id, long memberId) {
        Post p = new Post();
        p.setId(id);
        p.setMemberId(memberId);
        p.setBoardType("PROGRAM");
        p.setTitle("테스트 제목");
        p.setBody("테스트 내용");
        return p;
    }
}
