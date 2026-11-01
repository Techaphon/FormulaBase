package com.formulabase.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "formulas")
public class Formula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String documentNo;

    @jakarta.persistence.Version
    private long version;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private int revision = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FormulaCalculationBasis calculationBasis = FormulaCalculationBasis.PER_PRODUCT_UNIT;

    /** Finished product mass per sellable piece. Required only for % w/w formulas. */
    @Column(precision = 19, scale = 8)
    private BigDecimal productUnitWeightGrams;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormulaStatus status = FormulaStatus.DRAFT;

    @OneToMany(mappedBy = "formula", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<FormulaMaterial> materials = new ArrayList<>();

    @OneToMany(mappedBy = "formula", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @jakarta.persistence.OrderBy("stepNumber ASC")
    private List<ProductionStep> steps = new ArrayList<>();

    @Column(length = 255)
    private String rejectionReason;

    /** Links a revision to the formula version from which it was derived. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_revision_id")
    private Formula previousRevision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_id")
    private User submittedBy;

    @Column(name = "submitted_at")
    private java.time.OffsetDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @Column(name = "approved_at")
    private java.time.OffsetDateTime approvedAt;

    @Column(name = "approval_reason", length = 255)
    private String approvalReason;

    protected Formula() {
    }

    public Formula(String documentNo, String name) {
        this.documentNo = documentNo;
        this.name = name;
    }

    public void addMaterial(Material material, java.math.BigDecimal quantityPerUnit) {
        materials.add(new FormulaMaterial(this, material, quantityPerUnit));
    }
    public void addMaterial(Material material, java.math.BigDecimal quantityPerUnit, FormulaPhase phase) {
        materials.add(new FormulaMaterial(this, material, quantityPerUnit, phase));
    }
    public void addMaterialPercent(Material material, BigDecimal percentage, FormulaPhase phase) {
        // Quantity-per-unit is retained for database compatibility with legacy formulas;
        // percentage formulas calculate from productUnitWeightGrams instead.
        materials.add(new FormulaMaterial(this, material, BigDecimal.ZERO, phase, percentage));
    }
    public void addStep(int stepNumber, String instruction, Integer targetSeconds, boolean requiresTemperature) {
        steps.add(new ProductionStep(this, stepNumber, instruction, targetSeconds, requiresTemperature));
    }
    public void replaceMaterials(List<FormulaMaterial> updatedMaterials) { materials.clear(); materials.addAll(updatedMaterials); }
    public void replaceSteps(List<ProductionStep> updatedSteps) { steps.clear(); steps.addAll(updatedSteps); }

    public UUID getId() { return id; }
    public String getDocumentNo() { return documentNo; }
    public String getName() { return name; }
    public int getRevision() { return revision; }
    public FormulaCalculationBasis getCalculationBasis() { return calculationBasis; }
    public BigDecimal getProductUnitWeightGrams() { return productUnitWeightGrams; }
    public FormulaStatus getStatus() { return status; }
    public List<FormulaMaterial> getMaterials() { return materials; }
    public List<ProductionStep> getSteps() { return steps; }
    public String getRejectionReason() { return rejectionReason; }
    public Formula getPreviousRevision() { return previousRevision; }
    public User getCreatedBy() { return createdBy; }
    public User getSubmittedBy() { return submittedBy; }
    public java.time.OffsetDateTime getSubmittedAt() { return submittedAt; }
    public User getApprovedBy() { return approvedBy; }
    public java.time.OffsetDateTime getApprovedAt() { return approvedAt; }
    public String getApprovalReason() { return approvalReason; }
    public void setDocumentNo(String documentNo) { this.documentNo = documentNo; }
    public void setName(String name) { this.name = name; }
    public void setRevision(int revision) { this.revision = revision; }
    public void setCalculationBasis(FormulaCalculationBasis calculationBasis) { this.calculationBasis = calculationBasis; }
    public void setProductUnitWeightGrams(BigDecimal productUnitWeightGrams) { this.productUnitWeightGrams = productUnitWeightGrams; }
    public void setStatus(FormulaStatus status) { this.status = status; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setPreviousRevision(Formula previousRevision) { this.previousRevision = previousRevision; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    /** Records workflow submission only; the physical document is signed on paper. */
    public void recordSubmission(User user) { this.submittedBy = user; this.submittedAt = java.time.OffsetDateTime.now(); }
    public void approve(User user, String reason) { this.approvedBy = user; this.approvedAt = java.time.OffsetDateTime.now(); this.approvalReason = reason; }
}
