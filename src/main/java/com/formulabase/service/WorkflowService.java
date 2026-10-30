package com.formulabase.service;

import com.formulabase.domain.FormulaStatus;
import com.formulabase.domain.MonthlyPlanningCycleStatus;
import com.formulabase.domain.ProductionPlanStatus;
import com.formulabase.repository.FormulaRepository;
import com.formulabase.repository.MonthlyPlanningCycleRepository;
import com.formulabase.repository.ProductionPlanRepository;
import com.formulabase.domain.Formula;
import com.formulabase.domain.MonthlyPlanningCycle;
import com.formulabase.domain.ProductionPlan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Read-only orchestration view of the hand-offs between QC, CEO, PC and Worker. */
@Service
public class WorkflowService {
    private final FormulaRepository formulaRepository;
    private final MonthlyPlanningCycleRepository cycleRepository;
    private final ProductionPlanRepository planRepository;

    public WorkflowService(FormulaRepository formulaRepository,
                           MonthlyPlanningCycleRepository cycleRepository,
                           ProductionPlanRepository planRepository) {
        this.formulaRepository = formulaRepository;
        this.cycleRepository = cycleRepository;
        this.planRepository = planRepository;
    }

    @Transactional(readOnly = true)
    public WorkflowSnapshot snapshot(LocalDate today) {
        return new WorkflowSnapshot(
                formulaRepository.countByStatus(FormulaStatus.DRAFT),
                formulaRepository.countByStatus(FormulaStatus.PENDING_APPROVAL),
                formulaRepository.countByStatus(FormulaStatus.CONFIRMED),
                cycleRepository.countByStatus(MonthlyPlanningCycleStatus.DRAFT),
                cycleRepository.countByStatus(MonthlyPlanningCycleStatus.LOCKED),
                planRepository.countByStatus(ProductionPlanStatus.PLANNED),
                planRepository.countByStatus(ProductionPlanStatus.IN_PROGRESS),
                planRepository.countByStatus(ProductionPlanStatus.COMPLETED),
                planRepository.countByStatusAndQualityReviewedAtIsNull(ProductionPlanStatus.LOSS_EXCEPTION),
                planRepository.countByPlannedDateAndStatusIn(today,
                        List.of(ProductionPlanStatus.PLANNED, ProductionPlanStatus.IN_PROGRESS,
                                ProductionPlanStatus.LOSS_EXCEPTION)));
    }

    @Transactional(readOnly = true)
    public List<Formula> pendingApprovals() {
        return formulaRepository.findByStatusOrderByDocumentNoAsc(FormulaStatus.PENDING_APPROVAL);
    }

    @Transactional(readOnly = true)
    public List<MonthlyPlanningCycle> draftCycles() {
        return cycleRepository.findByStatusOrderByPlanningMonthDesc(MonthlyPlanningCycleStatus.DRAFT);
    }

    @Transactional(readOnly = true)
    public List<ProductionPlan> todayTasks(LocalDate today) {
        return planRepository.findByPlannedDateAndStatusInOrderByPlannedDateAsc(today,
                List.of(ProductionPlanStatus.PLANNED, ProductionPlanStatus.IN_PROGRESS,
                        ProductionPlanStatus.LOSS_EXCEPTION));
    }

    public record WorkflowSnapshot(long draftFormulas, long pendingFormulaApprovals,
                                   long confirmedFormulas, long draftCycles, long lockedCycles,
                                   long plannedTasks, long inProgressTasks, long completedTasks,
                                   long lossExceptions, long dueTodayTasks) {
    }
}
