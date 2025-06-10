package com.library.management.controller.admin;

import com.library.management.dto.RegistrationForm;
import com.library.management.model.Role;
import com.library.management.model.User;
import com.library.management.service.RoleService;
import com.library.management.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final RoleService roleService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("allRoles", roleService.findAllRoles());
        model.addAttribute("form", new RegistrationForm());
        return "admin/users";
    }

    @PostMapping("/new")
    public String createUser(@Valid @ModelAttribute("form") RegistrationForm form,
                             @RequestParam("roles") Set<Long> roleIds,
                             RedirectAttributes flash) {
        try {
            userService.createUser(form, roleIds);
        } catch (ValidationException e) {
            flash.addFlashAttribute("form", form);
            flash.addFlashAttribute("userCreationError", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}")
    public String showEditForm(@PathVariable("id") long id, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            throw new ValidationException("User not found with id=" + id);
        }

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));

        model.addAttribute("user", user);
        model.addAttribute("isAdminUser", isAdmin);

        Set<Role> allRoles = roleService.findAllRoles();
        model.addAttribute("allRoles", allRoles);

        Set<Long> userRoleIds = user.getRoles()
                .stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
        model.addAttribute("userRoleIds", userRoleIds);

        return "admin/user-edit";
    }


    @PostMapping("/{id}")
    public String processEditForm(
            @PathVariable("id") long id,
            @RequestParam("enabled") Boolean enabledCheckbox,
            @RequestParam("roles") Set<Long> rolesSelected
    ) {
        User user = userService.findById(id);

        if (user == null) {
            throw new ValidationException("User not found with id=" + id);
        }

        user.setEnabled(enabledCheckbox);

        userService.updateUser(user, rolesSelected);

        return "redirect:/admin/users";
    }
}
