package com.formulabase.repository;

import com.formulabase.domain.Formula;
import com.formulabase.domain.FormulaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FormulaRepository extends JpaRepository<Formula, UUID> {
    List<Formula> findAllByOrderByDocumentNoAsc();
    boolean existsByDocumentNoIgnoreCase(String documentNo);
    List<Formula> findByStatusOrderByDocumentNoAsc(FormulaStatus status);
    long countByStatus(FormulaStatus status);
}
