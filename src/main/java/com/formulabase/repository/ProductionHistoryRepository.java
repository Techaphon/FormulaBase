package com.formulabase.repository;

import com.formulabase.domain.ProductionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductionHistoryRepository extends JpaRepository<ProductionHistory, UUID> {
    boolean existsByProductionPlanId(UUID productionPlanId);
}
