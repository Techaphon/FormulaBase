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

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "formula_materials")
public class FormulaMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formula_id", nullable = false)
    private Formula formula;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantityPerUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaterialUnit unit;

    protected FormulaMaterial() {
    }

    public FormulaMaterial(Formula formula, Material material, BigDecimal quantityPerUnit) {
        if (quantityPerUnit == null || quantityPerUnit.signum() < 0) {
            throw new IllegalArgumentException("Quantity per unit cannot be negative");
        }
        this.formula = formula;
        this.material = material;
        this.quantityPerUnit = quantityPerUnit;
        this.unit = material.getStockUnit();
    }

    public UUID getId() { return id; }
    public Formula getFormula() { return formula; }
    public Material getMaterial() { return material; }
    public BigDecimal getQuantityPerUnit() { return quantityPerUnit; }
    public MaterialUnit getUnit() { return unit; }
}
