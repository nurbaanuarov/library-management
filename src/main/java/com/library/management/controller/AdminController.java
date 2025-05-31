package com.library.management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    /**
     * GET /admin
     * Simply shows a “Welcome to the admin page” dashboard,
     * with links to Users / Authors / Books.
     */
    @GetMapping
    public String dashboard(Model model) {
        // We can add any model attributes here if needed; for now, it's just a welcome message
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "admin/dashboard";
    }

    /**
     * (optional placeholder) GET /admin/users
     * You will likely want to list all users here.
     * For now it just returns a stub template.
     */
    @GetMapping("/users")
    public String listUsers(Model model) {
        // Stub: later you’ll populate model with a list of users
        model.addAttribute("pageTitle", "All Users");
        return "admin/users";
    }

    /**
     * (optional placeholder) GET /admin/authors
     * Stub for listing all authors.
     */
    @GetMapping("/authors")
    public String listAuthors(Model model) {
        model.addAttribute("pageTitle", "All Authors");
        return "admin/authors";
    }

    /**
     * (optional placeholder) GET /admin/books
     * Stub for listing all books.
     */
    @GetMapping("/books")
    public String listBooks(Model model) {
        model.addAttribute("pageTitle", "All Books");
        return "admin/books";
    }
}
