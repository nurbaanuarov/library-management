package com.library.management.controller.librarian;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/librarian")
public class LibrarianMainController {

    @GetMapping
    public String dashboard() {
        return "librarian/dashboard";
    }
}
