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
import java.util.List;
import java.util.UUID;

@Service
public class PlanningService {
    private final MonthlyPlanningCycleRepository cycleRepository;
    private final ProductionPlanRepository planRepository;
    private final FormulaRepository formulaRepository;
    private final UserRepository userRepository;

    public PlanningService(MonthlyPlanningCycleRepository cycleRepository,
                           ProductionPlanRepository planRepository,
                           FormulaRepository formulaRepository,
                           UserRepository userRepository) {
        this.cycleRepository = cycleRepository;
        this.planRepository = planRepository;
        this.formulaRepository = formulaRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<MonthlyPlanningCycle> cycles() { return cycleRepository.findAllByOrderByPlanningMonthDesc(); }

    @Transactional
    public MonthlyPlanningCycle createCycle(String planningMonth) {
        if (planningMonth == null || !planningMonth.matches("\\d{4}-\\d{2}")) throw new IllegalArgumentException("Select a valid planning month");
        if (cycleRepository.existsByPlanningMonth(planningMonth)) {
            throw new IllegalArgumentException("Planning cycle already exists for this month");
        }
        return cycleRepository.save(new MonthlyPlanningCycle(planningMonth));
    }

    @Transactional(readOnly = true)
    public MonthlyPlanningCycle getCycle(UUID id) {
        return cycleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Planning cycle not found"));
    }

    @Transactional(readOnly = true)
    public List<ProductionPlan> plans(UUID cycleId) { return planRepository.findByPlanningCycleIdOrderByPlannedDateAsc(cycleId); }

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
        return planRepository.save(plan);
    }

    @Transactional
    public void lockCycle(UUID cycleId) {
        MonthlyPlanningCycle cycle = getCycle(cycleId);
        if (planRepository.findByPlanningCycleIdOrderByPlannedDateAsc(cycleId).isEmpty()) throw new IllegalStateException("Add at least one production task before locking the month");
        cycle.setStatus(MonthlyPlanningCycleStatus.LOCKED);
    }

    @Transactional
    public void reschedule(UUID planId, LocalDate date, User actor) {
        if (actor == null || (actor.getRole() != UserRole.CEO && actor.getRole() != UserRole.PC)) throw new IllegalStateException("Only CEO or PC can reschedule a plan");
        ProductionPlan plan = planRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("Production plan not found"));
        if (date == null || !date.toString().startsWith(plan.getPlanningCycle().getPlanningMonth())) throw new IllegalArgumentException("New date must be within the planning month");
        plan.reschedule(date);
    }
}
