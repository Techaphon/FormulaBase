package com.formulabase.web;

import com.formulabase.repository.MaterialRepository;
import com.formulabase.domain.MaterialUnit;
import com.formulabase.service.FormulaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/formulas")
public class FormulaController {
    private final FormulaService service;
    private final MaterialRepository materialRepository;
    private final SessionHelper sessionHelper;
    public FormulaController(FormulaService service, MaterialRepository materialRepository, SessionHelper sessionHelper) {
        this.service = service;
        this.materialRepository = materialRepository;
        this.sessionHelper = sessionHelper;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        if (sessionHelper.current(session) == null) return "redirect:/login";
        var user = sessionHelper.current(session);
        model.addAttribute("formulas", user.getRole() == com.formulabase.domain.UserRole.PC
                ? service.findAll().stream().filter(f -> f.getStatus() == com.formulabase.domain.FormulaStatus.CONFIRMED).toList()
                : service.findAll());
        return "formulas/list";
    }

    @GetMapping("/new")
    public String form(HttpSession session, Model model) {
        var user = sessionHelper.current(session);
        if (user == null) return "redirect:/login";
        if (user.getRole() != com.formulabase.domain.UserRole.QC && user.getRole() != com.formulabase.domain.UserRole.CEO) return "redirect:/formulas";
        model.addAttribute("materials", materialRepository.findAll());
        model.addAttribute("units", MaterialUnit.values());
        return "formulas/form";
    }

    @PostMapping
    public String create(@RequestParam String documentNo, @RequestParam String name,
                         @RequestParam List<String> materialIds,
                         @RequestParam(required = false) List<String> newMaterialCodes,
                         @RequestParam(required = false) List<String> newMaterialNames,
                         @RequestParam(required = false) List<String> newMaterialUnits,
                         @RequestParam List<BigDecimal> quantitiesPerUnit,
                         HttpSession session,
                         RedirectAttributes redirect) {
        try {
            var formula = service.create(documentNo, name, materialIds, newMaterialCodes, newMaterialNames,
                    newMaterialUnits, quantitiesPerUnit, sessionHelper.current(session));
            redirect.addFlashAttribute("message", "Formula saved: " + formula.getDocumentNo());
            return "redirect:/formulas";
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/formulas/new";
        }
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable UUID id, HttpSession session, RedirectAttributes redirect) {
        try { service.submitForApproval(id, sessionHelper.current(session)); redirect.addFlashAttribute("message", "Formula submitted for CEO approval"); }
        catch (RuntimeException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/formulas";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable UUID id, HttpSession session, RedirectAttributes redirect) {
        try { service.confirm(id, sessionHelper.current(session)); redirect.addFlashAttribute("message", "Formula confirmed by CEO"); }
        catch (RuntimeException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/formulas";
    }
}
