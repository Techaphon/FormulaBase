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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "formulas")
public class Formula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String documentNo;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private int revision = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormulaStatus status = FormulaStatus.DRAFT;

    @OneToMany(mappedBy = "formula", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<FormulaMaterial> materials = new ArrayList<>();

    protected Formula() {
    }

    public Formula(String documentNo, String name) {
        this.documentNo = documentNo;
        this.name = name;
    }

    public void addMaterial(Material material, java.math.BigDecimal quantityPerUnit) {
        materials.add(new FormulaMaterial(this, material, quantityPerUnit));
    }

    public UUID getId() { return id; }
    public String getDocumentNo() { return documentNo; }
    public String getName() { return name; }
    public int getRevision() { return revision; }
    public FormulaStatus getStatus() { return status; }
    public List<FormulaMaterial> getMaterials() { return materials; }
    public void setDocumentNo(String documentNo) { this.documentNo = documentNo; }
    public void setName(String name) { this.name = name; }
    public void setRevision(int revision) { this.revision = revision; }
    public void setStatus(FormulaStatus status) { this.status = status; }
}
