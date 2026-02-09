package com.example.demo.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.service.ChildService;
import com.example.demo.vo.Child;

@Controller
public class UsrChildController {

	private final ChildService childService;

	public UsrChildController(ChildService childService) {
		this.childService = childService;
	}

	private long getLoginedUserId(HttpSession session) {
		Object obj = session.getAttribute("loginedUserId");
		if (obj == null) return 0;
		return ((Number) obj).longValue();
	}

	private Long getSelectedChildId(HttpSession session) {
		Object obj = session.getAttribute("selectedChildId");
		if (obj == null) return null;
		return ((Number) obj).longValue();
	}

	@GetMapping("/usr/child/list")
	public String showList(HttpSession session, Model model,
			@RequestParam(value = "needSelect", required = false) String needSelect) {

		long loginedUserId = getLoginedUserId(session);

		List<Child> children = childService.getChildrenByUserId(loginedUserId);
		model.addAttribute("children", children);

		Long selectedChildId = getSelectedChildId(session);
		Child selectedChild = null;

		if (selectedChildId != null) {
			selectedChild = childService.getChildByIdAndUserId(selectedChildId, loginedUserId);

			if (selectedChild == null) {
				session.removeAttribute("selectedChildId");
				selectedChildId = null;
			}
		}

		model.addAttribute("selectedChildId", selectedChildId);
		model.addAttribute("selectedChild", selectedChild);

		if (needSelect != null) {
			model.addAttribute("needSelect", true);
		}

		return "usr/child/list";
	}

	@GetMapping("/usr/child/write")
	public String showWrite() {
		return "usr/child/write";
	}

	@PostMapping("/usr/child/doWrite")
	public String doWrite(HttpSession session,
			@RequestParam("name") String name,
			@RequestParam(value = "birthDate", required = false) String birthDate,
			@RequestParam(value = "gender", required = false, defaultValue = "U") String gender,
			@RequestParam(value = "note", required = false) String note,
			RedirectAttributes ra) {

		long loginedUserId = getLoginedUserId(session);

		name = name == null ? "" : name.trim();
		if (name.isEmpty()) {
			ra.addFlashAttribute("msg", "이름은 필수입니다.");
			return "redirect:/usr/child/write";
		}

		boolean ok = childService.writeChild(loginedUserId, name, birthDate, gender, note);
		ra.addFlashAttribute("msg", ok ? "아이 프로필이 등록되었습니다." : "등록에 실패했습니다.");

		return "redirect:/usr/child/list";
	}

	@GetMapping("/usr/child/modify")
	public String showModify(HttpSession session, Model model,
			@RequestParam("id") long id,
			RedirectAttributes ra) {

		long loginedUserId = getLoginedUserId(session);

		Child child = childService.getChildByIdAndUserId(id, loginedUserId);
		if (child == null) {
			ra.addFlashAttribute("msg", "존재하지 않거나 접근할 수 없는 아이입니다.");
			return "redirect:/usr/child/list";
		}

		model.addAttribute("child", child);
		return "usr/child/modify";
	}

	@PostMapping("/usr/child/doModify")
	public String doModify(HttpSession session,
			@RequestParam("id") long id,
			@RequestParam("name") String name,
			@RequestParam(value = "birthDate", required = false) String birthDate,
			@RequestParam(value = "gender", required = false, defaultValue = "U") String gender,
			@RequestParam(value = "note", required = false) String note,
			RedirectAttributes ra) {

		long loginedUserId = getLoginedUserId(session);

		name = name == null ? "" : name.trim();
		if (name.isEmpty()) {
			ra.addFlashAttribute("msg", "이름은 필수입니다.");
			return "redirect:/usr/child/modify?id=" + id;
		}

		boolean ok = childService.modifyChild(id, loginedUserId, name, birthDate, gender, note);
		ra.addFlashAttribute("msg", ok ? "수정되었습니다." : "수정에 실패했습니다.");

		return "redirect:/usr/child/list";
	}

	@PostMapping("/usr/child/doDelete")
	public String doDelete(HttpSession session,
			@RequestParam("id") long id,
			RedirectAttributes ra) {

		long loginedUserId = getLoginedUserId(session);

		boolean ok = childService.deleteChild(id, loginedUserId);

		Long selectedChildId = getSelectedChildId(session);
		if (selectedChildId != null && selectedChildId == id) {
			session.removeAttribute("selectedChildId");
		}

		ra.addFlashAttribute("msg", ok ? "삭제되었습니다." : "삭제에 실패했습니다.");
		return "redirect:/usr/child/list";
	}

	@PostMapping("/usr/child/doSelect")
	public String doSelect(HttpSession session,
			@RequestParam("id") long id,
			RedirectAttributes ra) {

		long loginedUserId = getLoginedUserId(session);

		Child child = childService.getChildByIdAndUserId(id, loginedUserId);
		if (child == null) {
			session.removeAttribute("selectedChildId");
			ra.addFlashAttribute("msg", "대표 아이로 선택할 수 없습니다.");
			return "redirect:/usr/child/list";
		}

		session.setAttribute("selectedChildId", id);
		ra.addFlashAttribute("msg", "대표 아이가 선택되었습니다: " + child.getName());

		return "redirect:/usr/child/list";
	}
}
