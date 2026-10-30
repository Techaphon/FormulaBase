package com.formulabase;

import com.formulabase.domain.*;
import com.formulabase.repository.*;
import com.formulabase.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:demoflow", "spring.jpa.hibernate.ddl-auto=create-drop"})
@Transactional
class DemoFlowTest {
    @Autowired FormulaService formulas;
    @Autowired PlanningService planning;
    @Autowired ProductionExecutionService production;
    @Autowired UserRepository users;
    @Autowired ProductionHistoryRepository history;

    @Test void qcToCeoToPcToWorkerCompletesBatch() {
        var qc=users.findByUsernameIgnoreCase("qc").orElseThrow();
        var ceo=users.findByUsernameIgnoreCase("ceo").orElseThrow();
        var worker=users.findByUsernameIgnoreCase("worker1").orElseThrow();
        var formula=formulas.create("FLOW-001", "Demo serum", List.of(""), List.of("FLOW-RM"),
                List.of("Water"), List.of("ML"), List.of(new BigDecimal("10")), qc);
        formulas.submitForApproval(formula.getId(),qc);
        assertThrows(IllegalStateException.class,()->formulas.confirm(formula.getId(),qc));
        formulas.confirm(formula.getId(),ceo);
        var cycle=planning.createCycle(LocalDate.now().toString().substring(0,7));
        var plan=planning.addPlan(cycle.getId(),formula.getId(),worker.getId(),LocalDate.now(),100);
        planning.lockCycle(cycle.getId());
        production.start(plan.getId(),worker,new BigDecimal("5"));
        assertEquals(105,plan.getPlannedQuantity());
        assertEquals(0, production.materialRequirements(plan.getId()).get(0).total().compareTo(new BigDecimal("1050")));
        production.complete(plan.getId(),worker,105,5);
        assertEquals(100,plan.getGoodQuantity());
        assertEquals(ProductionPlanStatus.COMPLETED,plan.getStatus());
        assertTrue(history.existsByProductionPlanId(plan.getId()));
    }

    @Test void abnormalOutputCanBeReviewedOnceWithoutChangingQuantities() {
        var qc=users.findByUsernameIgnoreCase("qc").orElseThrow();
        var ceo=users.findByUsernameIgnoreCase("ceo").orElseThrow();
        var worker=users.findByUsernameIgnoreCase("worker1").orElseThrow();
        var formula=formulas.create("QC-001", "Quality test", List.of(""), List.of("QC-RM"),
                List.of("Water"), List.of("ML"), List.of(new BigDecimal("10")), qc);
        formulas.submitForApproval(formula.getId(),qc);
        formulas.confirm(formula.getId(),ceo);
        var cycle=planning.createCycle(LocalDate.now().toString().substring(0,7));
        var plan=planning.addPlan(cycle.getId(),formula.getId(),worker.getId(),LocalDate.now(),100);
        planning.lockCycle(cycle.getId());
        assertThrows(IllegalStateException.class,()->planning.addPlan(cycle.getId(),formula.getId(),worker.getId(),LocalDate.now(),100));
        production.start(plan.getId(),worker,new BigDecimal("5"));
        production.complete(plan.getId(),worker,105,10);
        assertEquals(ProductionPlanStatus.LOSS_EXCEPTION,plan.getStatus());
        assertThrows(IllegalArgumentException.class,()->plan.reviewQuality(" ","qc"));
        plan.reviewQuality("Confirmed actual counts", "qc");
        assertNotNull(plan.getQualityReviewedAt());
        assertEquals("qc",plan.getQualityReviewedBy());
        assertEquals(95,plan.getGoodQuantity());
        assertThrows(IllegalStateException.class,()->plan.reviewQuality("Overwrite", "ceo"));
        assertThrows(IllegalStateException.class,()->production.complete(plan.getId(),worker,105,0));
    }
}
