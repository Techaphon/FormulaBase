package com.formulabase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "materials")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaterialUnit stockUnit;

    protected Material() {
    }

    public Material(String code, String name, MaterialUnit stockUnit) {
        this.code = code;
        this.name = name;
        this.stockUnit = stockUnit;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public MaterialUnit getStockUnit() { return stockUnit; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setStockUnit(MaterialUnit stockUnit) { this.stockUnit = stockUnit; }
}
