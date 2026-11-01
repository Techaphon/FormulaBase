package com.formulabase.web;

import com.formulabase.domain.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RoleAccess implements WebMvcConfigurer {
    private final SessionHelper sessions;
    public RoleAccess(SessionHelper sessions) { this.sessions = sessions; }
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                var user = sessions.current(request.getSession());
                String path = request.getServletPath();
                if (user == null) { response.sendRedirect("/login"); return false; }
                if (user.getRole() == UserRole.CEO) return true;
                boolean read = request.getMethod().equals("GET");
                boolean allowed = path.equals("/") || path.equals("/workflow") || path.equals("/logout");
                if (user.getRole() == UserRole.QC) allowed |= path.startsWith("/quality") || path.startsWith("/materials") || (path.startsWith("/formulas") && !path.endsWith("/confirm")) || (read && path.startsWith("/print/bom/"));
                if (user.getRole() == UserRole.PC) allowed |= path.startsWith("/planning") || path.startsWith("/history") || (read && (path.equals("/formulas") || path.startsWith("/print/")));
                if (user.getRole() == UserRole.WORKER) allowed |= path.startsWith("/worker/") || (read && path.startsWith("/print/work-order/"));
                if (!allowed) {
                    if (read) response.sendRedirect("/");
                    else response.sendError(403);
                }
                return allowed;
            }
            @Override public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView model) {
                if (model == null || model.getViewName() == null || model.getViewName().startsWith("redirect:")) return;
                var user = sessions.current(request.getSession());
                if (user == null) return;
                var role = user.getRole();
                model.addObject("currentUser", user);
                model.addObject("canManageFormula", role == UserRole.CEO || role == UserRole.QC);
                model.addObject("canPlan", role == UserRole.CEO || role == UserRole.PC);
                model.addObject("isCeo", role == UserRole.CEO);
                model.addObject("canExecute", role == UserRole.CEO || role == UserRole.WORKER);
            }
        }).addPathPatterns("/", "/workflow", "/formulas/**", "/materials/**", "/planning/**", "/worker/**", "/quality/**", "/users/**", "/audit/**", "/history/**", "/print/**", "/logout");
    }
}
