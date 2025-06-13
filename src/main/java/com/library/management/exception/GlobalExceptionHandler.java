package com.library.management.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({
            AuthorNotFoundException.class,
            BookNotFoundException.class,
            BookCopyNotFoundException.class,
            BookRequestNotFoundException.class,
            GenreNotFoundException.class,
            RoleNotFoundException.class,
            UserNotFoundException.class
    })
    public String handleNotFound(RuntimeException ex, Model model) {
        model.addAttribute("errorTitle", "Resource Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/not-found";
    }

    @ExceptionHandler(InputValidationException.class)
    public String handleValidation(InputValidationException ex, Model model) {
        model.addAttribute("errorTitle", "Invalid Input");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/invalid-input";
    }

    @ExceptionHandler(EntityInUseException.class)
    public String handleEntityInUse(EntityInUseException ex, Model model) {
        model.addAttribute("errorTitle", "Cannot Delete");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/entity-in-use";
    }

    @ExceptionHandler(DataAccessException.class)
    public String handleDatabase(DataAccessException ex, Model model) {
        model.addAttribute("errorTitle", "Database Error");
        model.addAttribute("errorMessage", "An unexpected database error occurred.");
        return "error/database-error";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException ex, Model model) {
        model.addAttribute("errorTitle", "Invalid Operation");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/illegal-state";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, Model model) {
        model.addAttribute("errorTitle", "Unexpected Error");
        model.addAttribute("errorMessage", "Something went wrong.");
        return "error/general";
    }
}
