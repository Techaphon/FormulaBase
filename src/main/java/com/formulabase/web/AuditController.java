package com.formulabase.web;
import com.formulabase.domain.UserRole;
import com.formulabase.service.AuditLogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
@Controller @RequestMapping("/audit")
public class AuditController {
 private final AuditLogService audit; private final SessionHelper sessions;
 public AuditController(AuditLogService audit,SessionHelper sessions){this.audit=audit;this.sessions=sessions;}
 @GetMapping public String list(@RequestParam(required=false)String keyword,HttpSession s,Model m){var u=sessions.current(s);if(u==null||u.getRole()!=UserRole.CEO)return "redirect:/";m.addAttribute("logs",audit.search(keyword));m.addAttribute("keyword",keyword==null?"":keyword);return "audit/list";}
}
