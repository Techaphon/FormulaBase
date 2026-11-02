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
import java.util.EnumMap;

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
        var formulas = counts(formulaRepository.countGroupedByStatus(), FormulaStatus.class);
        var cycles = counts(cycleRepository.countGroupedByStatus(), MonthlyPlanningCycleStatus.class);
        var plans = planCounts(planRepository.countGroupedByStatusWithDueDate(today));
        return new WorkflowSnapshot(
                formulas.getOrDefault(FormulaStatus.DRAFT, 0L),
                formulas.getOrDefault(FormulaStatus.PENDING_APPROVAL, 0L),
                formulas.getOrDefault(FormulaStatus.CONFIRMED, 0L),
                cycles.getOrDefault(MonthlyPlanningCycleStatus.DRAFT, 0L),
                cycles.getOrDefault(MonthlyPlanningCycleStatus.LOCKED, 0L),
                plans.getOrDefault(ProductionPlanStatus.PLANNED, new Count(0, 0)).total(),
                plans.getOrDefault(ProductionPlanStatus.IN_PROGRESS, new Count(0, 0)).total(),
                plans.getOrDefault(ProductionPlanStatus.COMPLETED, new Count(0, 0)).total(),
                plans.getOrDefault(ProductionPlanStatus.LOSS_EXCEPTION, new Count(0, 0)).total(),
                plans.entrySet().stream().filter(entry -> entry.getKey() == ProductionPlanStatus.PLANNED
                                || entry.getKey() == ProductionPlanStatus.IN_PROGRESS
                                || entry.getKey() == ProductionPlanStatus.LOSS_EXCEPTION)
                        .mapToLong(entry -> entry.getValue().dueToday()).sum());
    }

    private <E extends Enum<E>> EnumMap<E, Long> counts(List<Object[]> rows, Class<E> type) {
        var result = new EnumMap<E, Long>(type);
        for (Object[] row : rows) result.put(type.cast(row[0]), ((Number) row[1]).longValue());
        return result;
    }

    private EnumMap<ProductionPlanStatus, Count> planCounts(List<Object[]> rows) {
        var result = new EnumMap<ProductionPlanStatus, Count>(ProductionPlanStatus.class);
        for (Object[] row : rows) {
            result.put((ProductionPlanStatus) row[0], new Count(((Number) row[1]).longValue(),
                    row[2] == null ? 0 : ((Number) row[2]).longValue()));
        }
        return result;
    }

    private record Count(long total, long dueToday) { }

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
