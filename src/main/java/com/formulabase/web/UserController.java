package com.formulabase.web;
import com.formulabase.domain.UserRole;
import com.formulabase.service.AuditLogService;
import com.formulabase.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {
 private final UserService users; private final SessionHelper sessions; private final AuditLogService audit;
 public UserController(UserService users,SessionHelper sessions,AuditLogService audit){this.users=users;this.sessions=sessions;this.audit=audit;}
 @GetMapping public String list(HttpSession s,Model m){if(!ceo(s))return "redirect:/";m.addAttribute("users",users.all());m.addAttribute("roles",UserRole.values());return "users/list";}
 @PostMapping public String create(@RequestParam String username,@RequestParam String password,@RequestParam String fullName,@RequestParam UserRole role,HttpSession s,RedirectAttributes r){try{var actor=sessions.current(s);var user=users.create(username,password,fullName,role,actor);audit.record(actor,"CREATE_USER","USER",user.getId(),"Created @"+user.getUsername());r.addFlashAttribute("message","User created");}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());}return "redirect:/users";}
 @PostMapping("/{id}/toggle") public String toggle(@PathVariable UUID id,HttpSession s,RedirectAttributes r){try{var actor=sessions.current(s);var user=users.toggle(id,actor);audit.record(actor,"TOGGLE_USER","USER",id,"User @"+user.getUsername()+" active="+user.isActive());r.addFlashAttribute("message","User status updated");}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());}return "redirect:/users";}
 private boolean ceo(HttpSession s){var u=sessions.current(s);return u!=null&&u.getRole()==UserRole.CEO;}
}
