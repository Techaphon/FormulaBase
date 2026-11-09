package com.formulabase.repository;

import com.formulabase.domain.MonthlyPlanningCycle;
import com.formulabase.domain.MonthlyPlanningCycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MonthlyPlanningCycleRepository extends JpaRepository<MonthlyPlanningCycle, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from MonthlyPlanningCycle c where c.id = :id")
    java.util.Optional<MonthlyPlanningCycle> findLockedById(@org.springframework.data.repository.query.Param("id") UUID id);
    boolean existsByPlanningMonth(String planningMonth);
    List<MonthlyPlanningCycle> findAllByOrderByPlanningMonthDesc();
    List<MonthlyPlanningCycle> findByStatusOrderByPlanningMonthDesc(MonthlyPlanningCycleStatus status);
    @Query("select c.status, count(c) from MonthlyPlanningCycle c group by c.status")
    List<Object[]> countGroupedByStatus();
    long countByStatus(MonthlyPlanningCycleStatus status);
}
