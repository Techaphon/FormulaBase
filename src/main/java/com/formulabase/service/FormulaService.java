package com.formulabase.service;

import com.formulabase.domain.Formula;
import com.formulabase.domain.FormulaStatus;
import com.formulabase.domain.Material;
import com.formulabase.domain.User;
import com.formulabase.domain.UserRole;
import com.formulabase.domain.MaterialUnit;
import com.formulabase.repository.FormulaRepository;
import com.formulabase.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FormulaService {
    private final FormulaRepository formulaRepository;
    private final MaterialRepository materialRepository;

    public FormulaService(FormulaRepository formulaRepository, MaterialRepository materialRepository) {
        this.formulaRepository = formulaRepository;
        this.materialRepository = materialRepository;
    }

    @Transactional(readOnly = true)
    public List<Formula> findAll() {
        return formulaRepository.findAllByOrderByDocumentNoAsc();
    }

    @Transactional
    public Formula create(String documentNo, String name, List<String> materialIds,
                          List<BigDecimal> quantitiesPerUnit) {
        return createDraft(documentNo, name, materialIds, null, null, null, quantitiesPerUnit);
    }

    @Transactional
    public Formula create(String documentNo, String name, List<String> materialIds,
                          List<BigDecimal> quantitiesPerUnit, User actor) {
        requireRole(actor, UserRole.QC, UserRole.CEO);
        return createDraft(documentNo, name, materialIds, null, null, null, quantitiesPerUnit);
    }

    @Transactional
    public Formula create(String documentNo, String name, List<String> materialIds,
                          List<String> newMaterialCodes, List<String> newMaterialNames,
                          List<String> newMaterialUnits, List<BigDecimal> quantitiesPerUnit,
                          User actor) {
        requireRole(actor, UserRole.QC, UserRole.CEO);
        return createDraft(documentNo, name, materialIds, newMaterialCodes, newMaterialNames,
                newMaterialUnits, quantitiesPerUnit);
    }

    private Formula createDraft(String documentNo, String name, List<String> materialIds,
                                List<String> newMaterialCodes, List<String> newMaterialNames,
                                List<String> newMaterialUnits, List<BigDecimal> quantitiesPerUnit) {
        if (documentNo == null || documentNo.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Document number and formula name are required");
        }
        if (formulaRepository.existsByDocumentNoIgnoreCase(documentNo.trim())) {
            throw new IllegalArgumentException("Document number already exists");
        }
        if ((materialIds == null || materialIds.isEmpty()) && quantitiesPerUnit != null && quantitiesPerUnit.size() == 1) materialIds = java.util.Collections.singletonList("");
        if (materialIds == null || materialIds.isEmpty() || quantitiesPerUnit == null
                || materialIds.size() != quantitiesPerUnit.size()) {
            throw new IllegalArgumentException("At least one material row is required");
        }

        Formula formula = new Formula(documentNo.trim(), name.trim());
        for (int i = 0; i < materialIds.size(); i++) {
            String selectedId = valueAt(materialIds, i);
            Material material;
            if (selectedId != null && !selectedId.isBlank()) {
                material = materialRepository.findById(java.util.UUID.fromString(selectedId))
                        .orElseThrow(() -> new IllegalArgumentException("Material not found"));
            } else {
                String code = valueAt(newMaterialCodes, i);
                String materialName = valueAt(newMaterialNames, i);
                String unitValue = valueAt(newMaterialUnits, i);
                if (code == null || code.isBlank() || materialName == null || materialName.isBlank()
                        || unitValue == null || unitValue.isBlank()) {
                    throw new IllegalArgumentException("Select an existing material or fill in new material code, name and unit");
                }
                MaterialUnit unit;
                try { unit = MaterialUnit.valueOf(unitValue); }
                catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Invalid material unit"); }
                material = materialRepository.findByCodeIgnoreCase(code.trim())
                        .orElseGet(() -> materialRepository.save(new Material(code.trim(), materialName.trim(), unit)));
            }
            formula.addMaterial(material, quantitiesPerUnit.get(i));
        }
        return formulaRepository.save(formula);
    }

    private <T> T valueAt(List<T> values, int index) {
        return values != null && index < values.size() ? values.get(index) : null;
    }

    @Transactional
    public void submitForApproval(java.util.UUID id, User actor) {
        requireRole(actor, UserRole.QC, UserRole.CEO);
        Formula formula = formulaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formula not found"));
        if (formula.getStatus() != FormulaStatus.DRAFT && formula.getStatus() != FormulaStatus.REJECTED) {
            throw new IllegalStateException("Only draft or rejected formulas can be submitted");
        }
        formula.setStatus(FormulaStatus.PENDING_APPROVAL);
    }

    @Transactional
    public void confirm(java.util.UUID id, User actor) {
        requireRole(actor, UserRole.CEO);
        Formula formula = formulaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formula not found"));
        if (formula.getStatus() != FormulaStatus.DRAFT && formula.getStatus() != FormulaStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only draft or pending formulas can be confirmed");
        }
        formula.setStatus(FormulaStatus.CONFIRMED);
    }

    private void requireRole(User actor, UserRole... allowedRoles) {
        if (actor == null) throw new IllegalStateException("Please sign in first");
        for (UserRole role : allowedRoles) {
            if (actor.getRole() == role) return;
        }
        throw new IllegalStateException("You do not have permission for this action");
    }
}
