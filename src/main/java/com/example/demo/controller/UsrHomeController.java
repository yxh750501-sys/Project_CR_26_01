package com.example.demo.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.constant.SessionConst;
import com.example.demo.service.PostService;
import com.example.demo.vo.Post;

/**
 * 메인 홈 화면 컨트롤러.
 *
 * <ul>
 *   <li>GET /          → 홈 JSP 렌더링 (로그인 여부로 JSP 내 분기)</li>
 *   <li>GET /usr/home  → "/" 로 리다이렉트 (하위 호환)</li>
 * </ul>
 *
 * <p>"/" 는 /usr/** 패턴에 해당하지 않으므로 NeedLoginInterceptor 미적용.
 * 로그인 상태는 세션으로 확인하며 JSP에서 ${loginedUserId} 유무로 분기한다.
 */
@Controller
public class UsrHomeController {

    private static final int HOME_POSTS_LIMIT = 5;

    private final PostService postService;

    public UsrHomeController(PostService postService) {
        this.postService = postService;
    }

    /**
     * GET / — 메인 홈.
     * 비로그인·로그인 모두 동일 URL로 진입하며 JSP 내에서 세션 유무로 분기한다.
     */
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        List<Post> programPosts = postService.getLatestPosts("PROGRAM", HOME_POSTS_LIMIT);
        List<Post> freePosts    = postService.getLatestPosts("FREE",    HOME_POSTS_LIMIT);

        model.addAttribute("programPosts",  programPosts);
        model.addAttribute("freePosts",     freePosts);
        model.addAttribute("loginedUserId", session.getAttribute(SessionConst.LOGINED_USER_ID));

        return "usr/home/home";
    }

    /**
     * GET /usr/home — 구 URL 하위 호환 리다이렉트.
     */
    @GetMapping("/usr/home")
    public String legacyHome() {
        return "redirect:/";
    }
}
