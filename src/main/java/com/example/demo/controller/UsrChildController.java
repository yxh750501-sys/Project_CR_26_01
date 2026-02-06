package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.service.ChildService;
import com.example.demo.vo.Child;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/usr/child")
public class UsrChildController {

    private final ChildService childService;

    public UsrChildController(ChildService childService) {
        this.childService = childService;
    }

    private Long getLoginedUserId(HttpSession session) {
        Object v = session.getAttribute("loginedUserId");
        if (v == null) return null;

        if (v instanceof Long) return (Long) v;
        if (v instanceof Integer) return ((Integer) v).longValue();

        try { return Long.parseLong(String.valueOf(v)); }
        catch (NumberFormatException e) { return null; }
    }

    @GetMapping("/list")
    public String showList(HttpSession session, Model model) {
        Long userId = getLoginedUserId(session);
        if (userId == null) return "redirect:/usr/member/login";

        List<Child> children = childService.getChildren(userId);
        model.addAttribute("children", children);
        return "usr/child/list";
    }

    @GetMapping("/write")
    public String showWrite(HttpSession session) {
        Long userId = getLoginedUserId(session);
        if (userId == null) return "redirect:/usr/member/login";
        return "usr/child/write";
    }

    @PostMapping("/doWrite")
    public String doWrite(HttpSession session,
                          @RequestParam String name,
                          @RequestParam(required = false) String birthDate,
                          @RequestParam(defaultValue = "U") String gender,
                          @RequestParam(required = false) String note) {
        Long userId = getLoginedUserId(session);
        if (userId == null) return "redirect:/usr/member/login";

        LocalDate bd = (birthDate == null || birthDate.isBlank()) ? null : LocalDate.parse(birthDate);
        childService.write(userId, name, bd, gender, note);
        return "redirect:/usr/child/list";
    }

    @GetMapping("/modify")
    public String showModify(HttpSession session, @RequestParam long id, Model model) {
        Long userId = getLoginedUserId(session);
        if (userId == null) return "redirect:/usr/member/login";

        Child child = childService.getChild(id, userId);
        if (child == null) return "redirect:/usr/child/list";

        model.addAttribute("child", child);
        return "usr/child/modify";
    }

    @PostMapping("/doModify")
    public String doModify(HttpSession session,
                           @RequestParam long id,
                           @RequestParam String name,
                           @RequestParam(required = false) String birthDate,
                           @RequestParam(defaultValue = "U") String gender,
                           @RequestParam(required = false) String note) {
        Long userId = getLoginedUserId(session);
        if (userId == null) return "redirect:/usr/member/login";

        LocalDate bd = (birthDate == null || birthDate.isBlank()) ? null : LocalDate.parse(birthDate);
        childService.modify(id, userId, name, bd, gender, note);
        return "redirect:/usr/child/list";
    }

    @PostMapping("/doDelete")
    public String doDelete(HttpSession session, @RequestParam long id) {
        Long userId = getLoginedUserId(session);
        if (userId == null) return "redirect:/usr/member/login";

        childService.delete(id, userId);
        return "redirect:/usr/child/list";
    }
}
