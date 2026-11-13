package com.formulabase.service;
import com.formulabase.domain.*;
import com.formulabase.repository.AuditLogRepository;
import com.formulabase.web.UiText;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.*;
import java.util.*;
@Service
@Transactional(readOnly=true)
public class AuditSearchService {
 private final AuditLogRepository repository; private final EntityManager em; private final UiText text;
 public AuditSearchService(AuditLogRepository repository, EntityManager em, UiText text) { this.repository=repository; this.em=em; this.text=text; }
 private Specification<AuditLog> scope(Set<UUID> ids) {
  return (r,q,b)->ids==null?b.conjunction():ids.isEmpty()?b.disjunction():b.and(b.equal(r.get("subjectType"),"FORMULA"),r.get("subjectId").in(ids));
 }
 public List<String> options(String field, Set<UUID> ids) {
  var b=em.getCriteriaBuilder(); var q=b.createQuery(String.class); var r=q.from(AuditLog.class);
  q.select(r.get(field)).distinct(true).where(scope(ids).toPredicate(r,q,b)).orderBy(b.asc(r.get(field)));
  return em.createQuery(q).getResultList();
 }
 public List<User> actors(Set<UUID> ids) {
  var b=em.getCriteriaBuilder(); var q=b.createQuery(User.class); var r=q.from(AuditLog.class);
  q.select(r.get("actor")).distinct(true).where(b.and(scope(ids).toPredicate(r,q,b),b.isNotNull(r.get("actor"))));
  return em.createQuery(q).getResultList();
 }
 public Page<AuditLog> search(String keyword,String actor,String action,String subject,int page,Set<UUID> ids) {
  String term=keyword==null?"":keyword.trim().toLowerCase(Locale.ROOT);
  var codes=options("action",ids).stream().filter(code->text.auditAction(code,"th").toLowerCase(Locale.ROOT).contains(term)||text.auditAction(code,"en").toLowerCase(Locale.ROOT).contains(term)).toList();
  Specification<AuditLog> filter=(r,q,b)->{
   var predicates=new ArrayList<jakarta.persistence.criteria.Predicate>();
   if(actor!=null&&!actor.isBlank())predicates.add(b.equal(r.get("actor").get("username"),actor));
   if(action!=null&&!action.isBlank())predicates.add(b.equal(r.get("action"),action));
   if(subject!=null&&!subject.isBlank())predicates.add(b.equal(r.get("subjectType"),subject));
   if(!term.isEmpty()) {
    String pattern="%"+term.replace("!","!!").replace("%","!%").replace("_","!_")+"%";
    var person=r.join("actor",jakarta.persistence.criteria.JoinType.LEFT);
    predicates.add(b.or(b.like(b.lower(r.get("action")),pattern,'!'),b.like(b.lower(r.get("details")),pattern,'!'),b.like(b.lower(person.get("username")),pattern,'!'),b.like(b.lower(person.get("fullName")),pattern,'!'),codes.isEmpty()?b.disjunction():r.get("action").in(codes)));
   }
   return b.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
  };
  return repository.findAll(scope(ids).and(filter),PageRequest.of(Math.max(0,page),50,Sort.by(Sort.Direction.DESC,"createdAt","id")));
 }
}
