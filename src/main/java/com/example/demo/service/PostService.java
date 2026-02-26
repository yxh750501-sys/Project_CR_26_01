package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.form.PostForm;
import com.example.demo.repository.PostFileRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.vo.Post;
import com.example.demo.vo.PostFile;

/**
 * 게시글 서비스.
 *
 * <p>글당 첨부 파일 최대 5개 정책을 적용한다.
 * 수정 시 기존 파일은 유지되며 새 파일이 추가된다.
 */
@Service
public class PostService {

    /** 글당 최대 첨부 파일 수 */
    static final int MAX_FILES_PER_POST = 5;

    private final PostRepository     postRepository;
    private final PostFileRepository postFileRepository;
    private final FileStorageService fileStorageService;

    public PostService(PostRepository postRepository,
                       PostFileRepository postFileRepository,
                       FileStorageService fileStorageService) {
        this.postRepository     = postRepository;
        this.postFileRepository = postFileRepository;
        this.fileStorageService = fileStorageService;
    }

    // ── 목록 ────────────────────────────────────────────────────

    /**
     * 게시판 목록 페이지 데이터를 반환한다.
     *
     * @return map keys: posts, page, size, total, totalPages
     */
    public Map<String, Object> getList(String boardType, int page, int size) {
        if (page < 1) page = 1;
        int total      = postRepository.countByBoard(boardType);
        int totalPages = (total == 0) ? 1 : (int) Math.ceil((double) total / size);
        if (page > totalPages) page = totalPages;

        int        offset = (page - 1) * size;
        List<Post> posts  = postRepository.findPageByBoard(boardType, offset, size);

        Map<String, Object> result = new HashMap<>();
        result.put("posts",      posts);
        result.put("page",       page);
        result.put("size",       size);
        result.put("total",      total);
        result.put("totalPages", totalPages);
        return result;
    }

    // ── 상세 ────────────────────────────────────────────────────

    public Post getDetail(long id) {
        Post post = postRepository.findById(id);
        if (post == null) return null;
        List<PostFile> files = postFileRepository.findByPostId(id);
        post.setFiles(files != null ? files : new ArrayList<>());
        return post;
    }

    // ── 작성 ────────────────────────────────────────────────────

    @Transactional
    public long create(long userId, String boardType, PostForm form, MultipartFile[] files) {
        Post post = buildPost(userId, boardType, 0L, form);
        postRepository.insertPost(post);
        long postId = postRepository.getLastInsertId();
        saveFiles(files, postId, 0);
        return postId;
    }

    // ── 수정 ────────────────────────────────────────────────────

    @Transactional
    public void update(long userId, long postId, PostForm form, MultipartFile[] files) {
        Post existing = postRepository.findById(postId);
        if (existing == null) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }
        if (existing.getMemberId() != userId) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        int existingCount = postFileRepository.findByPostId(postId).size();
        int newCount      = countNonEmpty(files);
        if (existingCount + newCount > MAX_FILES_PER_POST) {
            throw new IllegalArgumentException(
                    "첨부 파일은 글당 최대 " + MAX_FILES_PER_POST + "개입니다. "
                    + "(현재 " + existingCount + "개 첨부됨)");
        }

        Post updated = buildPost(userId, existing.getBoardType(), postId, form);
        postRepository.updateById(updated);
        saveFiles(files, postId, existingCount);
    }

    // ── 삭제 ────────────────────────────────────────────────────

    @Transactional
    public void delete(long userId, long postId) {
        Post post = postRepository.findById(postId);
        if (post == null) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }
        if (post.getMemberId() != userId) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        // 첨부파일 디스크 정리 후 DB 삭제
        List<PostFile> attachments = postFileRepository.findByPostId(postId);
        for (PostFile f : attachments) {
            fileStorageService.deleteFile(f.getStoredName());
        }
        postFileRepository.deleteByPostId(postId);
        postRepository.deleteById(postId);
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────

    private Post buildPost(long userId, String boardType, long postId, PostForm form) {
        Post post = new Post();
        post.setId(postId);
        post.setBoardType(boardType);
        post.setMemberId(userId);
        post.setTitle(form.getTitle());
        post.setBody(form.getBody());
        post.setCategory(form.getCategory());
        post.setStartDate(form.getStartDate());
        post.setEndDate(form.getEndDate());
        post.setLocation(form.getLocation());
        post.setFee(form.getFee());
        post.setMaxPeople(form.getMaxPeople());
        post.setApplyUrl(form.getApplyUrl());
        return post;
    }

    private void saveFiles(MultipartFile[] files, long postId, int existingCount) {
        if (files == null) return;
        int saved = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            if (existingCount + saved >= MAX_FILES_PER_POST) break;
            PostFile pf = fileStorageService.store(file, postId);
            postFileRepository.insertFile(pf);
            saved++;
        }
    }

    private int countNonEmpty(MultipartFile[] files) {
        if (files == null) return 0;
        int cnt = 0;
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty()) cnt++;
        }
        return cnt;
    }
}
