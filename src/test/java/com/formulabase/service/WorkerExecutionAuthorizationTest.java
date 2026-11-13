package com.formulabase.service;
import com.formulabase.domain.*;
import com.formulabase.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class WorkerExecutionAuthorizationTest {
 @Test void ceoAndOtherWorkersCannotChangeProductionButAssignedWorkerCanStart() {
  var plans=mock(ProductionPlanRepository.class);var histories=mock(ProductionHistoryRepository.class);var audit=mock(AuditLogService.class);
  var service=new ProductionExecutionService(plans,histories,new QuantityCalculationService(),audit);
  var plan=mock(ProductionPlan.class);when(plan.isVisibleToWorkerOn(any())).thenReturn(true);var assigned=mock(User.class);var other=mock(User.class);var ceo=mock(User.class);var cycle=mock(MonthlyPlanningCycle.class);var id=UUID.randomUUID();var step=UUID.randomUUID();
  when(plans.findById(id)).thenReturn(Optional.of(plan));when(plan.getAssignedWorker()).thenReturn(assigned);when(plan.getPlannedDate()).thenReturn(com.formulabase.config.BusinessTime.today());when(assigned.getRole()).thenReturn(UserRole.WORKER);when(assigned.getId()).thenReturn(UUID.randomUUID());when(other.getRole()).thenReturn(UserRole.WORKER);when(other.getId()).thenReturn(UUID.randomUUID());when(ceo.getRole()).thenReturn(UserRole.CEO);
  for(var actor:List.of(ceo,other)) {
   assertThrows(IllegalArgumentException.class,()->service.start(id,actor,BigDecimal.ZERO));
   assertThrows(IllegalArgumentException.class,()->service.complete(id,actor,10,0));
   assertThrows(IllegalArgumentException.class,()->service.startStep(id,step,actor));
   assertThrows(IllegalArgumentException.class,()->service.completeStep(id,step,BigDecimal.TEN,actor));
   assertThrows(IllegalArgumentException.class,()->service.acknowledgeStepAlert(id,step,"note",actor));
  }
  verifyNoInteractions(audit,histories);
  when(plan.getPlanningCycle()).thenReturn(cycle);when(cycle.getStatus()).thenReturn(MonthlyPlanningCycleStatus.LOCKED);when(plan.getPlannedDate()).thenReturn(com.formulabase.config.BusinessTime.today());when(plan.getTargetQuantity()).thenReturn(10);
  assertSame(plan,service.start(id,assigned,BigDecimal.ZERO));verify(plan).start(eq(BigDecimal.ZERO),any());
 }
}
