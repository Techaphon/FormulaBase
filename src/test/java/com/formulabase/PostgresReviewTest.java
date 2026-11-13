package com.formulabase;
import com.formulabase.domain.*;
import com.formulabase.repository.*;
import com.formulabase.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
@EnabledIfEnvironmentVariable(named="FORMULABASE_TEST_PG_URL",matches="jdbc:postgresql://127\\.0\\.0\\.1:.*")
@org.springframework.test.context.ActiveProfiles({"prod","demo"})
@SpringBootTest(properties={"demo.seed.enabled=true","demo.seed.password=review-only-password","spring.flyway.enabled=true","spring.jpa.hibernate.ddl-auto=validate","spring.sql.init.mode=never"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostgresReviewTest {
 @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
  r.add("spring.datasource.url",()->System.getenv("FORMULABASE_TEST_PG_URL"));r.add("spring.datasource.username",()->"review_admin");r.add("spring.datasource.password",()->"");r.add("spring.datasource.driver-class-name",()->"org.postgresql.Driver");
 }
 @Autowired JdbcTemplate jdbc; @Autowired UserRepository users; @Autowired UserService service; @Autowired AuditSearchService audit;
 @Test @Order(1) void flywayAndSchemaValidationPassOnPostgres() {
  assertEquals(9,jdbc.queryForObject("select count(*) from flyway_schema_history where success",Integer.class));
  assertEquals(1,jdbc.queryForObject("select count(*) from users where primary_admin and active and not archived",Integer.class));
 }
 @Test @Order(2) void concurrentAttemptsCannotRemovePrimaryAdmin() throws Exception {
  var ceo=users.findByUsernameIgnoreCase("ceo").orElseThrow();
  var a=users.save(new User("concurrent_a","test","A",UserRole.CEO));var b=users.save(new User("concurrent_b","test","B",UserRole.CEO));
  var pool=Executors.newFixedThreadPool(2);var ready=new CountDownLatch(2);var start=new CountDownLatch(1);
  try {
   var f1=pool.submit(()->{ready.countDown();start.await();assertThrows(IllegalArgumentException.class,()->service.delete(ceo.getId(),a));return true;});
   var f2=pool.submit(()->{ready.countDown();start.await();assertThrows(IllegalArgumentException.class,()->service.toggle(ceo.getId(),b));return true;});
   assertTrue(ready.await(5,TimeUnit.SECONDS));start.countDown();assertTrue(f1.get(10,TimeUnit.SECONDS));assertTrue(f2.get(10,TimeUnit.SECONDS));
   var remaining=users.findById(ceo.getId()).orElseThrow();assertTrue(remaining.isPrimaryAdmin()&&remaining.isActive()&&!remaining.isArchived());
  } finally {pool.shutdownNow();}
 }
 @Test @Order(4) void requestCapturedBeforeDeactivationMustNotManageUsers() {
  var ceo=users.findByUsernameIgnoreCase("ceo").orElseThrow();
  var staleActor=users.save(new User("stale_actor","test","Stale actor",UserRole.CEO));
  service.toggle(staleActor.getId(),ceo);
  assertFalse(users.findById(staleActor.getId()).orElseThrow().isActive());
  assertThrows(IllegalStateException.class,()->service.create("unauthorized_after_disable","review-password","Should not be created",UserRole.WORKER,staleActor));
 }
 @Test @Order(5) void concurrentCrossDeletionLeavesOneAuthorizedAdministrator() throws Exception {
  var a=users.save(new User("cross_a","test","A",UserRole.CEO));
  var b=users.save(new User("cross_b","test","B",UserRole.CEO));
  var ready=new CountDownLatch(2);var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
  try {
   var f1=pool.submit(()->{ready.countDown();start.await();try{service.delete(b.getId(),a);return true;}catch(IllegalStateException denied){return false;}});
   var f2=pool.submit(()->{ready.countDown();start.await();try{service.delete(a.getId(),b);return true;}catch(IllegalStateException denied){return false;}});
   assertTrue(ready.await(5,TimeUnit.SECONDS));start.countDown();boolean first=f1.get(10,TimeUnit.SECONDS),second=f2.get(10,TimeUnit.SECONDS);
   assertNotEquals(first,second);
   assertNotEquals(users.findById(a.getId()).orElseThrow().isArchived(),users.findById(b.getId()).orElseThrow().isArchived());
  } finally {pool.shutdownNow();}
 }
 @Test @Order(3) void benchmarkAuditWith100000SyntheticRows() {
  jdbc.update("insert into audit_logs(id,actor_id,action,subject_type,subject_id,details,created_at) select gen_random_uuid(),(select id from users where username='ceo'),case when n%10=0 then 'QUALITY_REVIEW' else 'CREATE_FORMULA' end,'PLAN',gen_random_uuid(),'Synthetic benchmark '||n,now()-n*interval '1 second' from generate_series(1,100000) n");
  jdbc.execute("analyze audit_logs");
  for(String term:new String[]{"","Review batch quality","Synthetic benchmark 99999"}) {
   audit.search(term,null,null,null,0,null);
   var timings=new ArrayList<Double>();
   for(int i=0;i<7;i++){long t=System.nanoTime();var result=audit.search(term,null,null,null,0,null);timings.add((System.nanoTime()-t)/1000000.0);assertTrue(result.getTotalElements()>0);assertTrue(result.getContent().size()<=50);}
   Collections.sort(timings);System.out.println("AUDIT_BENCH term="+term+" median_ms="+timings.get(3)+" max_ms="+timings.get(6));
  }
 }
}
