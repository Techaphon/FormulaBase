package com.formulabase.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "production_steps", uniqueConstraints = @UniqueConstraint(columnNames = {"formula_id", "step_number"}))
public class ProductionStep {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "formula_id") private Formula formula;
    @Column(name = "step_number", nullable = false) private int stepNumber;
    @Column(nullable = false, length = 1000) private String instruction;
    private Integer targetSeconds;
    @Column(nullable = false) private boolean requiresTemperature;
    protected ProductionStep() { }
    public ProductionStep(Formula formula, int stepNumber, String instruction, Integer targetSeconds, boolean requiresTemperature) {
        this.formula = formula; this.stepNumber = stepNumber; this.instruction = instruction; this.targetSeconds = targetSeconds; this.requiresTemperature = requiresTemperature;
    }
    public UUID getId() { return id; }
    public int getStepNumber() { return stepNumber; }
    public String getInstruction() { return instruction; }
    public Integer getTargetSeconds() { return targetSeconds; }
    public boolean isRequiresTemperature() { return requiresTemperature; }
}
