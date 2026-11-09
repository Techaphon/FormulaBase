package com.formulabase.web;

import com.formulabase.domain.*;
import com.formulabase.service.*;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanningControllerVisibilityTest {
 @Test void cancelledTasksAreHiddenWithoutDeletingThemOrAffectingCounts() {
  var planning = mock(PlanningService.class); var formulas = mock(FormulaService.class);
  var users = mock(UserService.class); var sessions = mock(SessionHelper.class);
  var audit = mock(AuditLogService.class); var session = mock(HttpSession.class);
  var actor = mock(User.class); when(actor.getRole()).thenReturn(UserRole.PC); when(sessions.current(session)).thenReturn(actor);
  var cycle = mock(MonthlyPlanningCycle.class); when(cycle.getPlanningMonth()).thenReturn("2027-02");
  UUID cycleId = UUID.randomUUID(), activeId = UUID.randomUUID(); when(planning.getCycle(cycleId)).thenReturn(cycle);
  var active = mock(ProductionPlan.class); when(active.getStatus()).thenReturn(ProductionPlanStatus.PLANNED); when(active.getId()).thenReturn(activeId);
  var cancelled = mock(ProductionPlan.class); when(cancelled.getStatus()).thenReturn(ProductionPlanStatus.CANCELLED);
  when(cancelled.getRescheduleLogs()).thenReturn(List.of(mock(RescheduleLog.class)));
  when(planning.plans(cycleId)).thenReturn(List.of(cancelled, active));
  var model = new ExtendedModelMap();
  assertEquals("planning/view", new PlanningController(planning,formulas,users,sessions,audit).view(cycleId,session,model));
  assertEquals(List.of(active), model.getAttribute("plans"));
  assertEquals(1, model.getAttribute("cancelledCount"));
  assertEquals(1L, model.getAttribute("deleteBlockedCount"));
  assertEquals(1L, model.getAttribute("unassignedCount"));
  verify(audit).transfersFor(List.of(activeId));
  verify(planning, never()).deletePlan(any(),any(),any());
  verify(planning, never()).deleteCycle(any(),any());
 }
}
