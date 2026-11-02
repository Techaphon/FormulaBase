package com.formulabase.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "step_executions", uniqueConstraints = @UniqueConstraint(columnNames = {"production_plan_id", "step_id"}))
public class StepExecution {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "production_plan_id") private ProductionPlan productionPlan;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "step_id") private ProductionStep step;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "worker_id") private User worker;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ExecutionStatus status = ExecutionStatus.PENDING;
    @Column(precision = 8, scale = 2) private BigDecimal recordedTemperature;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private boolean overdue;
    private boolean wasOverdue;
    private String acknowledgementNote;
    private OffsetDateTime acknowledgedAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "acknowledged_by_id") private User acknowledgedBy;
    protected StepExecution() { }
    public StepExecution(ProductionPlan plan, ProductionStep step) { this.productionPlan = plan; this.step = step; }
    public UUID getId() { return id; }
    public ProductionPlan getProductionPlan() { return productionPlan; }
    public ProductionStep getStep() { return step; }
    public User getWorker() { return worker; }
    public ExecutionStatus getStatus() { return status; }
    public BigDecimal getRecordedTemperature() { return recordedTemperature; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public boolean isOverdue() { return overdue; }
    public boolean isWasOverdue() { return wasOverdue; }
    public String getAcknowledgementNote() { return acknowledgementNote; }
    public OffsetDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public User getAcknowledgedBy() { return acknowledgedBy; }
    public void start(User worker) { if (status != ExecutionStatus.PENDING) throw new IllegalStateException("Only pending steps can be started"); this.worker=worker; this.startedAt=OffsetDateTime.now(); this.status=ExecutionStatus.IN_PROGRESS; }
    public void complete(BigDecimal temperature) { if (status != ExecutionStatus.IN_PROGRESS) throw new IllegalStateException("Only active steps can be completed"); step.validateTemperature(temperature); this.recordedTemperature=temperature; this.completedAt=OffsetDateTime.now(); this.status=ExecutionStatus.COMPLETED; if(step.getTargetSeconds()!=null && startedAt!=null) { overdue=java.time.Duration.between(startedAt,completedAt).getSeconds()>step.getTargetSeconds(); wasOverdue=overdue; } }
    public void acknowledge(String note, User actor) { if(!overdue) throw new IllegalStateException("This step has no overdue alert"); if(note==null||note.isBlank()) throw new IllegalArgumentException("Enter an acknowledgement note"); acknowledgementNote=note.trim(); acknowledgedBy=actor; acknowledgedAt=OffsetDateTime.now(); overdue=false; }
}
