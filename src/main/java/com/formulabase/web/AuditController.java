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
    private final AuditLogService audit;
    private final SessionHelper sessions;
    private final com.formulabase.repository.FormulaRepository formulas;

    public AuditController(AuditLogService audit, SessionHelper sessions, com.formulabase.repository.FormulaRepository formulas) {
        this.audit = audit;
        this.sessions = sessions;
        this.formulas = formulas;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String actor,
                       @RequestParam(required = false) String action,
                       @RequestParam(required = false) String subject,
                       HttpSession session, Model model) {
        var user = sessions.current(session);
        if (user == null || (user.getRole() != UserRole.CEO && user.getRole() != UserRole.QC)) return "redirect:/";
        var visibleLogs = audit.search(keyword);
        if (user.getRole() == UserRole.QC) {
            var visibleIds = new java.util.HashSet<>(formulas.findIdsByStatusIn(java.util.List.of(
                    com.formulabase.domain.FormulaStatus.PENDING_APPROVAL,
                    com.formulabase.domain.FormulaStatus.CONFIRMED, com.formulabase.domain.FormulaStatus.REJECTED)));
            visibleLogs = visibleLogs.stream().filter(log -> "FORMULA".equals(log.getSubjectType())
                    && visibleIds.contains(log.getSubjectId())).toList();
        }
        var logs = visibleLogs.stream()
                .filter(log -> actor == null || actor.isBlank() || (log.getActor() != null && log.getActor().getUsername().equals(actor)))
                .filter(log -> action == null || action.isBlank() || log.getAction().equals(action))
                .filter(log -> subject == null || subject.isBlank() || log.getSubjectType().equals(subject))
                .toList();
        model.addAttribute("logs", logs);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("selectedActor", actor == null ? "" : actor);
        model.addAttribute("selectedAction", action == null ? "" : action);
        model.addAttribute("selectedSubject", subject == null ? "" : subject);
        model.addAttribute("actors", visibleLogs.stream().map(log -> log.getActor()).filter(java.util.Objects::nonNull).distinct().toList());
        model.addAttribute("actions", visibleLogs.stream().map(log -> log.getAction()).distinct().sorted().toList());
        model.addAttribute("subjects", visibleLogs.stream().map(log -> log.getSubjectType()).distinct().sorted().toList());
        model.addAttribute("formulaOnly", user.getRole() == UserRole.QC);
        return "audit/list";
    }
}
