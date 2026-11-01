package com.formulabase.web;
import com.formulabase.repository.ProductionPlanRepository;
import com.formulabase.domain.ProductionPlanStatus;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;
@Controller
@RequestMapping("/quality")
public class QualityController {
 private final ProductionPlanRepository plans; private final SessionHelper sessions; private final com.formulabase.service.AuditLogService audit;
 public QualityController(ProductionPlanRepository plans, SessionHelper sessions, com.formulabase.service.AuditLogService audit){this.plans=plans;this.sessions=sessions;this.audit=audit;}
 @GetMapping public String list(Model model){model.addAttribute("plans",plans.findAll().stream().filter(p->p.getStatus()==ProductionPlanStatus.LOSS_EXCEPTION).toList());return "quality/list";}
 @PostMapping("/{id}/review") @Transactional
 public String review(@PathVariable UUID id,@RequestParam String note,HttpSession session,RedirectAttributes redirect){
 try{var plan=plans.findById(id).orElseThrow();var user=sessions.current(session);plan.reviewQuality(note,user.getUsername());audit.record(user,"QUALITY_REVIEW","PLAN",plan.getId(),note.trim());redirect.addFlashAttribute("message","Quality review saved");}
 catch(RuntimeException ex){redirect.addFlashAttribute("error",ex.getMessage());}return "redirect:/quality";
 }
}
