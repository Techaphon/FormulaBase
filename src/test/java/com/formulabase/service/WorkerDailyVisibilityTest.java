package com.formulabase.service;
import com.formulabase.domain.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class WorkerDailyVisibilityTest {
 @Test void startedYesterdayRemainsVisibleWhileOtherDaysStayHidden() {
  LocalDate today=com.formulabase.config.BusinessTime.today();
  var old=new ProductionPlan(null,null,today.minusDays(1),10);
  var current=new ProductionPlan(null,null,today,10);
  var future=new ProductionPlan(null,null,today.plusDays(1),10);
  assertFalse(old.isVisibleToWorkerOn(today));assertTrue(current.isVisibleToWorkerOn(today));assertFalse(future.isVisibleToWorkerOn(today));
  old.start(BigDecimal.ZERO,new ProductionPlan.QuantitySnapshot(0,10));
  assertTrue(old.isVisibleToWorkerOn(today));assertTrue(old.isVisibleToWorkerOn(today.plusDays(1)));assertFalse(old.isVisibleToWorkerOn(today.minusDays(2)));
  old.complete(10,0);assertTrue(old.isVisibleToWorkerOn(today));assertFalse(old.isVisibleToWorkerOn(today.plusDays(1)));
 }
}
