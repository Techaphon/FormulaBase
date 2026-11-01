package com.formulabase.repository;

import com.formulabase.domain.ProductionPlan;
import com.formulabase.domain.ProductionPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, UUID> {
    List<ProductionPlan> findByPlanningCycleIdOrderByPlannedDateAsc(UUID planningCycleId);
    List<ProductionPlan> findByPlannedDateOrderByPlannedDateAsc(LocalDate plannedDate);
    List<ProductionPlan> findByPlannedDateAndStatusInOrderByPlannedDateAsc(LocalDate plannedDate, List<ProductionPlanStatus> statuses);
    List<ProductionPlan> findByAssignedWorkerIdAndPlannedDateLessThanEqualAndStatusInOrderByPlannedDateAsc(
            UUID workerId, LocalDate plannedDate, List<com.formulabase.domain.ProductionPlanStatus> statuses);
    long countByStatus(ProductionPlanStatus status);
    long countByStatusAndQualityReviewedAtIsNull(ProductionPlanStatus status);
    long countByPlannedDateAndStatusIn(LocalDate plannedDate, List<ProductionPlanStatus> statuses);
    boolean existsByFormulaIdAndAssignedWorkerId(UUID formulaId, UUID workerId);
}
