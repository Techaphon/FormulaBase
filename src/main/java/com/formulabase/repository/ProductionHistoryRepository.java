package com.formulabase.repository;

import com.formulabase.domain.ProductionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface ProductionHistoryRepository extends JpaRepository<ProductionHistory, UUID> {
    boolean existsByProductionPlanId(UUID productionPlanId);
    List<ProductionHistory> findAllByOrderByCompletedAtDesc();
}
