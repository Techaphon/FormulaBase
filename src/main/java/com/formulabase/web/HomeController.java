package com.formulabase.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import com.formulabase.service.WorkflowService;
import java.time.LocalDate;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
    private final WorkflowService workflowService;
    private final SessionHelper sessions;

    public HomeController(WorkflowService workflowService, SessionHelper sessions) {
        this.workflowService = workflowService;
        this.sessions = sessions;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        return session.getAttribute("FORMULA_BASE_USERNAME") == null ? "redirect:/login" : "redirect:/workflow";
    }

    @GetMapping("/workflow")
    public String workflow(HttpSession session, Model model) {
        if (session.getAttribute("FORMULA_BASE_USERNAME") == null) return "redirect:/login";
        var user = sessions.current(session);
        if (user == null) return "redirect:/login";
        LocalDate today = LocalDate.now();
        model.addAttribute("today", today);
        if (user.getRole() == com.formulabase.domain.UserRole.WORKER) return "redirect:/worker/tasks";
        if (user.getRole() != com.formulabase.domain.UserRole.CEO) {
            if (user.getRole() == com.formulabase.domain.UserRole.QC) {
                model.addAttribute("snapshot", workflowService.snapshot(LocalDate.now()));
                model.addAttribute("pendingApprovals", workflowService.pendingApprovals());
            } else {
                model.addAttribute("draftCycles", workflowService.draftCycles());
            }
            return "workflow/role";
        }
        model.addAttribute("snapshot", workflowService.snapshot(today));
        model.addAttribute("pendingApprovals", workflowService.pendingApprovals());
        model.addAttribute("draftCycles", workflowService.draftCycles());
        model.addAttribute("todayTasks", workflowService.todayTasks(today));
        model.addAttribute("today", today);
        return "workflow/dashboard";
    }
}
