package com.formulabase.service;

import com.formulabase.domain.ProductionHistory;
import com.formulabase.domain.ProductionPlan;
import com.formulabase.domain.ProductionPlanStatus;
import com.formulabase.domain.User;
import com.formulabase.domain.UserRole;
import com.formulabase.domain.FormulaCalculationBasis;
import com.formulabase.repository.ProductionHistoryRepository;
import com.formulabase.repository.ProductionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ProductionExecutionService {
    private final ProductionPlanRepository planRepository;
    private final ProductionHistoryRepository historyRepository;
    private final QuantityCalculationService quantityCalculationService;
    private final AuditLogService audit;

    public ProductionExecutionService(ProductionPlanRepository planRepository,
                                      ProductionHistoryRepository historyRepository,
                                      QuantityCalculationService quantityCalculationService, AuditLogService audit) {
        this.planRepository = planRepository;
        this.historyRepository = historyRepository;
        this.quantityCalculationService = quantityCalculationService;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<ProductionPlan> tasksForWorker(User worker, LocalDate today) {
        if (worker != null && worker.getRole() == UserRole.CEO) return planRepository.findAll();
        if (worker == null || worker.getRole() != UserRole.WORKER) {
            throw new IllegalArgumentException("Only workers can view worker tasks");
        }
        return planRepository.findByAssignedWorkerIdAndPlannedDateLessThanEqualAndStatusInOrderByPlannedDateAsc(
                worker.getId(), today,
                List.of(ProductionPlanStatus.PLANNED, ProductionPlanStatus.IN_PROGRESS,
                        ProductionPlanStatus.LOSS_EXCEPTION)).stream()
                .filter(p -> p.getPlanningCycle().getStatus() == com.formulabase.domain.MonthlyPlanningCycleStatus.LOCKED).toList();
    }

    @Transactional(readOnly = true)
    public boolean hasAssignedPlanForFormula(UUID formulaId, User worker) {
        return worker != null && worker.getRole() == UserRole.WORKER
                && planRepository.existsByFormulaIdAndAssignedWorkerId(formulaId, worker.getId());
    }

    @Transactional
    public ProductionPlan start(UUID planId, User worker, BigDecimal lossPercentage) {
        ProductionPlan plan = getPlan(planId);
        assertAssignedWorker(plan, worker);
        if (plan.getPlanningCycle().getStatus() != com.formulabase.domain.MonthlyPlanningCycleStatus.LOCKED)
            throw new IllegalStateException("Lock the monthly plan before starting production");
        if (plan.getPlannedDate().isAfter(LocalDate.now())) throw new IllegalStateException("This task is scheduled for a future date");
        if (lossPercentage == null) throw new IllegalArgumentException("Loss percentage is required before Start");
        var result = quantityCalculationService.calculate(plan.getTargetQuantity(), lossPercentage);
        plan.start(lossPercentage, new ProductionPlan.QuantitySnapshot(
                result.lossAllowanceQuantity(), result.plannedQuantity()));
        audit.record(worker, "START_BATCH", "PLAN", plan.getId(), "Loss allowance " + lossPercentage + "%");
        return plan;
    }

    @Transactional
    public ProductionHistory complete(UUID planId, User worker, int actualProduced, int rejected) {
        ProductionPlan plan = getPlan(planId);
        assertAssignedWorker(plan, worker);
        if (!plan.getStepExecutions().isEmpty()
                && plan.getStepExecutions().stream().anyMatch(step -> step.getStatus() != com.formulabase.domain.ExecutionStatus.COMPLETED)) {
            throw new IllegalStateException("Complete every production step before recording the final batch output");
        }
        plan.complete(actualProduced, rejected);
        ProductionHistory history = historyRepository.save(new ProductionHistory(
                plan, actualProduced, rejected, plan.getGoodQuantity(), plan.getSurplusQuantity()));
        audit.record(worker, "COMPLETE_BATCH", "PLAN", plan.getId(), "Actual " + actualProduced + ", rejected " + rejected);
        return history;
    }

    @Transactional(readOnly = true)
    public ProductionPlan getPlan(UUID planId) {
        return planRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("Production plan not found"));
    }

    @Transactional(readOnly = true)
    public List<MaterialRequirement> materialRequirements(UUID planId) {
        var plan = getPlan(planId);
        int units = plan.getPlannedQuantity() == null ? plan.getTargetQuantity() : plan.getPlannedQuantity();
        boolean percentFormula = plan.getFormula().getCalculationBasis() == FormulaCalculationBasis.PERCENT_W_W;
        BigDecimal batchMassGrams = percentFormula
                ? plan.getFormula().getProductUnitWeightGrams().multiply(BigDecimal.valueOf(units))
                : null;
        return plan.getFormula().getMaterials().stream().map(item -> {
            if (percentFormula) {
                BigDecimal total = batchMassGrams.multiply(item.getPercentage()).movePointLeft(2);
                BigDecimal perPiece = plan.getFormula().getProductUnitWeightGrams().multiply(item.getPercentage()).movePointLeft(2);
                return new MaterialRequirement(item.getPhase().name(), item.getMaterial().getCode(), item.getMaterial().getName(),
                        "% w/w", "G", item.getPercentage(), perPiece, total);
            }
            return new MaterialRequirement(item.getPhase().name(), item.getMaterial().getCode(), item.getMaterial().getName(),
                    item.getUnit().name(), item.getUnit().name(), null, item.getQuantityPerUnit(),
                    item.getQuantityPerUnit().multiply(BigDecimal.valueOf(units)));
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<com.formulabase.domain.StepExecution> steps(UUID planId) { return getPlan(planId).getStepExecutions(); }

    @Transactional
    public void startStep(UUID planId, UUID stepId, User actor) {
        ProductionPlan plan = getPlan(planId); assertAssignedWorker(plan, actor);
        if (plan.getStatus() != ProductionPlanStatus.IN_PROGRESS) throw new IllegalStateException("Start the batch before a production step");
        var steps = plan.getStepExecutions();
        var current = steps.stream().filter(s -> s.getId().equals(stepId)).findFirst().orElseThrow(() -> new IllegalArgumentException("Production step not found"));
        boolean previousDone = steps.stream().filter(s -> s.getStep().getStepNumber() < current.getStep().getStepNumber()).allMatch(s -> s.getStatus() == com.formulabase.domain.ExecutionStatus.COMPLETED);
        if (!previousDone) throw new IllegalStateException("Complete earlier steps first");
        current.start(actor); audit.record(actor, "START_STEP", "STEP", current.getId(), current.getStep().getInstruction());
    }

    @Transactional
    public void completeStep(UUID planId, UUID stepId, BigDecimal temperature, User actor) {
        ProductionPlan plan = getPlan(planId); assertAssignedWorker(plan, actor);
        var step = plan.getStepExecutions().stream().filter(s -> s.getId().equals(stepId)).findFirst().orElseThrow(() -> new IllegalArgumentException("Production step not found"));
        step.complete(temperature);
        audit.record(actor, step.isOverdue() ? "COMPLETE_STEP_OVERDUE" : "COMPLETE_STEP", "STEP", step.getId(), step.getStep().getInstruction());
    }

    @Transactional
    public void acknowledgeStepAlert(UUID planId, UUID stepId, String note, User actor) {
        ProductionPlan plan = getPlan(planId); assertAssignedWorker(plan, actor);
        var step = plan.getStepExecutions().stream().filter(s -> s.getId().equals(stepId)).findFirst().orElseThrow(() -> new IllegalArgumentException("Production step not found"));
        step.acknowledge(note, actor); audit.record(actor, "ACKNOWLEDGE_OVERDUE", "STEP", step.getId(), note.trim());
    }

    /** perUnit is retained for the legacy work-order layout; percentage is set only for % w/w formulas. */
    public record MaterialRequirement(String phase, String code, String name, String perUnitUnit, String totalUnit,
                                      BigDecimal percentage, BigDecimal perUnit, BigDecimal total) { }

    private void assertAssignedWorker(ProductionPlan plan, User worker) {
        if (worker != null && worker.getRole() == UserRole.CEO) return;
        if (worker == null || worker.getRole() != UserRole.WORKER
                || plan.getAssignedWorker() == null
                || !plan.getAssignedWorker().getId().equals(worker.getId())) {
            throw new IllegalArgumentException("This batch is not assigned to the current worker");
        }
    }
}
