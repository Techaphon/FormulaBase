package com.formulabase.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Double-submit token protection for the server-rendered session application. */
@Component
public class CsrfProtectionFilter extends OncePerRequestFilter {
    private static final String SESSION_KEY = "FORMULA_BASE_CSRF";
    private static final String COOKIE_NAME = "XSRF-TOKEN";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = (String) request.getSession(true).getAttribute(SESSION_KEY);
        if (token == null) {
            byte[] value = new byte[32]; RANDOM.nextBytes(value);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(value);
            request.getSession().setAttribute(SESSION_KEY, token);
        }
        // Render the token directly in server-side forms. This keeps login safe even
        // if the browser delays or blocks the JavaScript fallback that adds tokens.
        request.setAttribute("csrfToken", token);
        addCookie(response, token, request.isSecure());
        if (!HttpMethod.GET.matches(request.getMethod()) && !HttpMethod.HEAD.matches(request.getMethod()) && !HttpMethod.OPTIONS.matches(request.getMethod())) {
            String supplied = request.getParameter("_csrf");
            // The login page is reachable before any application JavaScript has run.
            // Accept the SameSite cookie only for that first form post as a safe fallback;
            // every authenticated state-changing request still requires its hidden token.
            if (supplied == null && "/login".equals(request.getServletPath())) {
                supplied = cookieValue(request, COOKIE_NAME);
            }
            if (supplied == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void addCookie(HttpServletResponse response, String token, boolean secure) {
        response.addHeader("Set-Cookie", COOKIE_NAME + "=" + token + "; Path=/; SameSite=Strict" + (secure ? "; Secure" : ""));
    }

    private String cookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (var cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}
