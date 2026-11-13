package com.formulabase;
import com.formulabase.domain.*;
import com.formulabase.repository.*;
import com.formulabase.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:pdfreview", "spring.jpa.hibernate.ddl-auto=create-drop"})
@Transactional
class PdfFeedbackTest {
 @Autowired AuditLogRepository logs; @Autowired AuditSearchService search;
 @Autowired UserRepository users; @Autowired UserService service;
 @Test void translatedSearchPaginationAndScopeWorkBeforeLimit() {
  var ceo=users.findByUsernameIgnoreCase("ceo").orElseThrow(); var id=UUID.randomUUID();
  logs.saveAndFlush(new AuditLog(ceo,"QUALITY_REVIEW","PLAN",id,"older record"));
  for(int i=0;i<210;i++) logs.save(new AuditLog(ceo,"CREATE_FORMULA","FORMULA",UUID.randomUUID(),"newer record"));
  logs.flush();
  assertEquals(1,search.search("Review batch quality",null,null,null,0,null).getTotalElements());
  assertEquals(1,search.search("ตรวจสอบคุณภาพงานผลิต",null,null,null,0,null).getTotalElements());
  assertEquals(1,search.search(null,null,"QUALITY_REVIEW",null,0,null).getTotalElements());
  assertEquals(50,search.search(null,null,null,null,0,null).getContent().size());
  assertEquals(11,search.search(null,null,null,null,4,null).getContent().size());
  assertEquals(0,search.search(null,null,null,null,0,Set.of(id)).getTotalElements());
  assertTrue(search.options("action",null).contains("QUALITY_REVIEW"));
  assertTrue(search.options("action",Set.of()).isEmpty());
 }
 @Test void primaryAdminCannotBeRemovedDisabledOrDemotedByAnotherCeo() {
  var ceo=users.findByUsernameIgnoreCase("ceo").orElseThrow();
  var other=users.save(new User("other-admin","password","Other",UserRole.CEO));
  assertTrue(ceo.isPrimaryAdmin());
  assertThrows(IllegalArgumentException.class,()->service.delete(ceo.getId(),other));
  assertThrows(IllegalArgumentException.class,()->service.toggle(ceo.getId(),other));
  assertThrows(IllegalStateException.class,()->ceo.setRole(UserRole.WORKER));
  assertThrows(IllegalStateException.class,ceo::archive);
  assertThrows(IllegalStateException.class,()->ceo.setActive(false));
 }
}
