package com.example.demo.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.constant.SessionConst;
import com.example.demo.dto.RunSummaryDto;
import com.example.demo.service.ChildService;
import com.example.demo.service.MyPageService;
import com.example.demo.service.UserService;
import com.example.demo.vo.Center;
import com.example.demo.vo.Child;
import com.example.demo.vo.User;

/**
 * 프로필 & 내 기록 허브 페이지 컨트롤러.
 *
 * <p>NeedLoginInterceptor가 /usr/** 를 보호하므로 컨트롤러 내부에서
 * 세션 null 체크가 없어도 안전하지만, 방어 코드로 redirect를 유지한다.
 */
@Controller
@RequestMapping("/usr/my")
public class UsrMyController {

    private static final Logger log = LoggerFactory.getLogger(UsrMyController.class);

    private final MyPageService myPageService;
    private final ChildService  childService;
    private final UserService   userService;

    public UsrMyController(MyPageService myPageService,
                           ChildService childService,
                           UserService userService) {
        this.myPageService = myPageService;
        this.childService  = childService;
        this.userService   = userService;
    }

    // ── 메인 화면 ─────────────────────────────────────────────────

    /**
     * GET /usr/my
     *
     * @param childId 아이 필터(0 = 전체)
     * @param msg     성공 메시지 (redirect 후 표시)
     * @param error   오류 메시지 (redirect 후 표시)
     */
    @GetMapping("")
    public String myPage(
            @RequestParam(value = "childId", defaultValue = "0") long childId,
            @RequestParam(value = "msg",     required = false)   String msg,
            @RequestParam(value = "error",   required = false)   String error,
            HttpSession session,
            Model model) {

        Object raw = session.getAttribute(SessionConst.LOGINED_USER_ID);
        if (raw == null) {
            return "redirect:/usr/member/login";
        }
        long userId = ((Number) raw).longValue();

        User user = userService.getUserById(userId);

        List<Child>         children        = childService.getChildrenByUserId(userId);
        List<RunSummaryDto> submittedRuns   = myPageService.getRecentSubmittedRuns(userId, childId);
        List<RunSummaryDto> draftRuns       = myPageService.getDraftRuns(userId, childId);
        List<Center>        favoriteCenters = myPageService.getFavoriteCenters(userId);

        model.addAttribute("loginedUser",     user);
        model.addAttribute("children",        children);
        model.addAttribute("selectedChildId", childId);
        model.addAttribute("submittedRuns",   submittedRuns);
        model.addAttribute("draftRuns",       draftRuns);
        model.addAttribute("favoriteCenters", favoriteCenters);
        model.addAttribute("msg",             msg);
        model.addAttribute("error",           error);

        return "usr/my/my";
    }

    // ── 프로필 수정 ───────────────────────────────────────────────

    /**
     * POST /usr/my/doUpdateProfile
     * 이름·전화번호 수정.
     */
    @PostMapping("/doUpdateProfile")
    public String doUpdateProfile(
            @RequestParam("name")  String name,
            @RequestParam(value = "phone", required = false) String phone,
            HttpSession session) {

        Object raw = session.getAttribute(SessionConst.LOGINED_USER_ID);
        if (raw == null) return "redirect:/usr/member/login";
        long userId = ((Number) raw).longValue();

        if (name == null || name.isBlank()) {
            return "redirect:/usr/my?error=" + encode("이름을 입력해 주세요.");
        }
        if (name.length() > 50) {
            return "redirect:/usr/my?error=" + encode("이름은 50자 이내로 입력해 주세요.");
        }

        userService.updateProfile(userId, name.trim(), phone);
        return "redirect:/usr/my?msg=" + encode("프로필이 업데이트되었습니다.");
    }

    // ── 비밀번호 변경 ─────────────────────────────────────────────

    /**
     * POST /usr/my/doChangePassword
     * 현재 비밀번호 확인 후 새 비밀번호로 변경.
     */
    @PostMapping("/doChangePassword")
    public String doChangePassword(
            @RequestParam("currentPw")    String currentPw,
            @RequestParam("newPw")        String newPw,
            @RequestParam("newPwConfirm") String newPwConfirm,
            HttpSession session) {

        Object raw = session.getAttribute(SessionConst.LOGINED_USER_ID);
        if (raw == null) return "redirect:/usr/member/login";
        long userId = ((Number) raw).longValue();

        if (newPw == null || newPw.length() < 8 || newPw.length() > 64) {
            return "redirect:/usr/my?error=" + encode("새 비밀번호는 8~64자로 입력해 주세요.");
        }
        if (!newPw.equals(newPwConfirm)) {
            return "redirect:/usr/my?error=" + encode("새 비밀번호가 일치하지 않습니다.");
        }

        boolean changed = userService.changePassword(userId, currentPw, newPw);
        if (!changed) {
            return "redirect:/usr/my?error=" + encode("현재 비밀번호가 올바르지 않습니다.");
        }
        return "redirect:/usr/my?msg=" + encode("비밀번호가 변경되었습니다.");
    }

    // ── 프로필 이미지 업로드 ──────────────────────────────────────

    /**
     * POST /usr/my/doUploadProfileImage
     * 프로필 사진 등록·변경.
     */
    @PostMapping("/doUploadProfileImage")
    public String doUploadProfileImage(
            @RequestParam("profileImage") MultipartFile file,
            HttpSession session) {

        Object raw = session.getAttribute(SessionConst.LOGINED_USER_ID);
        if (raw == null) return "redirect:/usr/member/login";
        long userId = ((Number) raw).longValue();

        if (file == null || file.isEmpty()) {
            return "redirect:/usr/my?error=" + encode("이미지를 선택해 주세요.");
        }

        try {
            userService.updateProfileImage(userId, file);
        } catch (IllegalArgumentException e) {
            return "redirect:/usr/my?error=" + encode(e.getMessage());
        } catch (Exception e) {
            log.error("프로필 이미지 업로드 실패: userId={}", userId, e);
            return "redirect:/usr/my?error=" + encode("이미지 업로드에 실패했습니다.");
        }

        return "redirect:/usr/my?msg=" + encode("프로필 사진이 변경되었습니다.");
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────

    private String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
}
