package com.formulabase.domain;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="audit_logs")
public class AuditLog {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="actor_id") private User actor;
 @Column(nullable=false,length=80) private String action;
 @Column(nullable=false,length=80) private String subjectType;
 private UUID subjectId;
 @Column(nullable=false,length=1000) private String details;
 @Column(nullable=false) private OffsetDateTime createdAt=OffsetDateTime.now();
 protected AuditLog(){}
 public AuditLog(User actor,String action,String subjectType,UUID subjectId,String details){this.actor=actor;this.action=action;this.subjectType=subjectType;this.subjectId=subjectId;this.details=details;}
 public UUID getId(){return id;} public User getActor(){return actor;} public String getAction(){return action;} public String getSubjectType(){return subjectType;} public UUID getSubjectId(){return subjectId;} public String getDetails(){return details;} public OffsetDateTime getCreatedAt(){return createdAt;}
}
