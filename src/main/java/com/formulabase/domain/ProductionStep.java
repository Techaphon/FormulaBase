package com.formulabase.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "production_steps", uniqueConstraints = @UniqueConstraint(columnNames = {"formula_id", "step_number"}))
public class ProductionStep {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "formula_id") private Formula formula;
    @Column(name = "step_number", nullable = false) private int stepNumber;
    @Column(nullable = false, length = 1000) private String instruction;
    @Column(name = "instruction_th", length = 1000) private String instructionThai;
    @Column(name = "instruction_en", length = 1000) private String instructionEnglish;
    private Integer targetSeconds;
    @Column(nullable = false) private boolean requiresTemperature;
    @Column(name = "temperature_min_c", precision = 8, scale = 2) private BigDecimal temperatureMinC;
    @Column(name = "temperature_max_c", precision = 8, scale = 2) private BigDecimal temperatureMaxC;
    protected ProductionStep() { }
    public ProductionStep(Formula formula, int stepNumber, String instruction, Integer targetSeconds, boolean requiresTemperature) {
        this(formula, stepNumber, instruction, instruction, targetSeconds, requiresTemperature, null, null);
    }
    public ProductionStep(Formula formula, int stepNumber, String instructionThai, String instructionEnglish,
                          Integer targetSeconds, boolean requiresTemperature, BigDecimal temperatureMinC, BigDecimal temperatureMaxC) {
        this.formula = formula;
        this.stepNumber = stepNumber;
        this.instruction = instructionEnglish;
        this.instructionThai = instructionThai;
        this.instructionEnglish = instructionEnglish;
        this.targetSeconds = targetSeconds;
        this.requiresTemperature = requiresTemperature;
        this.temperatureMinC = temperatureMinC;
        this.temperatureMaxC = temperatureMaxC;
    }
    public UUID getId() { return id; }
    public int getStepNumber() { return stepNumber; }
    public String getInstruction() { return instruction; }
    public String getInstructionThai() { return instructionThai; }
    public String getInstructionEnglish() { return instructionEnglish; }
    public Integer getTargetSeconds() { return targetSeconds; }
    public boolean isRequiresTemperature() { return requiresTemperature; }
    public BigDecimal getTemperatureMinC() { return temperatureMinC; }
    public BigDecimal getTemperatureMaxC() { return temperatureMaxC; }

    public String instructionFor(String language) {
        if ("th".equalsIgnoreCase(language) && instructionThai != null && !instructionThai.isBlank()) return instructionThai;
        if (instructionEnglish != null && !instructionEnglish.isBlank()) return instructionEnglish;
        return instruction;
    }

    public void validateTemperature(BigDecimal temperature) {
        if (!requiresTemperature) return;
        if (temperature == null) throw new IllegalArgumentException("Temperature is required for this step");
        if (temperatureMinC == null && temperatureMaxC == null) {
            throw new IllegalStateException("QC must set a temperature limit before this step can be completed");
        }
        if (temperatureMinC != null && temperature.compareTo(temperatureMinC) < 0) {
            throw new IllegalArgumentException("Temperature must be at least " + temperatureMinC + " °C for this step");
        }
        if (temperatureMaxC != null && temperature.compareTo(temperatureMaxC) > 0) {
            throw new IllegalArgumentException("Temperature must not exceed " + temperatureMaxC + " °C for this step");
        }
    }
}
