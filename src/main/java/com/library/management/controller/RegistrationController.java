package com.library.management.controller;

import com.library.management.dto.RegistrationForm;
import com.library.management.service.RegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService regService;

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("regForm", new RegistrationForm());
        return "register";
    }

    @PostMapping
    public String process(
            @Valid @ModelAttribute("regForm") RegistrationForm form,
            BindingResult br,
            RedirectAttributes flash
    ) {
        try {
            regService.register(form);
            return "redirect:/login?registered";
        } catch (ValidationException e) {
            br.reject("registration.failed", e.getMessage());
            flash.addFlashAttribute("registrationError", e.getMessage());
            flash.addFlashAttribute("regForm", form);
            return "redirect:/register";
        }
    }
}
