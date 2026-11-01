package com.formulabase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "production_histories")
public class ProductionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "production_plan_id", nullable = false, unique = true)
    private ProductionPlan productionPlan;

    @Column(nullable = false)
    private int targetQuantity;

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal lossPercentage;

    @Column(nullable = false)
    private int lossAllowanceQuantity;

    @Column(nullable = false)
    private int plannedQuantity;

    @Column(nullable = false)
    private int actualProducedQuantity;

    @Column(nullable = false)
    private int rejectedQuantity;

    @Column(nullable = false)
    private int goodQuantity;

    @Column(nullable = false)
    private int surplusQuantity;

    @Column(nullable = false)
    private OffsetDateTime completedAt;

    protected ProductionHistory() {
    }

    public ProductionHistory(ProductionPlan plan, int actualProduced, int rejected,
                             int good, int surplus) {
        this.productionPlan = plan;
        this.targetQuantity = plan.getTargetQuantity();
        this.lossPercentage = plan.getLossPercentage();
        this.lossAllowanceQuantity = plan.getLossAllowanceQuantity();
        this.plannedQuantity = plan.getPlannedQuantity();
        this.actualProducedQuantity = actualProduced;
        this.rejectedQuantity = rejected;
        this.goodQuantity = good;
        this.surplusQuantity = surplus;
        this.completedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public ProductionPlan getProductionPlan() { return productionPlan; }
    public int getTargetQuantity() { return targetQuantity; }
    public BigDecimal getLossPercentage() { return lossPercentage; }
    public int getLossAllowanceQuantity() { return lossAllowanceQuantity; }
    public int getPlannedQuantity() { return plannedQuantity; }
    public int getActualProducedQuantity() { return actualProducedQuantity; }
    public int getRejectedQuantity() { return rejectedQuantity; }
    public int getGoodQuantity() { return goodQuantity; }
    public int getSurplusQuantity() { return surplusQuantity; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
}
