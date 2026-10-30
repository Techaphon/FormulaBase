package com.formulabase.repository;

import com.formulabase.domain.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaterialRepository extends JpaRepository<Material, UUID> {
    Optional<Material> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    List<Material> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrderByCode(String name, String code);
}
