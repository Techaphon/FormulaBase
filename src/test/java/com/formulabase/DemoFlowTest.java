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
    @Autowired AuditLogService audit;
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

    @Test void ceoCannotConfirmFormulaUntilQcSubmitsIt() {
        var qc = users.findByUsernameIgnoreCase("qc").orElseThrow();
        var ceo = users.findByUsernameIgnoreCase("ceo").orElseThrow();
        var formula = formulas.create("APPROVAL-001", "Approval test", List.of(""), List.of("APPROVAL-RM"),
                List.of("Water"), List.of("ML"), List.of(BigDecimal.ONE), qc);
        assertThrows(IllegalStateException.class, () -> formulas.confirm(formula.getId(), ceo));
        formulas.submitForApproval(formula.getId(), qc);
        formulas.confirm(formula.getId(), ceo);
        assertEquals(FormulaStatus.CONFIRMED, formula.getStatus());
    }

    @Test void ceoMayConfirmOwnDraftWithReasonButNotAnotherUsersDraft() {
        var qc = users.findByUsernameIgnoreCase("qc").orElseThrow();
        var ceo = users.findByUsernameIgnoreCase("ceo").orElseThrow();
        var own = formulas.createWithSteps("SELF-001", "Own formula", List.of(""), List.of("SELF-RM"),
                List.of("Water"), List.of("G"), List.of(BigDecimal.ONE), null, null, null, ceo);
        assertThrows(IllegalArgumentException.class, () -> formulas.confirmOwnDraft(own.getId(), "", ceo));
        assertEquals(FormulaStatus.DRAFT, own.getStatus());
        formulas.confirmOwnDraft(own.getId(), "Reviewed in person", ceo);
        assertEquals(FormulaStatus.CONFIRMED, own.getStatus());
        var other = formulas.createWithSteps("OTHER-001", "QC formula", List.of(""), List.of("OTHER-RM"),
                List.of("Water"), List.of("G"), List.of(BigDecimal.ONE), null, null, null, qc);
        assertThrows(IllegalStateException.class, () -> formulas.confirmOwnDraft(other.getId(), "Reviewed", ceo));
    }

    @Test void percentFormulaCalculatesRawMaterialMassFromPlannedPieces() {
        var qc = users.findByUsernameIgnoreCase("qc").orElseThrow();
        var ceo = users.findByUsernameIgnoreCase("ceo").orElseThrow();
        var worker = users.findByUsernameIgnoreCase("worker1").orElseThrow();
        var formula = formulas.createWithSteps("PERCENT-001", "60 g cream", List.of("", ""),
                List.of("PCT-WATER", "PCT-OIL"), List.of("Water", "Oil"), List.of("G", "G"),
                List.of("A", "B"), FormulaCalculationBasis.PERCENT_W_W, new BigDecimal("60"),
                List.of(new BigDecimal("60"), new BigDecimal("40")), null,
                null, null, null, qc);
        formulas.submitForApproval(formula.getId(), qc);
        formulas.confirm(formula.getId(), ceo);
        var cycle = planning.createCycle(LocalDate.now().toString().substring(0, 7));
        var plan = planning.addPlan(cycle.getId(), formula.getId(), worker.getId(), LocalDate.now(), 100);
        planning.lockCycle(cycle.getId());
        production.start(plan.getId(), worker, new BigDecimal("5"));

        var requirements = production.materialRequirements(plan.getId());
        assertEquals(105, plan.getPlannedQuantity());
        assertEquals("% w/w", requirements.get(0).perUnitUnit());
        assertEquals("G", requirements.get(0).totalUnit());
        assertEquals(0, requirements.get(0).total().compareTo(new BigDecimal("3780")));
        assertEquals(0, requirements.get(1).total().compareTo(new BigDecimal("2520")));
    }

    @Test void confirmedFormulaIsPreservedWhenRevisionIsApproved() {
        var qc = users.findByUsernameIgnoreCase("qc").orElseThrow();
        var ceo = users.findByUsernameIgnoreCase("ceo").orElseThrow();
        var formula = formulas.createWithSteps("REV-001", "Revision source", List.of(""),
                List.of("REV-RM"), List.of("Water"), List.of("G"), List.of("A"),
                FormulaCalculationBasis.PERCENT_W_W, new BigDecimal("10"), List.of(new BigDecimal("100")), null,
                null, null, null, qc);
        formulas.submitForApproval(formula.getId(), qc);
        formulas.confirm(formula.getId(), ceo);
        var revision = formulas.createRevision(formula.getId(), qc);
        assertEquals(FormulaStatus.CONFIRMED, formula.getStatus());
        assertEquals(2, revision.getRevision());
        assertEquals(FormulaStatus.DRAFT, revision.getStatus());
        assertEquals(formula.getId(), revision.getPreviousRevision().getId());
        formulas.submitForApproval(revision.getId(), qc);
        formulas.confirm(revision.getId(), ceo);
        assertEquals(FormulaStatus.SUPERSEDED, formula.getStatus());
        assertEquals(FormulaStatus.CONFIRMED, revision.getStatus());
    }

    @Test void repeatedRevisionRequestsAlwaysUseANewDocumentNumber() {
        var qc = users.findByUsernameIgnoreCase("qc").orElseThrow();
        var ceo = users.findByUsernameIgnoreCase("ceo").orElseThrow();
        var formula = formulas.create("REV-NUMBER-001", "Revision numbering", List.of(""), List.of("REV-NUMBER-RM"),
                List.of("Water"), List.of("G"), List.of(BigDecimal.ONE), qc);
        formulas.submitForApproval(formula.getId(), qc);
        formulas.confirm(formula.getId(), ceo);

        var first = formulas.createRevision(formula.getId(), qc);
        var second = formulas.createRevision(formula.getId(), qc);

        assertEquals("REV-NUMBER-001-R2", first.getDocumentNo());
        assertEquals("REV-NUMBER-001-R3", second.getDocumentNo());
        assertNotEquals(first.getDocumentNo(), second.getDocumentNo());
    }

    @Test void auditSearchFindsTheActorUsername() {
        var worker = users.findByUsernameIgnoreCase("worker1").orElseThrow();
        audit.record(worker, "START_PRODUCTION", "PRODUCTION_PLAN", worker.getId(), "Started a demo batch");

        assertTrue(audit.search("worker1").stream().anyMatch(log -> log.getActor().getId().equals(worker.getId())));
    }

    @Test void temperatureRequiredStepRejectsValuesOutsideTheQcRangeAndKeepsBothInstructions() {
        var worker = users.findByUsernameIgnoreCase("worker1").orElseThrow();
        var formula = new Formula("TEMP-001", "Temperature test");
        formula.addStep(1, "ให้ความร้อนถึง 70°C", "Heat to 70°C", 600, true,
                new BigDecimal("68"), new BigDecimal("72"));
        var step = formula.getSteps().get(0);
        var execution = new StepExecution(null, step);
        execution.start(worker);

        assertEquals("ให้ความร้อนถึง 70°C", step.instructionFor("th"));
        assertEquals("Heat to 70°C", step.instructionFor("en"));
        assertThrows(IllegalArgumentException.class, () -> execution.complete(new BigDecimal("0.08")));
        execution.complete(new BigDecimal("70"));
        assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
    }

    @Test void stepWithoutQcTemperatureLimitsDoesNotRequireWorkerTemperature() {
        var worker = users.findByUsernameIgnoreCase("worker1").orElseThrow();
        var formula = new Formula("TEMP-LEGACY-001", "Legacy temperature test");
        formula.addStep(1, "Legacy heat instruction", 600, true);
        var execution = new StepExecution(null, formula.getSteps().get(0));
        execution.start(worker);

        execution.complete(null);
        assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
    }
}
