package com.formulabase.web;

import com.formulabase.repository.MaterialRepository;
import com.formulabase.domain.MaterialUnit;
import com.formulabase.domain.FormulaCalculationBasis;
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
    public String list(@RequestParam(required = false) String keyword, @RequestParam(required = false) com.formulabase.domain.FormulaStatus status, HttpSession session, Model model) {
        if (sessionHelper.current(session) == null) return "redirect:/login";
        var user = sessionHelper.current(session);
        var formulas = user.getRole() == com.formulabase.domain.UserRole.PC
                ? service.findAll().stream().filter(f -> f.getStatus() == com.formulabase.domain.FormulaStatus.CONFIRMED).toList()
                : service.findAll();
        model.addAttribute("formulas", formulas.stream().filter(f -> (keyword == null || keyword.isBlank() || f.getName().toLowerCase().contains(keyword.toLowerCase()) || f.getDocumentNo().toLowerCase().contains(keyword.toLowerCase())) && (status == null || f.getStatus() == status)).toList());
        model.addAttribute("keyword", keyword == null ? "" : keyword); model.addAttribute("selectedStatus", status); model.addAttribute("statuses", com.formulabase.domain.FormulaStatus.values());
        return "formulas/list";
    }

    @GetMapping("/new")
    public String form(HttpSession session, Model model) {
        var user = sessionHelper.current(session);
        if (user == null) return "redirect:/login";
        if (user.getRole() != com.formulabase.domain.UserRole.QC && user.getRole() != com.formulabase.domain.UserRole.CEO) return "redirect:/formulas";
        model.addAttribute("materials", materialRepository.findAll());
        model.addAttribute("units", MaterialUnit.values());
        model.addAttribute("phases", com.formulabase.domain.FormulaPhase.values());
        model.addAttribute("calculationBases", FormulaCalculationBasis.values());
        return "formulas/form";
    }

    @PostMapping
    public String create(@RequestParam String documentNo, @RequestParam String name,
                         @RequestParam(required = false) List<String> materialIds,
                         @RequestParam(required = false) List<String> newMaterialCodes,
                         @RequestParam(required = false) List<String> newMaterialNames,
                         @RequestParam(required = false) List<String> newMaterialUnits,
                         @RequestParam(required = false) List<String> materialPhases,
                         @RequestParam(defaultValue = "PERCENT_W_W") FormulaCalculationBasis calculationBasis,
                         @RequestParam(required = false) BigDecimal productUnitWeightGrams,
                         @RequestParam(required = false) List<BigDecimal> percentages,
                         @RequestParam(required = false) List<BigDecimal> quantitiesPerUnit,
                         @RequestParam(required = false) List<String> stepInstructions,
                         @RequestParam(required = false) List<Integer> stepTargetSeconds,
                         @RequestParam(required = false) List<Boolean> stepRequiresTemperature,
                         HttpSession session,
                         RedirectAttributes redirect) {
        try {
            var formula = service.createWithSteps(documentNo, name, materialIds, newMaterialCodes, newMaterialNames,
                    newMaterialUnits, materialPhases, calculationBasis, productUnitWeightGrams, percentages, quantitiesPerUnit, stepInstructions, stepTargetSeconds,
                    stepRequiresTemperature, sessionHelper.current(session));
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
    public String confirm(@PathVariable UUID id, @RequestParam(required = false) String approvalReason, HttpSession session, RedirectAttributes redirect) {
        try { service.confirm(id, approvalReason, sessionHelper.current(session)); redirect.addFlashAttribute("message", "Formula confirmed by CEO"); }
        catch (RuntimeException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/formulas";
    }

    @GetMapping("/{id}") public String detail(@PathVariable UUID id, HttpSession session, Model model) {
        if (sessionHelper.current(session) == null) return "redirect:/login";
        model.addAttribute("formula", service.get(id)); return "formulas/view";
    }
    @PostMapping("/{id}/revise") public String revise(@PathVariable UUID id, HttpSession session, RedirectAttributes redirect){try{var revision=service.createRevision(id,sessionHelper.current(session));redirect.addFlashAttribute("message","Created revision "+revision.getDocumentNo());return "redirect:/formulas/"+revision.getId()+"/edit";}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());return "redirect:/formulas/"+id;}}
    @GetMapping("/{id}/edit") public String edit(@PathVariable UUID id,HttpSession session,Model model){var user=sessionHelper.current(session);if(user==null)return "redirect:/login";if(user.getRole()!=com.formulabase.domain.UserRole.QC&&user.getRole()!=com.formulabase.domain.UserRole.CEO)return "redirect:/formulas";model.addAttribute("formula",service.get(id));model.addAttribute("materials",materialRepository.findAll());model.addAttribute("phases",com.formulabase.domain.FormulaPhase.values());model.addAttribute("calculationBases",FormulaCalculationBasis.values());return "formulas/edit";}
    @PostMapping("/{id}/edit") public String update(@PathVariable UUID id,@RequestParam String documentNo,@RequestParam String name,@RequestParam List<String> materialIds,@RequestParam(required=false) List<String> materialPhases,@RequestParam(defaultValue="PER_PRODUCT_UNIT") FormulaCalculationBasis calculationBasis,@RequestParam(required=false) BigDecimal productUnitWeightGrams,@RequestParam(required=false) List<BigDecimal> percentages,@RequestParam(required=false) List<BigDecimal> quantitiesPerUnit,HttpSession session,RedirectAttributes redirect){try{service.updateDraft(id,documentNo,name,materialIds,materialPhases,calculationBasis,productUnitWeightGrams,percentages,quantitiesPerUnit,sessionHelper.current(session));redirect.addFlashAttribute("message","Formula updated");}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());}return "redirect:/formulas/"+id;}
    @PostMapping("/{id}/reject") public String reject(@PathVariable UUID id,@RequestParam String reason,HttpSession session,RedirectAttributes redirect){try{service.reject(id,reason,sessionHelper.current(session));redirect.addFlashAttribute("message","Formula rejected");}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());}return "redirect:/formulas/"+id;}
    @PostMapping("/{id}/delete") public String delete(@PathVariable UUID id,HttpSession session,RedirectAttributes redirect){try{service.deleteDraft(id,sessionHelper.current(session));redirect.addFlashAttribute("message","Formula deleted");}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());}return "redirect:/formulas";}
}
