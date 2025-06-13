package com.library.management.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityInUseException.class)
    public String handleEntityInUse(EntityInUseException ex, Model model) {
        model.addAttribute("errorTitle", "Cannot Delete");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/entity-in-use";
    }
}
