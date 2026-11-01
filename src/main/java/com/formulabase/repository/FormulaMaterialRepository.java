package com.formulabase.repository;
import com.formulabase.domain.FormulaMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface FormulaMaterialRepository extends JpaRepository<FormulaMaterial, UUID> { long countByMaterialId(UUID materialId); }
