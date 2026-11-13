package com.formulabase.web;

import com.formulabase.domain.*;
import com.formulabase.repository.FormulaRepository;
import com.formulabase.service.AuditLogService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditControllerScopeTest {
 @Test void qcOnlySeesLogsForVisibleFormulaStatuses() {
  var audit = mock(com.formulabase.service.AuditSearchService.class); var sessions = mock(SessionHelper.class);
  var formulas = mock(FormulaRepository.class); var session = mock(HttpSession.class);
  var qc = mock(User.class); when(qc.getRole()).thenReturn(UserRole.QC); when(sessions.current(session)).thenReturn(qc);
  UUID allowed = UUID.randomUUID(), draft = UUID.randomUUID();
  var visible = mock(AuditLog.class); when(visible.getSubjectType()).thenReturn("FORMULA"); when(visible.getSubjectId()).thenReturn(allowed);
  var hidden = mock(AuditLog.class); when(hidden.getSubjectType()).thenReturn("FORMULA"); when(hidden.getSubjectId()).thenReturn(draft);
  var plan = mock(AuditLog.class); when(plan.getSubjectType()).thenReturn("PLAN");
  when(audit.search(eq(null),eq(null),eq(null),eq(null),eq(0),any())).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(visible)));
  when(formulas.findIdsByStatusIn(List.of(FormulaStatus.PENDING_APPROVAL, FormulaStatus.CONFIRMED, FormulaStatus.REJECTED, FormulaStatus.SUPERSEDED))).thenReturn(List.of(allowed));
  var model = new ExtendedModelMap();
  assertEquals("audit/list", new AuditController(audit, sessions, formulas).list(null,null,null,null,0,session,model));
  assertEquals(List.of(visible), model.getAttribute("logs"));
  verify(audit).search(null,null,null,null,0,java.util.Set.of(allowed));
 }
}
