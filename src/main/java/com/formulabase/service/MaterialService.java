package com.formulabase.service;

import com.formulabase.domain.Material;
import com.formulabase.domain.MaterialUnit;
import com.formulabase.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaterialService {
    private final MaterialRepository repository;
    private final com.formulabase.repository.FormulaMaterialRepository formulaMaterials;
    private final AuditLogService audit;

    public MaterialService(MaterialRepository repository, com.formulabase.repository.FormulaMaterialRepository formulaMaterials, AuditLogService audit) {
        this.repository = repository;
        this.formulaMaterials = formulaMaterials;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<Material> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return repository.findAll();
        return repository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrderByCode(keyword, keyword);
    }

    @Transactional
    public Material create(String code, String name, MaterialUnit unit) {
        return create(code, name, unit, null);
    }

    @Transactional
    public Material create(String code, String name, MaterialUnit unit, com.formulabase.domain.User actor) {
        if (code == null || code.isBlank() || name == null || name.isBlank() || unit == null) {
            throw new IllegalArgumentException("Code, name and unit are required");
        }
        if (repository.existsByCodeIgnoreCase(code.trim())) {
            throw new IllegalArgumentException("Material code already exists");
        }
        Material material = repository.save(new Material(code.trim(), name.trim(), unit));
        if (actor != null) audit.record(actor, "CREATE_MATERIAL", "MATERIAL", material.getId(), "Created " + material.getCode());
        return material;
    }

    @Transactional
    public void createBatch(List<String> codes, List<String> names, List<MaterialUnit> units, com.formulabase.domain.User actor) {
        if (codes == null || names == null || units == null || codes.isEmpty() || codes.size() != names.size() || codes.size() != units.size())
            throw new IllegalArgumentException("Code, name and unit are required");
        for (int i = 0; i < codes.size(); i++) create(codes.get(i), names.get(i), units.get(i), actor);
    }

    @Transactional
    public void delete(java.util.UUID id, com.formulabase.domain.User actor) {
        if (formulaMaterials.countByMaterialId(id) > 0) throw new IllegalStateException("Material is used in a formula and cannot be deleted");
        Material material = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Material not found"));
        audit.record(actor, "DELETE_MATERIAL", "MATERIAL", id, "Deleted " + material.getCode());
        repository.delete(material);
    }
}
