package com.k2so.watcher.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("activePage")
    public String activePage(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/dashboard")) {
            return "dashboard";
        } else if (uri.startsWith("/devices")) {
            return "devices";
        } else if (uri.startsWith("/scan")) {
            return "scan";
        } else if (uri.startsWith("/admin")) {
            return "admin";
        }
        return "";
    }
}
