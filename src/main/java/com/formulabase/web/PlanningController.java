package com.formulabase.web;

import com.formulabase.service.FormulaService;
import com.formulabase.service.PlanningService;
import com.formulabase.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.UUID;
import jakarta.servlet.http.HttpSession;
import com.formulabase.domain.UserRole;

@Controller
@RequestMapping("/planning")
public class PlanningController {
    private final PlanningService planningService;
    private final FormulaService formulaService;
    private final UserService userService;
    private final SessionHelper sessionHelper;
    private final com.formulabase.service.AuditLogService audit;
    public PlanningController(PlanningService planningService, FormulaService formulaService, UserService userService, SessionHelper sessionHelper, com.formulabase.service.AuditLogService audit) {
        this.planningService = planningService;
        this.formulaService = formulaService;
        this.userService = userService;
        this.sessionHelper = sessionHelper;
        this.audit = audit;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        if (!allowed(session, UserRole.PC, UserRole.CEO)) return "redirect:/login";
        model.addAttribute("cycles", planningService.cycles());
        return "planning/list";
    }

    @PostMapping
    public String createCycle(@RequestParam String planningMonth, HttpSession session, RedirectAttributes redirect) {
        if (!allowed(session, UserRole.PC, UserRole.CEO)) return "redirect:/login";
        try {
            var existing = planningService.cycles().stream().filter(cycle -> cycle.getPlanningMonth().equals(planningMonth)).findFirst();
            var cycle = existing.orElseGet(() -> planningService.createCycle(planningMonth, sessionHelper.current(session)));
            redirect.addFlashAttribute("message", existing.isPresent() ? "Planning cycle already exists" : "Planning cycle created");
            return "redirect:/planning/" + cycle.getId();
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/planning";
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable UUID id, HttpSession session, Model model) {
        if (!allowed(session, UserRole.PC, UserRole.CEO)) return "redirect:/login";
        var cycle = planningService.getCycle(id);
        model.addAttribute("cycle", cycle);
        var month = java.time.YearMonth.parse(cycle.getPlanningMonth());
        model.addAttribute("monthStart", month.atDay(1));
        model.addAttribute("monthEnd", month.atEndOfMonth());
        var allPlans = planningService.plans(id);
        var plans = allPlans.stream()
                .filter(plan -> plan.getStatus() != com.formulabase.domain.ProductionPlanStatus.CANCELLED)
                .toList();
        model.addAttribute("cancelledCount", allPlans.size() - plans.size());
        model.addAttribute("deleteBlockedCount", allPlans.stream().filter(plan ->
                !plan.isDeletableDraftTask()).count());
        model.addAttribute("plans", plans);
        model.addAttribute("transferHistory", audit.transfersFor(plans.stream().map(com.formulabase.domain.ProductionPlan::getId).toList()));
        model.addAttribute("unassignedCount", plans.stream().filter(plan -> plan.getAssignedWorker() == null && plan.getStatus() == com.formulabase.domain.ProductionPlanStatus.PLANNED).count());
        model.addAttribute("formulas", formulaService.findAll());
        model.addAttribute("workers", userService.workers());
        return "planning/view";
    }

    @PostMapping("/{id}/plans")
    public String addPlan(@PathVariable UUID id, @RequestParam UUID formulaId,
                          @RequestParam(required = false) UUID workerId,
                          @RequestParam LocalDate plannedDate, @RequestParam int targetQuantity,
                          HttpSession session, RedirectAttributes redirect) {
        if (!allowed(session, UserRole.PC, UserRole.CEO)) return "redirect:/login";
        try { planningService.addPlan(id, formulaId, workerId, plannedDate, targetQuantity, sessionHelper.current(session)); redirect.addFlashAttribute("message", "Production task added"); }
        catch (RuntimeException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/planning/" + id;
    }

    @PostMapping("/{id}/lock")
    public String lock(@PathVariable UUID id, HttpSession session, RedirectAttributes redirect) {
        if (!allowed(session, UserRole.PC, UserRole.CEO)) return "redirect:/login";
        try { planningService.lockCycle(id, sessionHelper.current(session)); redirect.addFlashAttribute("message", "Planning cycle locked"); }
        catch (RuntimeException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/planning/" + id;
    }

    @PostMapping("/plans/{id}/reschedule") public String reschedule(@PathVariable UUID id,@RequestParam LocalDate plannedDate,@RequestParam(required=false) UUID workerId,@RequestParam String reason,HttpSession session,RedirectAttributes redirect){
        try{planningService.reschedule(id,plannedDate,workerId,reason,sessionHelper.current(session));redirect.addFlashAttribute("message","Production task rescheduled");}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());}return "redirect:/planning/"+planningService.getPlanCycleId(id);
    }
    @PostMapping("/plans/{id}/cancel") public String cancel(@PathVariable UUID id,HttpSession session,RedirectAttributes redirect){
        UUID cycleId=planningService.getPlanCycleId(id);try{planningService.cancel(id,sessionHelper.current(session));redirect.addFlashAttribute("message","Production task cancelled");}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());}return "redirect:/planning/"+cycleId;
    }

    @PostMapping("/{cycleId}/plans/{planId}/delete")
    public String deletePlan(@PathVariable UUID cycleId, @PathVariable UUID planId, HttpSession session, RedirectAttributes redirect) {
        if (!allowed(session, UserRole.PC, UserRole.CEO)) return "redirect:/login";
        try { planningService.deletePlan(cycleId, planId, sessionHelper.current(session)); redirect.addFlashAttribute("message", "Draft production task deleted"); }
        catch (RuntimeException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/planning/" + cycleId;
    }

    @PostMapping("/{id}/delete")
    public String deleteCycle(@PathVariable UUID id, HttpSession session, RedirectAttributes redirect) {
        if (!allowed(session, UserRole.PC, UserRole.CEO)) return "redirect:/login";
        try { planningService.deleteCycle(id, sessionHelper.current(session)); redirect.addFlashAttribute("message", "Draft monthly plan deleted"); return "redirect:/planning"; }
        catch (RuntimeException ex) { redirect.addFlashAttribute("error", ex.getMessage()); return "redirect:/planning/" + id; }
    }

    private boolean allowed(HttpSession session, UserRole... roles) {
        var user = sessionHelper.current(session);
        if (user == null) return false;
        for (UserRole role : roles) if (user.getRole() == role) return true;
        return false;
    }
}
