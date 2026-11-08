package com.formulabase.web;

import com.formulabase.domain.UserRole;
import com.formulabase.service.ProductionExecutionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/worker")
public class WorkerController {
    private final ProductionExecutionService executionService;
    private final SessionHelper sessionHelper;
    public WorkerController(ProductionExecutionService executionService, SessionHelper sessionHelper) {
        this.executionService = executionService;
        this.sessionHelper = sessionHelper;
    }
    @GetMapping("/tasks")
    public String tasks(HttpSession session, Model model) {
        var user = sessionHelper.current(session);
        if (user == null || (user.getRole() != UserRole.WORKER && user.getRole() != UserRole.CEO)) return "redirect:/login";
        model.addAttribute("user", user);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("tasks", executionService.tasksForWorker(user, LocalDate.now()));
        return "worker/tasks";
    }
    @GetMapping("/plans/{id}")
    public String view(@PathVariable UUID id, HttpSession session, Model model) {
        var user = sessionHelper.current(session);
        if (user == null || (user.getRole() != UserRole.WORKER && user.getRole() != UserRole.CEO)) return "redirect:/login";
        var details = executionService.details(id);
        var plan = details.plan();
        if (user.getRole() != UserRole.CEO && (plan.getAssignedWorker() == null || !plan.getAssignedWorker().getId().equals(user.getId()))) return "redirect:/worker/tasks";
        model.addAttribute("user", user);
        model.addAttribute("plan", plan);
        model.addAttribute("materials", details.materials());
        var steps = details.steps();
        model.addAttribute("steps", steps);
        model.addAttribute("stepsComplete", steps.stream().allMatch(step -> step.getStatus() == com.formulabase.domain.ExecutionStatus.COMPLETED));
        // Expose only the next eligible Start action. The service also enforces
        // this rule, while the UI makes the sequence clear to the Worker.
        model.addAttribute("nextStartStepId", steps.stream()
                .anyMatch(step -> step.getStatus() == com.formulabase.domain.ExecutionStatus.IN_PROGRESS)
                ? null
                : steps.stream().filter(step -> step.getStatus() == com.formulabase.domain.ExecutionStatus.PENDING)
                        .findFirst().map(step -> step.getId()).orElse(null));
        return "worker/view";
    }
    @PostMapping("/plans/{id}/start")
    public String start(@PathVariable UUID id, @RequestParam BigDecimal lossPercentage,
                        HttpSession session, RedirectAttributes redirect) {
        try { executionService.start(id, sessionHelper.current(session), lossPercentage); redirect.addFlashAttribute("message", "Batch started"); }
        catch (RuntimeException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/worker/plans/" + id;
    }
    @PostMapping("/plans/{id}/complete")
    public String complete(@PathVariable UUID id, @RequestParam int actualProduced,
                           @RequestParam int rejected, HttpSession session, RedirectAttributes redirect) {
        try { executionService.complete(id, sessionHelper.current(session), actualProduced, rejected); redirect.addFlashAttribute("message", "Batch recorded"); }
        catch (RuntimeException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/worker/plans/" + id;
    }
    @PostMapping("/plans/{planId}/steps/{stepId}/start") public String startStep(@PathVariable UUID planId,@PathVariable UUID stepId,HttpSession session,RedirectAttributes redirect){try{executionService.startStep(planId,stepId,sessionHelper.current(session));redirect.addFlashAttribute("message","Production step started");}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());}return "redirect:/worker/plans/"+planId;}
    @PostMapping("/plans/{planId}/steps/{stepId}/complete") public String completeStep(@PathVariable UUID planId,@PathVariable UUID stepId,@RequestParam(required=false) BigDecimal temperature,HttpSession session,RedirectAttributes redirect){try{executionService.completeStep(planId,stepId,temperature,sessionHelper.current(session));redirect.addFlashAttribute("message","Production step recorded");}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());}return "redirect:/worker/plans/"+planId;}
    @PostMapping("/plans/{planId}/steps/{stepId}/acknowledge") public String acknowledge(@PathVariable UUID planId,@PathVariable UUID stepId,@RequestParam String note,HttpSession session,RedirectAttributes redirect){try{executionService.acknowledgeStepAlert(planId,stepId,note,sessionHelper.current(session));redirect.addFlashAttribute("message","Overdue alert acknowledged");}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());}return "redirect:/worker/plans/"+planId;}
}
