package com.formulabase.web;

import com.formulabase.domain.User;
import com.formulabase.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class SessionHelper {
    private static final String USERNAME = "FORMULA_BASE_USERNAME";
    private static final String USER_ID = "FORMULA_BASE_USER_ID";
    private final UserService userService;
    public SessionHelper(UserService userService) { this.userService = userService; }
    public void login(HttpSession session, User user) { session.setAttribute(USERNAME, user.getUsername()); session.setAttribute(USER_ID, user.getId()); }
    public User current(HttpSession session) {
        Object username = session.getAttribute(USERNAME);
        Object id = session.getAttribute(USER_ID);
        if (username == null || id == null) return null;
        User user = userService.findActive(username.toString());
        return user != null && user.getId().equals(id) ? user : null;
    }
    public void logout(HttpSession session) { session.invalidate(); }
}
