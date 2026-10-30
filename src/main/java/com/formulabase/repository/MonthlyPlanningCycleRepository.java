package com.formulabase.repository;

import com.formulabase.domain.MonthlyPlanningCycle;
import com.formulabase.domain.MonthlyPlanningCycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MonthlyPlanningCycleRepository extends JpaRepository<MonthlyPlanningCycle, UUID> {
    boolean existsByPlanningMonth(String planningMonth);
    List<MonthlyPlanningCycle> findAllByOrderByPlanningMonthDesc();
    List<MonthlyPlanningCycle> findByStatusOrderByPlanningMonthDesc(MonthlyPlanningCycleStatus status);
    long countByStatus(MonthlyPlanningCycleStatus status);
}
