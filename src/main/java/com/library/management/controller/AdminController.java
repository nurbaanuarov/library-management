package com.library.management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @GetMapping
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/authors")
    public String listAuthors() {
        return "admin/authors";
    }

    @GetMapping("/books")
    public String listBooks() {
        return "admin/books";
    }
}
