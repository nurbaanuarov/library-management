package com.library.management.controller;

import com.library.management.dto.RegistrationForm;
import com.library.management.exception.InputValidationException;
import com.library.management.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
            BindingResult br
    ) {
        if (br.hasErrors()) {
            return "register";
        }

        try {
            regService.register(form);
            return "redirect:/login?registered";
        } catch (InputValidationException e) {
            br.reject("registration.failed", e.getMessage());
            return "register";
        }
    }

}
