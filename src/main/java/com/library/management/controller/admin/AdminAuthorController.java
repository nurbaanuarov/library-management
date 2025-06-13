package com.library.management.controller.admin;

import com.library.management.model.Author;
import com.library.management.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/authors")
@RequiredArgsConstructor
public class AdminAuthorController {

    private final AuthorService authorService;

    @GetMapping
    public String listAuthors(Model model) {
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("author", new Author());
        return "admin/authors";
    }

    @PostMapping("/new")
    public String createAuthor(@ModelAttribute Author author) {
        authorService.save(author);
        return "redirect:/admin/authors";
    }

    @PostMapping("/delete")
    public String deleteAuthor(@RequestParam("id") Long id) {
        authorService.deleteById(id);
        return "redirect:/admin/authors";
    }
}
