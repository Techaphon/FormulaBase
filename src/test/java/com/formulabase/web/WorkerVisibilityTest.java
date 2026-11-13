package com.formulabase.web;
import com.formulabase.domain.*;
import com.formulabase.service.ProductionExecutionService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class WorkerVisibilityTest {
 @Test void assignedWorkerCannotOpenDraftOrFutureTaskByDirectUrl() {
  var service=mock(ProductionExecutionService.class);var sessions=mock(SessionHelper.class);var session=mock(HttpSession.class);var user=mock(User.class);var plan=mock(ProductionPlan.class);var cycle=mock(MonthlyPlanningCycle.class);var id=UUID.randomUUID();var userId=UUID.randomUUID();
  when(user.getRole()).thenReturn(UserRole.WORKER);when(user.getId()).thenReturn(userId);when(sessions.current(session)).thenReturn(user);when(plan.getAssignedWorker()).thenReturn(user);when(plan.getPlanningCycle()).thenReturn(cycle);when(service.details(id)).thenReturn(new ProductionExecutionService.PlanDetails(plan,List.of(),List.of()));
  var controller=new WorkerController(service,sessions);when(cycle.getStatus()).thenReturn(MonthlyPlanningCycleStatus.DRAFT);assertEquals("redirect:/worker/tasks",controller.view(id,session,new ExtendedModelMap()));
  when(cycle.getStatus()).thenReturn(MonthlyPlanningCycleStatus.LOCKED);when(plan.isVisibleToWorkerOn(any())).thenReturn(false);when(plan.getPlannedDate()).thenReturn(com.formulabase.config.BusinessTime.today().plusDays(1));assertEquals("redirect:/worker/tasks",controller.view(id,session,new ExtendedModelMap()));
  when(plan.getPlannedDate()).thenReturn(com.formulabase.config.BusinessTime.today().minusDays(1));assertEquals("redirect:/worker/tasks",controller.view(id,session,new ExtendedModelMap()));
  when(plan.isVisibleToWorkerOn(any())).thenReturn(true);when(plan.getPlannedDate()).thenReturn(com.formulabase.config.BusinessTime.today());when(plan.getStatus()).thenReturn(ProductionPlanStatus.PLANNED);assertEquals("worker/view",controller.view(id,session,new ExtendedModelMap()));
  when(plan.getStatus()).thenReturn(ProductionPlanStatus.COMPLETED);assertEquals("worker/view",controller.view(id,session,new ExtendedModelMap()));
  when(user.getRole()).thenReturn(UserRole.CEO);when(cycle.getStatus()).thenReturn(MonthlyPlanningCycleStatus.DRAFT);assertEquals("worker/view",controller.view(id,session,new ExtendedModelMap()));
 }
}
