package com.library.management.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Helper to format “METHOD URI” */
    private String reqInfo(HttpServletRequest req) {
        return req.getMethod() + " " + req.getRequestURI();
    }

    @ExceptionHandler({
            AuthorNotFoundException.class,
            BookNotFoundException.class,
            BookCopyNotFoundException.class,
            BookRequestNotFoundException.class,
            GenreNotFoundException.class,
            RoleNotFoundException.class,
            UserNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(RuntimeException ex,
                                 HttpServletRequest req,
                                 Model model) {
        String traceId = UUID.randomUUID().toString();
        log.warn("TRACE[{}] 404 {}, message={}", traceId, reqInfo(req), ex.getMessage());
        model.addAttribute("errorTitle",   "Resource Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("traceId",      traceId);
        return "error/not-found";
    }

    @ExceptionHandler(InputValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidation(InputValidationException ex,
                                   HttpServletRequest req,
                                   Model model) {
        String traceId = UUID.randomUUID().toString();
        log.warn("TRACE[{}] 400 {}, validation error: {}", traceId, reqInfo(req), ex.getMessage());
        model.addAttribute("errorTitle",   "Invalid Input");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("traceId",      traceId);
        return "error/invalid-input";
    }

    @ExceptionHandler(EntityInUseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleEntityInUse(EntityInUseException ex,
                                    HttpServletRequest req,
                                    Model model) {
        String traceId = UUID.randomUUID().toString();
        log.warn("TRACE[{}] 409 {}, in-use: {}", traceId, reqInfo(req), ex.getMessage());
        model.addAttribute("errorTitle",   "Resource In Use");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("traceId",      traceId);
        return "error/entity-in-use";
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleDatabase(DataAccessException ex,
                                 HttpServletRequest req,
                                 Model model) {
        String traceId = UUID.randomUUID().toString();
        log.error("TRACE[{}] 500 {}, database error", traceId, reqInfo(req), ex);
        model.addAttribute("errorTitle",   "Database Error");
        model.addAttribute("errorMessage", "An unexpected database error occurred.");
        model.addAttribute("traceId",      traceId);
        return "error/database-error";
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalState(IllegalStateException ex,
                                     HttpServletRequest req,
                                     Model model) {
        String traceId = UUID.randomUUID().toString();
        log.warn("TRACE[{}] 400 {}, illegal state: {}", traceId, reqInfo(req), ex.getMessage());
        model.addAttribute("errorTitle",   "Invalid Operation");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("traceId",      traceId);
        return "error/illegal-state";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex,
                                HttpServletRequest req,
                                Model model) {
        String traceId = UUID.randomUUID().toString();
        log.error("TRACE[{}] 500 {}, unexpected", traceId, reqInfo(req), ex);
        model.addAttribute("errorTitle",   "Unexpected Error");
        model.addAttribute("errorMessage", "Something went wrong.");
        model.addAttribute("traceId",      traceId);
        return "error/general";
    }
}
