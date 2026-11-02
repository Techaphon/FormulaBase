package com.formulabase.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Keeps expected user-facing validation failures out of Spring's Whitelabel page. */
@ControllerAdvice(annotations = Controller.class)
public class WebErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(WebErrorHandler.class);
    private final UiText uiText;

    public WebErrorHandler(UiText uiText) { this.uiText = uiText; }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String knownFailure(RuntimeException exception, HttpServletRequest request,
                               HttpServletResponse response, Model model) {
        boolean forbidden = exception.getMessage() != null && exception.getMessage().toLowerCase().contains("permission");
        response.setStatus(forbidden ? HttpServletResponse.SC_FORBIDDEN : HttpServletResponse.SC_BAD_REQUEST);
        model.addAttribute("title", forbidden ? "Access denied" : "Action could not be completed");
        model.addAttribute("message", uiText.translate(exception.getMessage()));
        model.addAttribute("backUrl", "/workflow");
        return "error/application";
    }

    @ExceptionHandler(Exception.class)
    public String unexpectedFailure(Exception exception, HttpServletRequest request,
                                    HttpServletResponse response, Model model) {
        log.error("Unexpected request failure for {}", request.getRequestURI(), exception);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("title", "Something went wrong");
        model.addAttribute("message", "The request could not be completed. Please return to the application and try again.");
        model.addAttribute("backUrl", "/workflow");
        return "error/application";
    }
}
