package com.formulabase.web;

import com.formulabase.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletRequest;
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
    private final boolean demoSeedEnabled;
    private final String demoSeedPassword;

    public AuthController(UserService userService, SessionHelper sessionHelper,
                          @Value("${demo.seed.enabled:false}") boolean demoSeedEnabled,
                          @Value("${demo.seed.password:}") String demoSeedPassword) {
        this.userService = userService;
        this.sessionHelper = sessionHelper;
        this.demoSeedEnabled = demoSeedEnabled;
        this.demoSeedPassword = demoSeedPassword;
    }

    @GetMapping("/login")
    public String login(Model model) {
        // The hint is intentionally available only during an explicitly enabled demo seed.
        // A normal production deployment never exposes a password on the login page.
        if (demoSeedEnabled && !demoSeedPassword.isBlank()) {
            model.addAttribute("demoPassword", demoSeedPassword);
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String authenticate(@RequestParam String username, @RequestParam String password,
                               HttpSession session, HttpServletRequest request, RedirectAttributes redirect) {
        try { var user = userService.authenticate(username, password); request.changeSessionId(); sessionHelper.login(session, user); return user.getRole() == com.formulabase.domain.UserRole.WORKER ? "redirect:/worker/tasks" : "redirect:/"; }
        catch (RuntimeException ex) { redirect.addFlashAttribute("error", ex.getMessage()); return "redirect:/login"; }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) { sessionHelper.logout(session); return "redirect:/login"; }
}
