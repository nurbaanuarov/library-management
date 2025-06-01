package com.library.management.controller;

import com.library.management.model.Role;
import com.library.management.model.User;
import com.library.management.service.RoleService;
import com.library.management.service.UserService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final RoleService roleService;

    /**
     * 1) List all users
     */
    @GetMapping
    public String listUsers(Model model) {
        List<User> allUsers = userService.findAllUsers();
        model.addAttribute("users", allUsers);
        return "admin/users";
    }

    /**
     * 2) Show edit form for a single user
     */
    @GetMapping("/{id}")
    public String showEditForm(@PathVariable("id") long id, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            throw new ValidationException("User not found with id=" + id);
        }
        model.addAttribute("user", user);

        Set<Role> allRoles = roleService.findAllRoles();
        model.addAttribute("allRoles", allRoles);

        Set<Long> userRoleIds = user.getRoles()
                .stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
        model.addAttribute("userRoleIds", userRoleIds);

        return "admin/user-edit";
    }

    /**
     * 3) Process the form submission
     */
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
