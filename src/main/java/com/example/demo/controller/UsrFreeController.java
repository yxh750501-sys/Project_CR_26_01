package com.example.demo.controller;

import java.util.Map;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.constant.SessionConst;
import com.example.demo.form.PostForm;
import com.example.demo.service.PostService;
import com.example.demo.vo.Post;

/**
 * 자유게시판 컨트롤러 (정보 공유).
 * boardType = "FREE"
 */
@Controller
@RequestMapping("/usr/free")
public class UsrFreeController {

    private static final Logger log        = LoggerFactory.getLogger(UsrFreeController.class);
    private static final String BOARD_TYPE = "FREE";
    private static final int    PAGE_SIZE  = 10;

    private final PostService postService;

    public UsrFreeController(PostService postService) {
        this.postService = postService;
    }

    // ── 목록 ────────────────────────────────────────────────────

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") int page,
                       HttpSession session,
                       Model model) {
        Map<String, Object> data = postService.getList(BOARD_TYPE, page, PAGE_SIZE);
        model.addAllAttributes(data);
        model.addAttribute("loginedUserId", session.getAttribute(SessionConst.LOGINED_USER_ID));
        return "usr/free/list";
    }

    // ── 상세 ────────────────────────────────────────────────────

    @GetMapping("/detail")
    public String detail(@RequestParam("id") long id,
                         HttpSession session,
                         Model model) {
        Post post = postService.getDetail(id);
        if (post == null) return "redirect:/usr/free/list";
        model.addAttribute("post", post);
        model.addAttribute("loginedUserId", session.getAttribute(SessionConst.LOGINED_USER_ID));
        return "usr/free/detail";
    }

    // ── 작성 폼 ─────────────────────────────────────────────────

    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "usr/free/write";
    }

    // ── 작성 처리 ────────────────────────────────────────────────

    @PostMapping("/doWrite")
    public String doWrite(@Valid @ModelAttribute("postForm") PostForm form,
                          BindingResult bindingResult,
                          @RequestParam(value = "files", required = false) MultipartFile[] files,
                          HttpSession session,
                          Model model) {
        if (bindingResult.hasErrors()) {
            return "usr/free/write";
        }
        long userId = resolveUserId(session);
        try {
            long postId = postService.create(userId, BOARD_TYPE, form, files);
            return "redirect:/usr/free/detail?id=" + postId;
        } catch (Exception e) {
            log.warn("자유게시판 글 작성 실패: userId={}, error={}", userId, e.getMessage());
            model.addAttribute("errorMsg", e.getMessage());
            return "usr/free/write";
        }
    }

    // ── 수정 폼 ─────────────────────────────────────────────────

    @GetMapping("/modify")
    public String modifyForm(@RequestParam("id") long id,
                             HttpSession session,
                             Model model) {
        Post post = postService.getDetail(id);
        if (post == null) return "redirect:/usr/free/list";

        long userId = resolveUserId(session);
        if (post.getMemberId() != userId) return "redirect:/usr/free/detail?id=" + id;

        model.addAttribute("postForm", toPostForm(post));
        model.addAttribute("post", post);
        return "usr/free/modify";
    }

    // ── 수정 처리 ────────────────────────────────────────────────

    @PostMapping("/doModify")
    public String doModify(@RequestParam("id") long id,
                           @Valid @ModelAttribute("postForm") PostForm form,
                           BindingResult bindingResult,
                           @RequestParam(value = "files", required = false) MultipartFile[] files,
                           HttpSession session,
                           Model model) {
        if (bindingResult.hasErrors()) {
            Post post = postService.getDetail(id);
            model.addAttribute("post", post);
            return "usr/free/modify";
        }
        long userId = resolveUserId(session);
        try {
            postService.update(userId, id, form, files);
            return "redirect:/usr/free/detail?id=" + id;
        } catch (IllegalArgumentException e) {
            log.warn("자유게시판 글 수정 실패: userId={}, postId={}, error={}", userId, id, e.getMessage());
            model.addAttribute("errorMsg", e.getMessage());
            Post post = postService.getDetail(id);
            model.addAttribute("post", post);
            return "usr/free/modify";
        }
    }

    // ── 삭제 처리 ────────────────────────────────────────────────

    @PostMapping("/doDelete")
    public String doDelete(@RequestParam("id") long id, HttpSession session) {
        long userId = resolveUserId(session);
        try {
            postService.delete(userId, id);
        } catch (IllegalArgumentException e) {
            log.warn("자유게시판 글 삭제 실패: userId={}, postId={}, error={}", userId, id, e.getMessage());
        }
        return "redirect:/usr/free/list";
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────

    private long resolveUserId(HttpSession session) {
        Object v = session.getAttribute(SessionConst.LOGINED_USER_ID);
        if (v == null) return 0L;
        return (v instanceof Number) ? ((Number) v).longValue()
                                     : Long.parseLong(v.toString());
    }

    private PostForm toPostForm(Post post) {
        PostForm f = new PostForm();
        f.setTitle(post.getTitle());
        f.setBody(post.getBody());
        return f;
    }
}
