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
 @PostMapping public String create(@RequestParam String username,@RequestParam String password,@RequestParam String fullName,@RequestParam UserRole role,HttpSession s,RedirectAttributes r){try{var actor=sessions.current(s);var user=users.create(username,password,fullName,role,actor);r.addFlashAttribute("message","User created");}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());}return "redirect:/users";}
 @PostMapping("/{id}/toggle") public String toggle(@PathVariable UUID id,HttpSession s,RedirectAttributes r){try{var actor=sessions.current(s);var user=users.toggle(id,actor);r.addFlashAttribute("message","User status updated");}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());}return "redirect:/users";}
 @GetMapping("/{id}/remove") public String remove(@PathVariable UUID id,HttpSession s,Model m,RedirectAttributes r){if(!ceo(s))return "redirect:/";try{var user=users.visibleUser(id);if(user.getId().equals(sessions.current(s).getId()))throw new IllegalArgumentException("Cannot delete your own account");m.addAttribute("targetUser",user);m.addAttribute("tasks",users.unfinishedTasks(id));m.addAttribute("workers",users.workers().stream().filter(w->!w.getId().equals(id)).toList());return "users/remove";}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());return "redirect:/users";}}
 @GetMapping("/{id}/deactivate") public String deactivatePage(@PathVariable UUID id,HttpSession s,Model m,RedirectAttributes r){String view=remove(id,s,m,r);if(view.equals("users/remove"))m.addAttribute("deactivating",true);return view;}
 @PostMapping("/{id}/deactivate") public String deactivate(@PathVariable UUID id,@RequestParam java.util.Map<String,String> params,HttpSession s,RedirectAttributes r){try{var replacements=new java.util.HashMap<UUID,UUID>();var versions=new java.util.HashMap<UUID,Long>();for(var e:params.entrySet()){if(e.getKey().startsWith("replacement_")){if(e.getValue().isBlank())throw new IllegalArgumentException("Choose a replacement for every unfinished task");replacements.put(UUID.fromString(e.getKey().substring(12)),UUID.fromString(e.getValue()));}else if(e.getKey().startsWith("version_"))versions.put(UUID.fromString(e.getKey().substring(8)),Long.valueOf(e.getValue()));}users.deactivateWithTransfers(id,sessions.current(s),replacements,versions);r.addFlashAttribute("message","User status updated");return "redirect:/users";}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());return "redirect:/users/"+id+"/deactivate";}}
 @PostMapping("/{id}/delete") public String delete(@PathVariable UUID id,@RequestParam java.util.Map<String,String> params,HttpSession s,RedirectAttributes r){try{var replacements=new java.util.HashMap<UUID,UUID>();var versions=new java.util.HashMap<UUID,Long>();for(var e:params.entrySet()){if(e.getKey().startsWith("replacement_")){if(e.getValue().isBlank())throw new IllegalArgumentException("Choose a replacement for every unfinished task");replacements.put(UUID.fromString(e.getKey().substring(12)),UUID.fromString(e.getValue()));}else if(e.getKey().startsWith("version_"))versions.put(UUID.fromString(e.getKey().substring(8)),Long.valueOf(e.getValue()));}users.deleteWithTransfers(id,sessions.current(s),replacements,versions);r.addFlashAttribute("message","User removed; history retained");return "redirect:/users";}catch(org.springframework.dao.OptimisticLockingFailureException | jakarta.persistence.OptimisticLockException e){r.addFlashAttribute("error","Tasks changed; review the list and try again");return "redirect:/users/"+id+"/remove";}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());return "redirect:/users/"+id+"/remove";}}
 private boolean ceo(HttpSession s){var u=sessions.current(s);return u!=null&&u.getRole()==UserRole.CEO;}
}
