package com.formulabase.web;

import com.formulabase.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserService userService;
    private final SessionHelper sessionHelper;
    public AuthController(UserService userService, SessionHelper sessionHelper) { this.userService = userService; this.sessionHelper = sessionHelper; }

    @GetMapping("/login")
    public String login(Model model) { return "auth/login"; }

    @PostMapping("/login")
    public String authenticate(@RequestParam String username, @RequestParam String password,
                               HttpSession session, RedirectAttributes redirect) {
        try { var user = userService.authenticate(username, password); sessionHelper.login(session, user); return user.getRole() == com.formulabase.domain.UserRole.WORKER ? "redirect:/worker/tasks" : "redirect:/"; }
        catch (RuntimeException ex) { redirect.addFlashAttribute("error", ex.getMessage()); return "redirect:/login"; }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) { sessionHelper.logout(session); return "redirect:/login"; }
}
