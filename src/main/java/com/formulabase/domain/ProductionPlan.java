package com.formulabase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OrderBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "production_plans")
public class ProductionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @jakarta.persistence.Version
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planning_cycle_id", nullable = false)
    private MonthlyPlanningCycle planningCycle;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "formula_id", nullable = false)
    private Formula formula;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_worker_id")
    private User assignedWorker;

    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Column(name = "target_quantity", nullable = false)
    private int targetQuantity;

    // Set by the assigned Worker immediately before Start; null means not set yet.
    @Column(name = "loss_percentage", precision = 7, scale = 4)
    private BigDecimal lossPercentage;

    @Column(name = "loss_allowance_quantity")
    private Integer lossAllowanceQuantity;

    @Column(name = "planned_quantity")
    private Integer plannedQuantity;

    @Column(name = "actual_produced_quantity")
    private Integer actualProducedQuantity;

    @Column(name = "rejected_quantity")
    private Integer rejectedQuantity;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductionPlanStatus status = ProductionPlanStatus.PLANNED;
    private String qualityReviewNote;
    private String qualityReviewedBy;
    private OffsetDateTime qualityReviewedAt;

    @OneToMany(mappedBy = "productionPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private java.util.List<StepExecution> stepExecutions = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "productionPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("changedAt DESC")
    private java.util.List<RescheduleLog> rescheduleLogs = new java.util.ArrayList<>();

    public String getQualityReviewNote() { return qualityReviewNote; }
    public String getQualityReviewedBy() { return qualityReviewedBy; }
    public OffsetDateTime getQualityReviewedAt() { return qualityReviewedAt; }
    public void reviewQuality(String note, String reviewer) {
        if (status != ProductionPlanStatus.LOSS_EXCEPTION || qualityReviewedAt != null)
            throw new IllegalStateException("Review is not available");
        if (note == null || note.isBlank() || note.length() > 255)
            throw new IllegalArgumentException("Enter a review note (1-255 characters)");
        qualityReviewNote = note.trim(); qualityReviewedBy = reviewer; qualityReviewedAt = OffsetDateTime.now();
    }

    protected ProductionPlan() {
    }

    public ProductionPlan(MonthlyPlanningCycle planningCycle, Formula formula,
                           LocalDate plannedDate, int targetQuantity) {
        if (targetQuantity < 0) {
            throw new IllegalArgumentException("Target quantity cannot be negative");
        }
        this.planningCycle = planningCycle;
        this.formula = formula;
        this.plannedDate = plannedDate;
        this.targetQuantity = targetQuantity;
    }

    public UUID getId() { return id; }
    public long getVersion() { return version; }
    public MonthlyPlanningCycle getPlanningCycle() { return planningCycle; }
    public Formula getFormula() { return formula; }
    public LocalDate getPlannedDate() { return plannedDate; }
    public int getTargetQuantity() { return targetQuantity; }
    public BigDecimal getLossPercentage() { return lossPercentage; }
    public Integer getLossAllowanceQuantity() { return lossAllowanceQuantity; }
    public Integer getPlannedQuantity() { return plannedQuantity; }
    public Integer getActualProducedQuantity() { return actualProducedQuantity; }
    public Integer getRejectedQuantity() { return rejectedQuantity; }
    public ProductionPlanStatus getStatus() { return status; }
    public User getAssignedWorker() { return assignedWorker; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public java.util.List<StepExecution> getStepExecutions() { return stepExecutions; }
    public java.util.List<RescheduleLog> getRescheduleLogs() { return rescheduleLogs; }

    public int getGoodQuantity() {
        if (actualProducedQuantity == null || rejectedQuantity == null) return 0;
        return Math.max(0, actualProducedQuantity - rejectedQuantity);
    }

    public int getSurplusQuantity() {
        return Math.max(0, getGoodQuantity() - targetQuantity);
    }

    public void assignWorker(User worker) {
        if (worker != null && worker.isArchived()) throw new IllegalArgumentException("Worker not found");
        if (worker != null && worker.getRole() != UserRole.WORKER) {
            throw new IllegalArgumentException("Assigned user must have WORKER role");
        }
        this.assignedWorker = worker;
    }

    public void start(BigDecimal lossPercentage, QuantitySnapshot snapshot) {
        if (status != ProductionPlanStatus.PLANNED) {
            throw new IllegalStateException("Only planned batches can be started");
        }
        this.lossPercentage = lossPercentage;
        this.lossAllowanceQuantity = snapshot.lossAllowanceQuantity();
        this.plannedQuantity = snapshot.plannedQuantity();
        this.startedAt = OffsetDateTime.now();
        this.status = ProductionPlanStatus.IN_PROGRESS;
    }

    public void reschedule(LocalDate plannedDate) {
        if (status != ProductionPlanStatus.PLANNED) throw new IllegalStateException("Only planned tasks can be rescheduled");
        this.plannedDate = plannedDate;
    }

    public void addStepExecution(ProductionStep step) { stepExecutions.add(new StepExecution(this, step)); }
    public void addRescheduleLog(LocalDate oldDate, LocalDate newDate, String reason, User actor) { rescheduleLogs.add(new RescheduleLog(this, oldDate, newDate, reason, actor)); }
    public void cancel() {
        if (status != ProductionPlanStatus.PLANNED) throw new IllegalStateException("Only planned tasks can be cancelled");
        status = ProductionPlanStatus.CANCELLED;
    }

    public void complete(int actualProduced, int rejected) {
        if (status != ProductionPlanStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress batches can be completed");
        }
        if (actualProduced < 0 || rejected < 0 || rejected > actualProduced) {
            throw new IllegalArgumentException("Production quantities are invalid");
        }
        this.actualProducedQuantity = actualProduced;
        this.rejectedQuantity = rejected;
        this.completedAt = OffsetDateTime.now();
        this.status = rejected > lossAllowanceQuantity || actualProduced - rejected < targetQuantity
                ? ProductionPlanStatus.LOSS_EXCEPTION
                : ProductionPlanStatus.COMPLETED;
    }

    public record QuantitySnapshot(int lossAllowanceQuantity, int plannedQuantity) { }
}
