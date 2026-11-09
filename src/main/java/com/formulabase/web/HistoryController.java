package com.formulabase.web;

import com.formulabase.repository.ProductionHistoryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/history")
public class HistoryController {
    private final ProductionHistoryRepository history;
    private final SessionHelper sessions;
    private final com.formulabase.service.AuditLogService audit;

    public HistoryController(ProductionHistoryRepository history, SessionHelper sessions, com.formulabase.service.AuditLogService audit) {
        this.history = history;
        this.sessions = sessions;
        this.audit = audit;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String worker,
                       @RequestParam(required = false) LocalDate from,
                       @RequestParam(required = false) LocalDate to,
                       HttpSession session, Model model) {
        if (sessions.current(session) == null) return "redirect:/login";
        var allRecords = history.findAllByOrderByCompletedAtDesc();
        var records = allRecords.stream()
                .filter(record -> keyword == null || keyword.isBlank()
                        || record.getProductionPlan().getFormula().getName().toLowerCase().contains(keyword.trim().toLowerCase())
                        || record.getProductionPlan().getFormula().getDocumentNo().toLowerCase().contains(keyword.trim().toLowerCase()))
                .filter(record -> worker == null || worker.isBlank()
                        || (record.getProductionPlan().getAssignedWorker() != null
                        && record.getProductionPlan().getAssignedWorker().getUsername().equals(worker)))
                .filter(record -> from == null || !record.getCompletedAt().atZoneSameInstant(com.formulabase.config.BusinessTime.ZONE).toLocalDate().isBefore(from))
                .filter(record -> to == null || !record.getCompletedAt().atZoneSameInstant(com.formulabase.config.BusinessTime.ZONE).toLocalDate().isAfter(to))
                .toList();
        model.addAttribute("records", records);
        model.addAttribute("transferHistory", audit.transfersFor(records.stream().map(record -> record.getProductionPlan().getId()).toList()));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("selectedWorker", worker == null ? "" : worker);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("workers", allRecords.stream()
                .map(record -> record.getProductionPlan().getAssignedWorker()).filter(java.util.Objects::nonNull)
                .distinct().toList());
        return "history/list";
    }
}
