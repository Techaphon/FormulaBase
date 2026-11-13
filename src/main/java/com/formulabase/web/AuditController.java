package com.formulabase.web;

import com.formulabase.domain.UserRole;
import com.formulabase.service.AuditLogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/audit")
public class AuditController {
    private final com.formulabase.service.AuditSearchService audit;
    private final SessionHelper sessions;
    private final com.formulabase.repository.FormulaRepository formulas;

    public AuditController(com.formulabase.service.AuditSearchService audit, SessionHelper sessions, com.formulabase.repository.FormulaRepository formulas) {
        this.audit = audit;
        this.sessions = sessions;
        this.formulas = formulas;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String actor,
                       @RequestParam(required = false) String action,
                       @RequestParam(required = false) String subject,
                       @RequestParam(defaultValue = "0") int page, HttpSession session, Model model) {
        var user = sessions.current(session);
        if (user == null || (user.getRole() != UserRole.CEO && user.getRole() != UserRole.QC)) return "redirect:/";
        java.util.Set<java.util.UUID> visibleIds = null;
        if (user.getRole() == UserRole.QC) visibleIds = new java.util.HashSet<>(formulas.findIdsByStatusIn(java.util.List.of(
            com.formulabase.domain.FormulaStatus.PENDING_APPROVAL, com.formulabase.domain.FormulaStatus.CONFIRMED, com.formulabase.domain.FormulaStatus.REJECTED, com.formulabase.domain.FormulaStatus.SUPERSEDED)));
        var result = audit.search(keyword,actor,action,subject,page,visibleIds);
        model.addAttribute("logs",result.getContent());
        model.addAttribute("resultPage",result);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("selectedActor", actor == null ? "" : actor);
        model.addAttribute("selectedAction", action == null ? "" : action);
        model.addAttribute("selectedSubject", subject == null ? "" : subject);
        model.addAttribute("actors", audit.actors(visibleIds));
        model.addAttribute("actions", audit.options("action",visibleIds));
        model.addAttribute("subjects", audit.options("subjectType",visibleIds));
        model.addAttribute("formulaOnly", user.getRole() == UserRole.QC);
        return "audit/list";
    }
}
