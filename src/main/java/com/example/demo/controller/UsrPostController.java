package com.example.demo.controller;

import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.constant.SessionConst;
import com.example.demo.service.PostService;
import com.example.demo.vo.Post;

/**
 * /usr/post/** 진입점.
 * 자유게시판(/usr/free/**)과 동일한 서비스·뷰를 공유하므로
 * PostService.getList("FREE") / getDetail() 을 호출하고
 * usr/free/ 뷰를 그대로 렌더링한다.
 *
 * <p>URL 구조:
 * <pre>
 *   GET /usr/post/list          자유게시판 목록
 *   GET /usr/post/detail?id=N   자유게시판 상세
 * </pre>
 */
@Controller
@RequestMapping("/usr/post")
public class UsrPostController {

    private static final String BOARD_TYPE = "FREE";
    private static final int    PAGE_SIZE  = 10;

    private final PostService postService;

    public UsrPostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * GET /usr/post/list
     * UsrFreeController.list() 와 동일한 로직, 동일한 뷰를 재사용한다.
     */
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") int page,
                       HttpSession session,
                       Model model) {
        Map<String, Object> data = postService.getList(BOARD_TYPE, page, PAGE_SIZE);
        model.addAllAttributes(data);
        model.addAttribute("loginedUserId", session.getAttribute(SessionConst.LOGINED_USER_ID));
        return "usr/free/list";
    }

    /**
     * GET /usr/post/detail?id=N
     * /usr/free/detail 과 동일한 상세 화면.
     */
    @GetMapping("/detail")
    public String detail(@RequestParam("id") long id,
                         HttpSession session,
                         Model model) {
        Post post = postService.getDetail(id);
        if (post == null) return "redirect:/usr/post/list";
        model.addAttribute("post", post);
        model.addAttribute("loginedUserId", session.getAttribute(SessionConst.LOGINED_USER_ID));
        return "usr/free/detail";
    }
}
