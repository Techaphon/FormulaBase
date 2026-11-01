package com.formulabase.web;
import com.formulabase.repository.ProductionHistoryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller @RequestMapping("/history")
public class HistoryController {
 private final ProductionHistoryRepository history; private final SessionHelper sessions;
 public HistoryController(ProductionHistoryRepository history,SessionHelper sessions){this.history=history;this.sessions=sessions;}
 @GetMapping public String list(HttpSession s,Model m){if(sessions.current(s)==null)return "redirect:/login";m.addAttribute("records",history.findAllByOrderByCompletedAtDesc());return "history/list";}
}
