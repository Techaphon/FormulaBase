package com.formulabase.web;
import com.formulabase.domain.*;
import com.formulabase.service.*;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PrintNavigationTest {
 @Test void workOrderReturnIsValidWithoutBrowserHistoryForEveryRole() {
  var formulas=mock(FormulaService.class);var plans=mock(ProductionExecutionService.class);var sessions=mock(SessionHelper.class);var audit=mock(AuditLogService.class);var session=mock(HttpSession.class);
  var plan=mock(ProductionPlan.class);when(plan.isVisibleToWorkerOn(any())).thenReturn(true);var cycle=mock(MonthlyPlanningCycle.class);var id=UUID.randomUUID();var cycleId=UUID.randomUUID();
  when(plan.getPlanningCycle()).thenReturn(cycle);when(cycle.getId()).thenReturn(cycleId);when(cycle.getStatus()).thenReturn(MonthlyPlanningCycleStatus.LOCKED);when(plan.getPlannedDate()).thenReturn(com.formulabase.config.BusinessTime.today());when(plan.getStatus()).thenReturn(ProductionPlanStatus.PLANNED);
  when(plans.details(id)).thenReturn(new ProductionExecutionService.PlanDetails(plan,List.of(),List.of()));
  var controller=new PrintController(formulas,plans,sessions,audit);
  for(var role:UserRole.values()) {
   var actor=mock(User.class);when(actor.getRole()).thenReturn(role);when(actor.getId()).thenReturn(UUID.randomUUID());when(plan.getAssignedWorker()).thenReturn(actor);when(sessions.current(session)).thenReturn(actor);
   var model=new ExtendedModelMap();assertEquals("print/work-order",controller.workOrder(id,session,model));
   String expected=role==UserRole.CEO||role==UserRole.WORKER?"/worker/plans/"+id:role==UserRole.PC?"/planning/"+cycleId:"/workflow";
   assertEquals(expected,model.get("returnUrl"));
  }
 }
 @Test void bomReturnRespectsWorkerAccess() {
  var formulas=mock(FormulaService.class);var plans=mock(ProductionExecutionService.class);var sessions=mock(SessionHelper.class);var session=mock(HttpSession.class);var actor=mock(User.class);var formula=mock(Formula.class);var id=UUID.randomUUID();
  when(actor.getRole()).thenReturn(UserRole.WORKER);when(sessions.current(session)).thenReturn(actor);when(plans.hasAssignedPlanForFormula(id,actor)).thenReturn(true);when(formulas.get(id)).thenReturn(formula);when(formula.getMaterials()).thenReturn(List.of());
  var controller=new PrintController(formulas,plans,sessions,mock(AuditLogService.class));var model=new ExtendedModelMap();controller.bom(id,1,session,model);assertEquals("/worker/tasks",model.get("returnUrl"));
  when(actor.getRole()).thenReturn(UserRole.CEO);controller.bom(id,1,session,model);assertEquals("/formulas/"+id,model.get("returnUrl"));
 }
}
