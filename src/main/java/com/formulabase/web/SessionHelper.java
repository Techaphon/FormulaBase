package com.formulabase.web;

import com.formulabase.domain.User;
import com.formulabase.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class SessionHelper {
    private static final String USERNAME = "FORMULA_BASE_USERNAME";
    private final UserService userService;
    public SessionHelper(UserService userService) { this.userService = userService; }
    public void login(HttpSession session, User user) { session.setAttribute(USERNAME, user.getUsername()); }
    public User current(HttpSession session) {
        Object username = session.getAttribute(USERNAME);
        if (username == null) return null;
        return userService.findActive(username.toString());
    }
    public void logout(HttpSession session) { session.invalidate(); }
}
