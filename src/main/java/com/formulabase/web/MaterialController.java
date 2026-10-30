package com.formulabase.web;

import com.formulabase.domain.MaterialUnit;
import com.formulabase.service.MaterialService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/materials")
public class MaterialController {
    private final MaterialService service;
    private final SessionHelper sessionHelper;
    public MaterialController(MaterialService service, SessionHelper sessionHelper) { this.service = service; this.sessionHelper = sessionHelper; }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, HttpSession session, Model model) {
        if (sessionHelper.current(session) == null) return "redirect:/login";
        model.addAttribute("materials", service.search(keyword));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("units", MaterialUnit.values());
        return "materials/list";
    }

    @PostMapping
    public String create(@RequestParam String code, @RequestParam String name,
                         @RequestParam MaterialUnit unit, HttpSession session, RedirectAttributes redirect) {
        var user = sessionHelper.current(session);
        if (user == null) return "redirect:/login";
        if (user.getRole() != com.formulabase.domain.UserRole.QC && user.getRole() != com.formulabase.domain.UserRole.CEO) return "redirect:/materials";
        try {
            service.create(code, name, unit);
            redirect.addFlashAttribute("message", "Material saved");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/materials";
    }
}
