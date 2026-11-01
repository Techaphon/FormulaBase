package com.formulabase.service;

import com.formulabase.domain.Formula;
import com.formulabase.domain.FormulaStatus;
import com.formulabase.domain.Material;
import com.formulabase.domain.User;
import com.formulabase.domain.UserRole;
import com.formulabase.domain.MaterialUnit;
import com.formulabase.domain.FormulaPhase;
import com.formulabase.domain.FormulaCalculationBasis;
import com.formulabase.repository.FormulaRepository;
import com.formulabase.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class FormulaService {
    private final FormulaRepository formulaRepository;
    private final MaterialRepository materialRepository;
    private final AuditLogService audit;

    public FormulaService(FormulaRepository formulaRepository, MaterialRepository materialRepository, AuditLogService audit) {
        this.formulaRepository = formulaRepository;
        this.materialRepository = materialRepository;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<Formula> findAll() {
        return formulaRepository.findAllByOrderByDocumentNoAsc();
    }

    /** Loads one formula for a document view without relying on a client-side list lookup. */
    @Transactional(readOnly = true)
    public Formula get(UUID id) {
        Formula formula = formulaRepository.findWithDetailsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formula not found"));
        // The web views use both collections after this read-only transaction closes.
        formula.getMaterials().forEach(item -> item.getMaterial().getCode());
        formula.getSteps().size();
        return formula;
    }

    @Transactional
    public Formula create(String documentNo, String name, List<String> materialIds,
                          List<BigDecimal> quantitiesPerUnit) {
        return createDraft(documentNo, name, materialIds, null, null, null, null,
                FormulaCalculationBasis.PER_PRODUCT_UNIT, null, null, quantitiesPerUnit);
    }

    @Transactional
    public Formula create(String documentNo, String name, List<String> materialIds,
                          List<BigDecimal> quantitiesPerUnit, User actor) {
        requireRole(actor, UserRole.QC, UserRole.CEO);
        return createDraft(documentNo, name, materialIds, null, null, null, null,
                FormulaCalculationBasis.PER_PRODUCT_UNIT, null, null, quantitiesPerUnit);
    }

    @Transactional
    public Formula create(String documentNo, String name, List<String> materialIds,
                          List<String> newMaterialCodes, List<String> newMaterialNames,
                          List<String> newMaterialUnits, List<BigDecimal> quantitiesPerUnit,
                          User actor) {
        requireRole(actor, UserRole.QC, UserRole.CEO);
        return createDraft(documentNo, name, materialIds, newMaterialCodes, newMaterialNames,
                newMaterialUnits, null, FormulaCalculationBasis.PER_PRODUCT_UNIT, null, null, quantitiesPerUnit);
    }

    @Transactional
    public Formula createWithSteps(String documentNo, String name, List<String> materialIds,
                          List<String> newMaterialCodes, List<String> newMaterialNames, List<String> newMaterialUnits,
                          List<BigDecimal> quantitiesPerUnit, List<String> stepInstructions, List<Integer> stepTargetSeconds,
                          List<Boolean> stepRequiresTemperature, User actor) {
        return createWithSteps(documentNo, name, materialIds, newMaterialCodes, newMaterialNames, newMaterialUnits,
                null, FormulaCalculationBasis.PER_PRODUCT_UNIT, null, null, quantitiesPerUnit,
                stepInstructions, stepTargetSeconds, stepRequiresTemperature, actor);
    }

    @Transactional
    public Formula createWithSteps(String documentNo, String name, List<String> materialIds,
                          List<String> newMaterialCodes, List<String> newMaterialNames, List<String> newMaterialUnits,
                          List<String> materialPhases, List<BigDecimal> quantitiesPerUnit, List<String> stepInstructions,
                          List<Integer> stepTargetSeconds, List<Boolean> stepRequiresTemperature, User actor) {
        return createWithSteps(documentNo, name, materialIds, newMaterialCodes, newMaterialNames, newMaterialUnits,
                materialPhases, FormulaCalculationBasis.PER_PRODUCT_UNIT, null, null, quantitiesPerUnit,
                stepInstructions, stepTargetSeconds, stepRequiresTemperature, actor);
    }

    @Transactional
    public Formula createWithSteps(String documentNo, String name, List<String> materialIds,
                          List<String> newMaterialCodes, List<String> newMaterialNames, List<String> newMaterialUnits,
                          List<String> materialPhases, FormulaCalculationBasis calculationBasis,
                          BigDecimal productUnitWeightGrams, List<BigDecimal> percentages,
                          List<BigDecimal> quantitiesPerUnit, List<String> stepInstructions,
                          List<Integer> stepTargetSeconds, List<Boolean> stepRequiresTemperature, User actor) {
        requireRole(actor, UserRole.QC, UserRole.CEO);
        Formula formula = createDraft(documentNo, name, materialIds, newMaterialCodes, newMaterialNames, newMaterialUnits,
                materialPhases, calculationBasis, productUnitWeightGrams, percentages, quantitiesPerUnit);
        formula.setCreatedBy(actor);
        addSteps(formula, stepInstructions, stepTargetSeconds, stepRequiresTemperature);
        Formula saved = formulaRepository.save(formula);
        audit.record(actor, "CREATE_FORMULA", "FORMULA", saved.getId(), "Created formula " + saved.getDocumentNo());
        return saved;
    }

    private Formula createDraft(String documentNo, String name, List<String> materialIds,
                                List<String> newMaterialCodes, List<String> newMaterialNames,
                                List<String> newMaterialUnits, List<String> materialPhases,
                                FormulaCalculationBasis calculationBasis, BigDecimal productUnitWeightGrams,
                                List<BigDecimal> percentages, List<BigDecimal> quantitiesPerUnit) {
        if (documentNo == null || documentNo.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Document number and formula name are required");
        }
        if (formulaRepository.existsByDocumentNoIgnoreCase(documentNo.trim())) {
            throw new IllegalArgumentException("Document number already exists");
        }
        int materialCount = maxSize(materialIds, newMaterialCodes, newMaterialNames);
        if (materialCount == 0) {
            throw new IllegalArgumentException("At least one material row is required");
        }
        FormulaCalculationBasis basis = calculationBasis == null ? FormulaCalculationBasis.PER_PRODUCT_UNIT : calculationBasis;
        if (basis == FormulaCalculationBasis.PERCENT_W_W) {
            validatePercentFormula(materialCount, productUnitWeightGrams, percentages);
        } else if (quantitiesPerUnit == null || materialCount != quantitiesPerUnit.size()) {
            throw new IllegalArgumentException("Enter a quantity per product unit for every material");
        }

        Formula formula = new Formula(documentNo.trim(), name.trim());
        formula.setCalculationBasis(basis);
        formula.setProductUnitWeightGrams(basis == FormulaCalculationBasis.PERCENT_W_W ? productUnitWeightGrams : null);
        java.util.Set<java.util.UUID> selectedMaterials = new java.util.HashSet<>();
        for (int i = 0; i < materialCount; i++) {
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
            if (!selectedMaterials.add(material.getId())) throw new IllegalArgumentException("A material can be added only once per formula");
            if (basis == FormulaCalculationBasis.PERCENT_W_W) {
                formula.addMaterialPercent(material, percentages.get(i), phaseAt(materialPhases, i));
            } else {
                formula.addMaterial(material, quantitiesPerUnit.get(i), phaseAt(materialPhases, i));
            }
        }
        return formulaRepository.save(formula);
    }

    private void addSteps(Formula formula, List<String> instructions, List<Integer> seconds, List<Boolean> temperatures) {
        if (instructions == null) return;
        int number = 1;
        for (int i = 0; i < instructions.size(); i++) {
            String instruction = valueAt(instructions, i);
            if (instruction == null || instruction.isBlank()) continue;
            Integer target = valueAt(seconds, i);
            if (target != null && target <= 0) throw new IllegalArgumentException("Target time must be greater than zero");
            formula.addStep(number++, instruction.trim(), target, Boolean.TRUE.equals(valueAt(temperatures, i)));
        }
    }

    private <T> T valueAt(List<T> values, int index) {
        return values != null && index < values.size() ? values.get(index) : null;
    }

    @SafeVarargs
    private final int maxSize(List<?>... values) {
        int maximum = 0;
        for (List<?> value : values) if (value != null) maximum = Math.max(maximum, value.size());
        return maximum;
    }

    private FormulaPhase phaseAt(List<String> phases, int index) {
        String raw = valueAt(phases, index);
        if (raw == null || raw.isBlank()) return FormulaPhase.A; // preserves formulas created before phase support
        try { return FormulaPhase.valueOf(raw.trim()); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Invalid material phase"); }
    }

    private void validatePercentFormula(int materialCount, BigDecimal productUnitWeightGrams, List<BigDecimal> percentages) {
        if (productUnitWeightGrams == null || productUnitWeightGrams.signum() <= 0) {
            throw new IllegalArgumentException("Enter a finished-product weight in grams for each piece");
        }
        if (percentages == null || percentages.size() != materialCount || percentages.stream().anyMatch(value -> value == null || value.signum() < 0)) {
            throw new IllegalArgumentException("Enter a non-negative % w/w value for every material");
        }
        BigDecimal total = percentages.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.subtract(BigDecimal.valueOf(100)).abs().compareTo(new BigDecimal("0.000001")) > 0) {
            throw new IllegalArgumentException("Formula percentages must total 100% w/w");
        }
    }

    @Transactional
    public void submitForApproval(java.util.UUID id, User actor) {
        requireRole(actor, UserRole.QC, UserRole.CEO);
        Formula formula = formulaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formula not found"));
        if (formula.getStatus() != FormulaStatus.DRAFT && formula.getStatus() != FormulaStatus.REJECTED) {
            throw new IllegalStateException("Only draft or rejected formulas can be submitted");
        }
        formula.recordSubmission(actor);
        formula.setStatus(FormulaStatus.PENDING_APPROVAL);
        audit.record(actor, "SUBMIT_FORMULA", "FORMULA", formula.getId(), "Submitted " + formula.getDocumentNo() + " for approval");
    }

    @Transactional
    public void confirm(java.util.UUID id, User actor) {
        confirm(id, null, actor);
    }

    @Transactional
    public void confirm(java.util.UUID id, String selfApprovalReason, User actor) {
        requireRole(actor, UserRole.CEO);
        Formula formula = formulaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formula not found"));
        if (formula.getStatus() != FormulaStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only submitted formulas can be confirmed");
        }
        boolean selfApproval = formula.getCreatedBy() != null && formula.getCreatedBy().getId().equals(actor.getId());
        if (selfApproval && (selfApprovalReason == null || selfApprovalReason.isBlank())) {
            throw new IllegalArgumentException("CEO self-approval requires a reason");
        }
        formula.setStatus(FormulaStatus.CONFIRMED);
        formula.setRejectionReason(null);
        formula.approve(actor, selfApproval ? selfApprovalReason.trim() : null);
        if (formula.getPreviousRevision() != null) {
            formula.getPreviousRevision().setStatus(FormulaStatus.SUPERSEDED);
        }
        audit.record(actor, "CONFIRM_FORMULA", "FORMULA", formula.getId(), "Confirmed " + formula.getDocumentNo() + (selfApproval ? " (self-approval: " + selfApprovalReason.trim() + ")" : ""));
    }

    /** Starts an editable, separate revision; it never mutates the confirmed version used by historical plans. */
    @Transactional
    public Formula createRevision(java.util.UUID sourceId, User actor) {
        requireRole(actor, UserRole.QC, UserRole.CEO);
        Formula source = get(sourceId);
        if (source.getStatus() != FormulaStatus.CONFIRMED) throw new IllegalStateException("Only confirmed formulas can be revised");
        String suffix = "-R" + (source.getRevision() + 1);
        String prefix = source.getDocumentNo();
        if (prefix.length() + suffix.length() > 50) prefix = prefix.substring(0, 50 - suffix.length());
        Formula revision = new Formula(prefix + suffix, source.getName());
        revision.setRevision(source.getRevision() + 1);
        revision.setPreviousRevision(source);
        revision.setCreatedBy(actor);
        revision.setCalculationBasis(source.getCalculationBasis());
        revision.setProductUnitWeightGrams(source.getProductUnitWeightGrams());
        for (var item : source.getMaterials()) {
            if (source.getCalculationBasis() == FormulaCalculationBasis.PERCENT_W_W) revision.addMaterialPercent(item.getMaterial(), item.getPercentage(), item.getPhase());
            else revision.addMaterial(item.getMaterial(), item.getQuantityPerUnit(), item.getPhase());
        }
        for (var step : source.getSteps()) revision.addStep(step.getStepNumber(), step.getInstruction(), step.getTargetSeconds(), step.isRequiresTemperature());
        Formula saved = formulaRepository.save(revision);
        audit.record(actor, "CREATE_FORMULA_REVISION", "FORMULA", saved.getId(), "Revision " + saved.getDocumentNo() + " derived from " + source.getDocumentNo());
        return saved;
    }

    @Transactional
    public void reject(java.util.UUID id, String reason, User actor) {
        requireRole(actor, UserRole.CEO);
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Rejection reason is required");
        Formula formula = formulaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Formula not found"));
        if (formula.getStatus() != FormulaStatus.PENDING_APPROVAL) throw new IllegalStateException("Only pending formulas can be rejected");
        formula.setStatus(FormulaStatus.REJECTED); formula.setRejectionReason(reason.trim());
        audit.record(actor, "REJECT_FORMULA", "FORMULA", formula.getId(), reason.trim());
    }

    @Transactional
    public Formula updateDraft(java.util.UUID id, String documentNo, String name, List<String> materialIds,
                               List<BigDecimal> quantities, User actor) {
        return updateDraft(id, documentNo, name, materialIds, null, quantities, actor);
    }

    @Transactional
    public Formula updateDraft(java.util.UUID id, String documentNo, String name, List<String> materialIds,
                               List<String> materialPhases, List<BigDecimal> quantities, User actor) {
        return updateDraft(id, documentNo, name, materialIds, materialPhases,
                FormulaCalculationBasis.PER_PRODUCT_UNIT, null, null, quantities, actor);
    }

    /**
     * Keeps the established per-product-unit formula path intact while allowing a
     * new formula revision to use a true mass percentage composition.
     */
    @Transactional
    public Formula updateDraft(java.util.UUID id, String documentNo, String name, List<String> materialIds,
                               List<String> materialPhases, FormulaCalculationBasis calculationBasis,
                               BigDecimal productUnitWeightGrams, List<BigDecimal> percentages,
                               List<BigDecimal> quantities, User actor) {
        requireRole(actor, UserRole.QC, UserRole.CEO);
        Formula formula = formulaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Formula not found"));
        if (formula.getStatus() != FormulaStatus.DRAFT && formula.getStatus() != FormulaStatus.REJECTED) throw new IllegalStateException("Only draft or rejected formulas can be edited");
        if (documentNo == null || documentNo.isBlank() || name == null || name.isBlank()) throw new IllegalArgumentException("Document number and formula name are required");
        if (formulaRepository.existsByDocumentNoIgnoreCaseAndIdNot(documentNo.trim(), id)) throw new IllegalArgumentException("Document number already exists");
        formula.setDocumentNo(documentNo.trim()); formula.setName(name.trim()); formula.setRevision(formula.getRevision() + 1);
        if (materialIds == null || materialIds.isEmpty()) throw new IllegalArgumentException("At least one material row is required");
        FormulaCalculationBasis basis = calculationBasis == null ? FormulaCalculationBasis.PER_PRODUCT_UNIT : calculationBasis;
        if (basis == FormulaCalculationBasis.PERCENT_W_W) {
            validatePercentFormula(materialIds.size(), productUnitWeightGrams, percentages);
        } else if (quantities == null || materialIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Enter a quantity per product unit for every material");
        }
        java.util.List<com.formulabase.domain.FormulaMaterial> items = new java.util.ArrayList<>();
        java.util.Set<java.util.UUID> selectedMaterials = new java.util.HashSet<>();
        for (int i=0;i<materialIds.size();i++) {
            Material material = materialRepository.findById(java.util.UUID.fromString(materialIds.get(i))).orElseThrow(() -> new IllegalArgumentException("Material not found"));
            if (!selectedMaterials.add(material.getId())) throw new IllegalArgumentException("A material can be added only once per formula");
            BigDecimal quantity = basis == FormulaCalculationBasis.PERCENT_W_W ? BigDecimal.ZERO : quantities.get(i);
            BigDecimal percentage = basis == FormulaCalculationBasis.PERCENT_W_W ? percentages.get(i) : null;
            items.add(new com.formulabase.domain.FormulaMaterial(formula, material, quantity, phaseAt(materialPhases, i), percentage));
        }
        formula.replaceMaterials(items);
        formula.setCalculationBasis(basis);
        formula.setProductUnitWeightGrams(basis == FormulaCalculationBasis.PERCENT_W_W ? productUnitWeightGrams : null);
        formula.setStatus(FormulaStatus.DRAFT); formula.setRejectionReason(null);
        audit.record(actor, "EDIT_FORMULA", "FORMULA", formula.getId(), "Updated " + formula.getDocumentNo());
        return formula;
    }

    @Transactional
    public void deleteDraft(java.util.UUID id, User actor) {
        requireRole(actor, UserRole.CEO);
        Formula formula = formulaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Formula not found"));
        if (formula.getStatus() != FormulaStatus.DRAFT && formula.getStatus() != FormulaStatus.REJECTED) throw new IllegalStateException("Only draft or rejected formulas can be deleted");
        audit.record(actor, "DELETE_FORMULA", "FORMULA", formula.getId(), "Deleted " + formula.getDocumentNo());
        formulaRepository.delete(formula);
    }

    private void requireRole(User actor, UserRole... allowedRoles) {
        if (actor == null) throw new IllegalStateException("Please sign in first");
        for (UserRole role : allowedRoles) {
            if (actor.getRole() == role) return;
        }
        throw new IllegalStateException("You do not have permission for this action");
    }
}
