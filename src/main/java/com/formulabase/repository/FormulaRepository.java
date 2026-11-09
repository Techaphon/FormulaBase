package com.formulabase.repository;

import com.formulabase.domain.Formula;
import com.formulabase.domain.FormulaStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FormulaRepository extends JpaRepository<Formula, UUID> {
    List<Formula> findAllByOrderByDocumentNoAsc();
    boolean existsByDocumentNoIgnoreCase(String documentNo);
    boolean existsByDocumentNoIgnoreCaseAndIdNot(String documentNo, java.util.UUID id);
    List<Formula> findByStatusOrderByDocumentNoAsc(FormulaStatus status);
    @Query("select f.status, count(f) from Formula f group by f.status")
    List<Object[]> countGroupedByStatus();
    // Fetch the material graph explicitly. Steps are already EAGER; fetching both bags in one
    // SQL query causes Hibernate's MultipleBagFetchException.
    @EntityGraph(attributePaths = {"materials", "materials.material"})
    @Query("select distinct f from Formula f where f.id = :id")
    java.util.Optional<Formula> findWithDetailsById(@Param("id") UUID id);
    @Query("select f.id from Formula f where f.status in :statuses")
    List<UUID> findIdsByStatusIn(@Param("statuses") java.util.Collection<FormulaStatus> statuses);
    long countByStatus(FormulaStatus status);
}
