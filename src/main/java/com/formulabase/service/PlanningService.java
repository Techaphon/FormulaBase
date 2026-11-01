package com.formulabase.service;

import com.formulabase.domain.Formula;
import com.formulabase.domain.MonthlyPlanningCycle;
import com.formulabase.domain.MonthlyPlanningCycleStatus;
import com.formulabase.domain.ProductionPlan;
import com.formulabase.repository.FormulaRepository;
import com.formulabase.repository.MonthlyPlanningCycleRepository;
import com.formulabase.repository.ProductionPlanRepository;
import com.formulabase.repository.UserRepository;
import com.formulabase.domain.User;
import com.formulabase.domain.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class PlanningService {
    private final MonthlyPlanningCycleRepository cycleRepository;
    private final ProductionPlanRepository planRepository;
    private final FormulaRepository formulaRepository;
    private final UserRepository userRepository;
    private final AuditLogService audit;

    public PlanningService(MonthlyPlanningCycleRepository cycleRepository,
                           ProductionPlanRepository planRepository,
                           FormulaRepository formulaRepository,
                           UserRepository userRepository, AuditLogService audit) {
        this.cycleRepository = cycleRepository;
        this.planRepository = planRepository;
        this.formulaRepository = formulaRepository;
        this.userRepository = userRepository;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<MonthlyPlanningCycle> cycles() { return cycleRepository.findAllByOrderByPlanningMonthDesc(); }

    @Transactional
    public MonthlyPlanningCycle createCycle(String planningMonth) {
        return createCycle(planningMonth, null);
    }

    @Transactional
    public MonthlyPlanningCycle createCycle(String planningMonth, User actor) {
        if (planningMonth == null || !planningMonth.matches("\\d{4}-\\d{2}")) throw new IllegalArgumentException("Select a valid planning month");
        if (cycleRepository.existsByPlanningMonth(planningMonth)) {
            throw new IllegalArgumentException("Planning cycle already exists for this month");
        }
        MonthlyPlanningCycle cycle = cycleRepository.save(new MonthlyPlanningCycle(planningMonth));
        if (actor != null) audit.record(actor, "CREATE_PLANNING_CYCLE", "CYCLE", cycle.getId(), "Created " + planningMonth);
        return cycle;
    }

    @Transactional(readOnly = true)
    public MonthlyPlanningCycle getCycle(UUID id) {
        return cycleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Planning cycle not found"));
    }

    @Transactional(readOnly = true)
    public List<ProductionPlan> plans(UUID cycleId) { return planRepository.findByPlanningCycleIdOrderByPlannedDateAsc(cycleId); }

    @Transactional(readOnly = true)
    public UUID getPlanCycleId(UUID planId) { return planRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("Production plan not found")).getPlanningCycle().getId(); }

    @Transactional
    public ProductionPlan addPlan(UUID cycleId, UUID formulaId, UUID workerId, LocalDate plannedDate, int targetQuantity) {
        return addPlan(cycleId, formulaId, workerId, plannedDate, targetQuantity, null);
    }

    @Transactional
    public ProductionPlan addPlan(UUID cycleId, UUID formulaId, UUID workerId, LocalDate plannedDate, int targetQuantity, User actor) {
        MonthlyPlanningCycle cycle = getCycle(cycleId);
        if (cycle.getStatus() == MonthlyPlanningCycleStatus.LOCKED && (actor == null || actor.getRole() != UserRole.CEO)) {
            throw new IllegalStateException("Locked planning cycle cannot be edited");
        }
        Formula formula = formulaRepository.findById(formulaId)
                .orElseThrow(() -> new IllegalArgumentException("Formula not found"));
        if (formula.getStatus() != com.formulabase.domain.FormulaStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only confirmed formulas can be planned");
        }
        if (plannedDate == null) throw new IllegalArgumentException("Planned date is required");
        if (!plannedDate.toString().startsWith(cycle.getPlanningMonth())) {
            throw new IllegalArgumentException("Planned date must be inside the selected month");
        }
        if (targetQuantity <= 0) throw new IllegalArgumentException("Target quantity must be greater than zero");
        if (plannedDate == null) throw new IllegalArgumentException("Planned date is required");
        ProductionPlan plan = new ProductionPlan(cycle, formula, plannedDate, targetQuantity);
        if (workerId != null) {
            plan.assignWorker(userRepository.findById(workerId)
                    .orElseThrow(() -> new IllegalArgumentException("Worker not found")));
        }
        for (var step : formula.getSteps()) plan.addStepExecution(step);
        ProductionPlan saved = planRepository.save(plan);
        if (actor != null) audit.record(actor, "CREATE_PRODUCTION_PLAN", "PLAN", saved.getId(), "Created task for " + plannedDate);
        return saved;
    }

    @Transactional
    public void lockCycle(UUID cycleId) {
        lockCycle(cycleId, null);
    }

    @Transactional
    public void lockCycle(UUID cycleId, User actor) {
        MonthlyPlanningCycle cycle = getCycle(cycleId);
        if (planRepository.findByPlanningCycleIdOrderByPlannedDateAsc(cycleId).isEmpty()) throw new IllegalStateException("Add at least one production task before locking the month");
        cycle.setStatus(MonthlyPlanningCycleStatus.LOCKED);
        if (actor != null) audit.record(actor, "LOCK_PLANNING_CYCLE", "CYCLE", cycle.getId(), "Locked " + cycle.getPlanningMonth());
    }

    @Transactional
    public void reschedule(UUID planId, LocalDate date, UUID workerId, String reason, User actor) {
        if (actor == null || (actor.getRole() != UserRole.CEO && actor.getRole() != UserRole.PC)) throw new IllegalStateException("Only CEO or PC can reschedule a plan");
        ProductionPlan plan = planRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("Production plan not found"));
        if (date == null || !date.toString().startsWith(plan.getPlanningCycle().getPlanningMonth())) throw new IllegalArgumentException("New date must be within the planning month");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Reschedule reason is required");
        OffsetDateTime deadline = plan.getPlannedDate().minusDays(1).atTime(23, 59, 59).atOffset(ZoneOffset.ofHours(7));
        if (OffsetDateTime.now().isAfter(deadline)) throw new IllegalStateException("Rescheduling closes at 23:59:59 on the day before production");
        LocalDate oldDate = plan.getPlannedDate();
        plan.reschedule(date);
        if (workerId != null) plan.assignWorker(userRepository.findById(workerId).orElseThrow(() -> new IllegalArgumentException("Worker not found")));
        plan.addRescheduleLog(oldDate, date, reason.trim(), actor);
        audit.record(actor, "RESCHEDULE_PLAN", "PLAN", plan.getId(), oldDate + " → " + date + "; worker " + (plan.getAssignedWorker() == null ? "unassigned" : plan.getAssignedWorker().getUsername()) + ": " + reason.trim());
    }

    @Transactional
    public void cancel(UUID planId, User actor) {
        if (actor == null || (actor.getRole() != UserRole.CEO && actor.getRole() != UserRole.PC)) throw new IllegalStateException("Only CEO or PC can cancel a plan");
        ProductionPlan plan = planRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("Production plan not found"));
        if (plan.getPlanningCycle().getStatus() == MonthlyPlanningCycleStatus.LOCKED && actor.getRole() != UserRole.CEO) throw new IllegalStateException("Only CEO can change a locked month");
        plan.cancel();
        audit.record(actor, "CANCEL_PLAN", "PLAN", plan.getId(), "Cancelled task on " + plan.getPlannedDate());
    }
}
