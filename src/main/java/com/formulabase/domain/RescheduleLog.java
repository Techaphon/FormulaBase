package com.formulabase.domain;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="reschedule_logs")
public class RescheduleLog {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="production_plan_id") private ProductionPlan productionPlan;
 private LocalDate previousDate; private LocalDate newDate;
 @Column(nullable=false,length=255) private String reason;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="changed_by_id") private User changedBy;
 @Column(nullable=false) private OffsetDateTime changedAt=OffsetDateTime.now();
 protected RescheduleLog(){}
 public RescheduleLog(ProductionPlan plan,LocalDate previousDate,LocalDate newDate,String reason,User changedBy){this.productionPlan=plan;this.previousDate=previousDate;this.newDate=newDate;this.reason=reason;this.changedBy=changedBy;}
 public UUID getId(){return id;} public LocalDate getPreviousDate(){return previousDate;} public LocalDate getNewDate(){return newDate;} public String getReason(){return reason;} public User getChangedBy(){return changedBy;} public OffsetDateTime getChangedAt(){return changedAt;}
}
