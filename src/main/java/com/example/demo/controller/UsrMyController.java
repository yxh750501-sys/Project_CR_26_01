package com.example.demo.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.constant.SessionConst;
import com.example.demo.dto.RunSummaryDto;
import com.example.demo.service.ChildService;
import com.example.demo.service.MyPageService;
import com.example.demo.vo.Center;
import com.example.demo.vo.Child;

/**
 * 내 기록 허브 페이지 컨트롤러.
 *
 * <p>NeedLoginInterceptor가 /usr/** 를 보호하므로 컨트롤러 내부에서
 * 세션 null 체크가 없어도 안전하지만, 방어 코드로 redirect를 유지한다.
 */
@Controller
@RequestMapping("/usr/my")
public class UsrMyController {

    private final MyPageService myPageService;
    private final ChildService  childService;

    public UsrMyController(MyPageService myPageService, ChildService childService) {
        this.myPageService = myPageService;
        this.childService  = childService;
    }

    /**
     * GET /usr/my
     *
     * @param childId 아이 필터(0 = 전체)
     */
    @GetMapping("")
    public String myPage(
            @RequestParam(value = "childId", defaultValue = "0") long childId,
            HttpSession session,
            Model model) {

        Object raw = session.getAttribute(SessionConst.LOGINED_USER_ID);
        if (raw == null) {
            return "redirect:/usr/member/login";
        }
        long userId = ((Number) raw).longValue();

        List<Child>        children       = childService.getChildrenByUserId(userId);
        List<RunSummaryDto> submittedRuns = myPageService.getRecentSubmittedRuns(userId, childId);
        List<RunSummaryDto> draftRuns     = myPageService.getDraftRuns(userId, childId);
        List<Center>       favoriteCenters = myPageService.getFavoriteCenters(userId);

        model.addAttribute("children",        children);
        model.addAttribute("selectedChildId", childId);
        model.addAttribute("submittedRuns",   submittedRuns);
        model.addAttribute("draftRuns",       draftRuns);
        model.addAttribute("favoriteCenters", favoriteCenters);

        return "usr/my/my";
    }
}
