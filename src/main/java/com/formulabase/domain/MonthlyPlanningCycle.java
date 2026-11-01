package com.formulabase.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "monthly_planning_cycles")
public class MonthlyPlanningCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @jakarta.persistence.Version
    private long version;

    @Column(name = "planning_month", nullable = false, unique = true, length = 7)
    private String planningMonth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MonthlyPlanningCycleStatus status = MonthlyPlanningCycleStatus.DRAFT;

    @OneToMany(mappedBy = "planningCycle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionPlan> productionPlans = new ArrayList<>();

    protected MonthlyPlanningCycle() {
    }

    public MonthlyPlanningCycle(String planningMonth) {
        if (planningMonth == null || !planningMonth.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException("Planning month must use yyyy-MM format");
        }
        this.planningMonth = planningMonth;
    }

    public UUID getId() { return id; }
    public String getPlanningMonth() { return planningMonth; }
    public MonthlyPlanningCycleStatus getStatus() { return status; }
    public List<ProductionPlan> getProductionPlans() { return productionPlans; }
    public void setStatus(MonthlyPlanningCycleStatus status) { this.status = status; }
}
