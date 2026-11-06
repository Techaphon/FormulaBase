package com.formulabase.web;

import com.formulabase.domain.MaterialUnit;
import com.formulabase.service.MaterialService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/materials")
public class MaterialController {
    private final MaterialService service;
    private final SessionHelper sessionHelper;
    private final com.formulabase.repository.FormulaMaterialRepository formulaMaterials;
    public MaterialController(MaterialService service, SessionHelper sessionHelper, com.formulabase.repository.FormulaMaterialRepository formulaMaterials) { this.service = service; this.sessionHelper = sessionHelper; this.formulaMaterials = formulaMaterials; }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) MaterialUnit unit,
                       HttpSession session, Model model) {
        if (sessionHelper.current(session) == null) return "redirect:/login";
        var materials = service.search(keyword);
        if (unit != null) materials = materials.stream().filter(material -> material.getStockUnit() == unit).toList();
        model.addAttribute("materials", materials);
        model.addAttribute("usedMaterialIds", formulaMaterials.findUsedMaterialIds());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("selectedUnit", unit);
        model.addAttribute("units", MaterialUnit.values());
        return "materials/list";
    }

    @PostMapping
    public String create(@RequestParam String code, @RequestParam String name,
                         @RequestParam MaterialUnit unit, HttpSession session, RedirectAttributes redirect) {
        var user = sessionHelper.current(session);
        if (user == null) return "redirect:/login";
        if (user.getRole() != com.formulabase.domain.UserRole.QC && user.getRole() != com.formulabase.domain.UserRole.CEO) return "redirect:/materials";
        try {
            service.create(code, name, unit, user);
            redirect.addFlashAttribute("message", "Material saved");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/materials";
    }
    @PostMapping("/batch")
    public String createBatch(@RequestParam java.util.List<String> code, @RequestParam java.util.List<String> name,
                              @RequestParam java.util.List<MaterialUnit> unit, HttpSession session, RedirectAttributes redirect) {
        var user = sessionHelper.current(session);
        if (user == null) return "redirect:/login";
        if (user.getRole() != com.formulabase.domain.UserRole.QC && user.getRole() != com.formulabase.domain.UserRole.CEO) return "redirect:/materials";
        try { service.createBatch(code, name, unit, user); redirect.addFlashAttribute("message", "Material saved"); }
        catch (RuntimeException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/materials";
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable java.util.UUID id, HttpSession session, RedirectAttributes redirect) {
        var user=sessionHelper.current(session);if(user==null)return "redirect:/login";if(user.getRole()!=com.formulabase.domain.UserRole.QC&&user.getRole()!=com.formulabase.domain.UserRole.CEO)return "redirect:/materials";
        try{service.delete(id,user);redirect.addFlashAttribute("message","Material deleted");}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());}return "redirect:/materials";
    }
}
