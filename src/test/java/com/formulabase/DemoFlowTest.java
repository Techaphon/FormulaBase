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
    @Autowired UserService userService;
    @Autowired ProductionHistoryRepository history;
    @Autowired WorkflowService workflow;

    @Test void draftPlanningDeletionPreservesPublishedAndRescheduledWork() {
        var ceo = users.findByUsernameIgnoreCase("ceo").orElseThrow();
        var qc = users.findByUsernameIgnoreCase("qc").orElseThrow();
        var pc = users.findByUsernameIgnoreCase("pc").orElseThrow();
        var formula = formulas.create("DELETE-PLAN-001", "Test formula", List.of(""), List.of("DEL-RM"),
                List.of("Water"), List.of("ML"), List.of(BigDecimal.TEN), qc);
        formulas.submitForApproval(formula.getId(), qc); formulas.confirm(formula.getId(), ceo);
        var cycle = planning.createCycle("2035-02", pc);
        var first = planning.addPlan(cycle.getId(), formula.getId(), null, LocalDate.of(2035,2,10), 10, pc);
        var second = planning.addPlan(cycle.getId(), formula.getId(), null, LocalDate.of(2035,2,11), 20, pc);
        assertThrows(IllegalStateException.class, () -> planning.deletePlan(cycle.getId(), first.getId(), qc));
        planning.deletePlan(cycle.getId(), first.getId(), pc);
        assertEquals(1, planning.plans(cycle.getId()).size());
        planning.reschedule(second.getId(), LocalDate.of(2035,2,12), null, "test", pc);
        assertThrows(IllegalStateException.class, () -> planning.deletePlan(cycle.getId(), second.getId(), pc));
        assertThrows(IllegalStateException.class, () -> planning.deleteCycle(cycle.getId(), pc));
        assertEquals(1, planning.plans(cycle.getId()).size());
        planning.lockCycle(cycle.getId(), pc);
        assertThrows(IllegalStateException.class, () -> planning.deleteCycle(cycle.getId(), ceo));
        assertThrows(IllegalStateException.class, () -> planning.deletePlan(cycle.getId(), second.getId(), ceo));
        var disposable = planning.createCycle("2035-03", pc);
        planning.addPlan(disposable.getId(), formula.getId(), null, LocalDate.of(2035,3,1), 5, pc);
        var id = disposable.getId();
        planning.deleteCycle(id, pc);
        assertThrows(IllegalArgumentException.class, () -> planning.getCycle(id));
        assertTrue(planning.plans(id).isEmpty());
    }

    @Test void unfinishedWorkIsTransferredWithoutChangingRecordedProgress() {
        var ceo = users.findByUsernameIgnoreCase("ceo").orElseThrow();
        var qc = users.findByUsernameIgnoreCase("qc").orElseThrow();
        var previous = users.findByUsernameIgnoreCase("worker1").orElseThrow();
        var replacement = userService.create("handover-worker", "password1234", "Next worker", UserRole.WORKER, ceo);
        var formula = formulas.createWithSteps("TRANSFER-001", "Transfer cream", List.of(""), List.of("TRANSFER-RM"),
                List.of("Water"), List.of("ML"), List.of(BigDecimal.TEN), List.of("Mix"), List.of(600), List.of(false), qc);
        formulas.submitForApproval(formula.getId(), qc);
        formulas.confirm(formula.getId(), ceo);
        var cycle = planning.createCycle(LocalDate.now().toString().substring(0, 7));
        var plan = planning.addPlan(cycle.getId(), formula.getId(), previous.getId(), LocalDate.now(), 100);
        var waiting = planning.addPlan(cycle.getId(), formula.getId(), previous.getId(), LocalDate.now(), 50);
        planning.lockCycle(cycle.getId());
        assertThrows(IllegalArgumentException.class, () -> production.start(plan.getId(), replacement, BigDecimal.ZERO));
        assertEquals(ProductionPlanStatus.PLANNED, plan.getStatus());
        production.start(plan.getId(), previous, new BigDecimal("5"));
        var step = production.steps(plan.getId()).get(0);
        production.startStep(plan.getId(), step.getId(), previous);
        var startedAt = step.getStartedAt();
        assertThrows(IllegalArgumentException.class, () -> userService.delete(previous.getId(), ceo));
        assertFalse(previous.isArchived());
        var replacements = java.util.Map.of(plan.getId(), replacement.getId(), waiting.getId(), replacement.getId());
        var versions = java.util.Map.of(plan.getId(), plan.getVersion(), waiting.getId(), waiting.getVersion());
        var staleVersions = java.util.Map.of(plan.getId(), plan.getVersion() + 1, waiting.getId(), waiting.getVersion());
        assertThrows(IllegalStateException.class, () -> userService.deleteWithTransfers(previous.getId(), ceo, replacements, staleVersions));
        var invalidReplacements = java.util.Map.of(plan.getId(), replacement.getId(), waiting.getId(), qc.getId());
        assertThrows(IllegalArgumentException.class, () -> userService.deleteWithTransfers(previous.getId(), ceo, invalidReplacements, versions));
        assertEquals(previous.getId(), plan.getAssignedWorker().getId());
        userService.deleteWithTransfers(previous.getId(), ceo, replacements, versions);
        assertTrue(previous.isArchived());
        assertEquals(replacement.getId(), plan.getAssignedWorker().getId());
        assertEquals(replacement.getId(), waiting.getAssignedWorker().getId());
        assertEquals(startedAt, step.getStartedAt());
        assertEquals(previous.getId(), step.getWorker().getId());
        assertEquals(105, plan.getPlannedQuantity());
        assertThrows(IllegalArgumentException.class, () -> production.completeStep(plan.getId(), step.getId(), null, previous));
        production.completeStep(plan.getId(), step.getId(), null, replacement);
        production.complete(plan.getId(), replacement, 105, 5);
        assertTrue(history.existsByProductionPlanId(plan.getId()));
        assertEquals(2, audit.transfersFor(List.of(plan.getId(), waiting.getId())).size());
    }

    @Test void userDeletionProtectsIdentityAndHistory() {
        var ceo = users.findByUsernameIgnoreCase("ceo").orElseThrow();
        var qc = users.findByUsernameIgnoreCase("qc").orElseThrow();
        var disposable = userService.create("delete-check", "password1234", "Delete Check", UserRole.WORKER, ceo);
        assertThrows(IllegalStateException.class, () -> userService.delete(disposable.getId(), qc));
        assertThrows(IllegalArgumentException.class, () -> userService.delete(ceo.getId(), ceo));
        userService.delete(disposable.getId(), ceo);
        assertTrue(users.findById(disposable.getId()).orElseThrow().isArchived());
        assertFalse(userService.all().stream().anyMatch(user -> user.getId().equals(disposable.getId())));
        assertNull(userService.findActive("delete-check"));
        assertThrows(IllegalArgumentException.class, () -> userService.authenticate("delete-check", "password1234"));
        assertThrows(IllegalArgumentException.class, () -> userService.toggle(disposable.getId(), ceo));
        assertThrows(IllegalArgumentException.class, () -> userService.create("delete-check", "password1234", "New Worker", UserRole.WORKER, ceo));
        var linked = userService.create("linked-check", "password1234", "Linked Check", UserRole.QC, ceo);
        audit.record(linked, "CREATE_MATERIAL", "MATERIAL", linked.getId(), "test");
        userService.delete(linked.getId(), ceo);
        assertTrue(users.findById(linked.getId()).orElseThrow().isArchived());
    }

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
        userService.delete(worker.getId(), ceo);
        assertTrue(history.existsByProductionPlanId(plan.getId()));
        assertEquals("worker1", production.getPlan(plan.getId()).getAssignedWorker().getUsername());
        assertFalse(userService.workers().stream().anyMatch(user -> user.getId().equals(worker.getId())));
        assertThrows(IllegalArgumentException.class, () -> plan.assignWorker(worker));
    }

    @Test void ceoCanRescheduleOverduePlanWithinItsMonthButPcCannot() {
        var qc = users.findByUsernameIgnoreCase("qc").orElseThrow();
        var ceo = users.findByUsernameIgnoreCase("ceo").orElseThrow();
        var pc = users.findByUsernameIgnoreCase("pc").orElseThrow();
        var formula = formulas.create("OVERDUE-001", "Overdue plan", List.of(""), List.of("OVERDUE-RM"),
                List.of("Water"), List.of("G"), List.of(BigDecimal.ONE), qc);
        formulas.submitForApproval(formula.getId(), qc);
        formulas.confirm(formula.getId(), ceo);
        var past = LocalDate.now(com.formulabase.config.BusinessTime.ZONE).minusDays(1);
        var cycle = planning.createCycle(past.toString().substring(0, 7));
        var plan = planning.addPlan(cycle.getId(), formula.getId(), null, past, 10);
        assertThrows(IllegalStateException.class, () -> planning.reschedule(plan.getId(), past, null, "Change", pc));
        planning.reschedule(plan.getId(), past, null, "CEO adjustment", ceo);
        assertEquals(past, plan.getPlannedDate());
    }

    @Test void cannotLockMonthContainingOnlyCancelledTasks() {
        var qc = users.findByUsernameIgnoreCase("qc").orElseThrow();
        var ceo = users.findByUsernameIgnoreCase("ceo").orElseThrow();
        var formula = formulas.create("CANCEL-ONLY", "Cancelled task", List.of(""), List.of("CANCEL-RM"),
                List.of("Water"), List.of("G"), List.of(BigDecimal.ONE), qc);
        formulas.submitForApproval(formula.getId(), qc);
        formulas.confirm(formula.getId(), ceo);
        var cycle = planning.createCycle(LocalDate.now().toString().substring(0, 7));
        var plan = planning.addPlan(cycle.getId(), formula.getId(), null, LocalDate.now(), 10);
        planning.cancel(plan.getId(), ceo);
        assertThrows(IllegalStateException.class, () -> planning.lockCycle(cycle.getId(), ceo));
        assertEquals(MonthlyPlanningCycleStatus.DRAFT, cycle.getStatus());
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
        assertEquals(1, workflow.snapshot(LocalDate.now()).lossExceptions());
        assertThrows(IllegalArgumentException.class,()->plan.reviewQuality(" ","qc"));
        plan.reviewQuality("Confirmed actual counts", "qc");
        assertEquals(0, workflow.snapshot(LocalDate.now()).lossExceptions());
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
