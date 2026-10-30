package com.formulabase.service;

import com.formulabase.domain.ProductionHistory;
import com.formulabase.domain.ProductionPlan;
import com.formulabase.domain.ProductionPlanStatus;
import com.formulabase.domain.User;
import com.formulabase.domain.UserRole;
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

    public ProductionExecutionService(ProductionPlanRepository planRepository,
                                      ProductionHistoryRepository historyRepository,
                                      QuantityCalculationService quantityCalculationService) {
        this.planRepository = planRepository;
        this.historyRepository = historyRepository;
        this.quantityCalculationService = quantityCalculationService;
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
        return plan;
    }

    @Transactional
    public ProductionHistory complete(UUID planId, User worker, int actualProduced, int rejected) {
        ProductionPlan plan = getPlan(planId);
        assertAssignedWorker(plan, worker);
        plan.complete(actualProduced, rejected);
        return historyRepository.save(new ProductionHistory(
                plan, actualProduced, rejected, plan.getGoodQuantity(), plan.getSurplusQuantity()));
    }

    @Transactional(readOnly = true)
    public ProductionPlan getPlan(UUID planId) {
        return planRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("Production plan not found"));
    }

    @Transactional(readOnly = true)
    public List<MaterialRequirement> materialRequirements(UUID planId) {
        var plan = getPlan(planId);
        int units = plan.getPlannedQuantity() == null ? plan.getTargetQuantity() : plan.getPlannedQuantity();
        return plan.getFormula().getMaterials().stream().map(item -> new MaterialRequirement(
                item.getMaterial().getName(), item.getUnit().name(), item.getQuantityPerUnit(),
                item.getQuantityPerUnit().multiply(BigDecimal.valueOf(units)))).toList();
    }

    public record MaterialRequirement(String name, String unit, BigDecimal perUnit, BigDecimal total) { }

    private void assertAssignedWorker(ProductionPlan plan, User worker) {
        if (worker != null && worker.getRole() == UserRole.CEO) return;
        if (worker == null || worker.getRole() != UserRole.WORKER
                || plan.getAssignedWorker() == null
                || !plan.getAssignedWorker().getId().equals(worker.getId())) {
            throw new IllegalArgumentException("This batch is not assigned to the current worker");
        }
    }
}
