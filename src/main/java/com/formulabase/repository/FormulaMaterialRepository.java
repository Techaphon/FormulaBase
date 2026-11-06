package com.formulabase.repository;
import com.formulabase.domain.FormulaMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface FormulaMaterialRepository extends JpaRepository<FormulaMaterial, UUID> {
 long countByMaterialId(UUID materialId);
 @org.springframework.data.jpa.repository.Query("select distinct fm.material.id from FormulaMaterial fm")
 java.util.Set<UUID> findUsedMaterialIds();
}
