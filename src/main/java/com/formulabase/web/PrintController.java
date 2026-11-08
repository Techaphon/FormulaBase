package com.formulabase.web;

import com.formulabase.domain.Formula;
import com.formulabase.domain.ProductionPlan;
import com.formulabase.domain.User;
import com.formulabase.domain.UserRole;
import com.formulabase.domain.FormulaCalculationBasis;
import com.formulabase.service.AuditLogService;
import com.formulabase.service.FormulaService;
import com.formulabase.service.ProductionExecutionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Printable document views. Values are calculated on the server from the formula and plan. */
@Controller
@RequestMapping("/print")
public class PrintController {
    private static final int MAX_BOM_BATCH_SIZE = 1_000_000;

    private final FormulaService formulas;
    private final ProductionExecutionService plans;
    private final SessionHelper sessions;
    private final AuditLogService audit;

    public PrintController(FormulaService formulas, ProductionExecutionService plans,
                           SessionHelper sessions, AuditLogService audit) {
        this.formulas = formulas;
        this.plans = plans;
        this.sessions = sessions;
        this.audit = audit;
    }

    @GetMapping("/bom/{id}")
    public String bom(@PathVariable UUID id,
                      @RequestParam(defaultValue = "1") int batchSize,
                      HttpSession session, Model model) {
        User actor = requireSignedIn(session);
        boolean assignedWorker = plans.hasAssignedPlanForFormula(id, actor);
        if (actor.getRole() != UserRole.CEO && actor.getRole() != UserRole.QC && actor.getRole() != UserRole.PC && !assignedWorker) {
            throw new IllegalStateException("You do not have permission to print a BOM");
        }
        if (batchSize < 1 || batchSize > MAX_BOM_BATCH_SIZE) {
            throw new IllegalArgumentException("Batch size must be between 1 and " + MAX_BOM_BATCH_SIZE);
        }

        Formula formula = formulas.get(id);
        boolean percentFormula = formula.getCalculationBasis() == FormulaCalculationBasis.PERCENT_W_W;
        BigDecimal batchMassGrams = percentFormula
                ? formula.getProductUnitWeightGrams().multiply(BigDecimal.valueOf(batchSize)) : null;
        List<BomLine> lines = formula.getMaterials().stream().map(item -> {
            if (percentFormula) {
                BigDecimal total = batchMassGrams.multiply(item.getPercentage()).movePointLeft(2);
                return new BomLine(item.getPhase().name(), item.getMaterial().getCode(), item.getMaterial().getName(),
                        "% w/w", "G", item.getPercentage(), total);
            }
            return new BomLine(item.getPhase().name(), item.getMaterial().getCode(), item.getMaterial().getName(),
                    item.getUnit().name(), item.getUnit().name(), item.getQuantityPerUnit(),
                    item.getQuantityPerUnit().multiply(BigDecimal.valueOf(batchSize)));
        }).toList();
        model.addAttribute("formula", formula);
        model.addAttribute("batchSize", batchSize);
        model.addAttribute("percentFormula", percentFormula);
        model.addAttribute("batchMassGrams", batchMassGrams);
        model.addAttribute("bomLines", lines);
        model.addAttribute("generatedAt", OffsetDateTime.now());
        model.addAttribute("printedBy", actor.getFullName());
        audit.record(actor, "OPEN_BOM_PRINT_PREVIEW", "FORMULA", formula.getId(),
                "BOM basis " + batchSize + " product unit(s)");
        return "print/bom";
    }

    @GetMapping("/work-order/{id}")
    public String workOrder(@PathVariable UUID id, HttpSession session, Model model) {
        User actor = requireSignedIn(session);
        var details = plans.details(id);
        ProductionPlan plan = details.plan();
        assertWorkOrderAccess(plan, actor);
        model.addAttribute("plan", plan);
        model.addAttribute("draftPreview", plan.getPlanningCycle().getStatus() == com.formulabase.domain.MonthlyPlanningCycleStatus.DRAFT);
        model.addAttribute("materials", details.materials());
        model.addAttribute("steps", details.steps());
        model.addAttribute("generatedAt", OffsetDateTime.now());
        model.addAttribute("printedBy", actor.getFullName());
        audit.record(actor, "OPEN_WORK_ORDER_PRINT_PREVIEW", "PLAN", plan.getId(),
                "Opened work order for " + plan.getPlannedDate());
        return "print/work-order";
    }

    private User requireSignedIn(HttpSession session) {
        User user = sessions.current(session);
        if (user == null) throw new IllegalStateException("Please sign in first");
        return user;
    }

    private void assertWorkOrderAccess(ProductionPlan plan, User actor) {
        if (actor.getRole() == UserRole.CEO || actor.getRole() == UserRole.PC || actor.getRole() == UserRole.QC) return;
        if (actor.getRole() == UserRole.WORKER && plan.getAssignedWorker() != null
                && plan.getAssignedWorker().getId().equals(actor.getId())) return;
        throw new IllegalStateException("You do not have permission to print this work order");
    }

    public record BomLine(String phase, String code, String name, String perUnitUnit, String totalUnit,
                          BigDecimal perUnit, BigDecimal total) { }
}
