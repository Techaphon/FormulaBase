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

    public HistoryController(ProductionHistoryRepository history, SessionHelper sessions) {
        this.history = history;
        this.sessions = sessions;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String worker,
                       @RequestParam(required = false) LocalDate from,
                       @RequestParam(required = false) LocalDate to,
                       HttpSession session, Model model) {
        if (sessions.current(session) == null) return "redirect:/login";
        var records = history.findAllByOrderByCompletedAtDesc().stream()
                .filter(record -> keyword == null || keyword.isBlank()
                        || record.getProductionPlan().getFormula().getName().toLowerCase().contains(keyword.trim().toLowerCase())
                        || record.getProductionPlan().getFormula().getDocumentNo().toLowerCase().contains(keyword.trim().toLowerCase()))
                .filter(record -> worker == null || worker.isBlank()
                        || (record.getProductionPlan().getAssignedWorker() != null
                        && record.getProductionPlan().getAssignedWorker().getUsername().equals(worker)))
                .filter(record -> from == null || !record.getCompletedAt().toLocalDate().isBefore(from))
                .filter(record -> to == null || !record.getCompletedAt().toLocalDate().isAfter(to))
                .toList();
        model.addAttribute("records", records);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("selectedWorker", worker == null ? "" : worker);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("workers", history.findAllByOrderByCompletedAtDesc().stream()
                .map(record -> record.getProductionPlan().getAssignedWorker()).filter(java.util.Objects::nonNull)
                .distinct().toList());
        return "history/list";
    }
}
