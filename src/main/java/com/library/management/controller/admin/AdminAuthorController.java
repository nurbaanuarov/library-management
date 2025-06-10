package com.library.management.controller.admin;

import com.library.management.model.Author;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/authors")
@RequiredArgsConstructor
public class AdminAuthorController {

//    private final AuthorService authorService;

    @GetMapping
    public String listAuthors(Model model) {
//        model.addAttribute("authors", authorService.findAll());
//        model.addAttribute("author", new Author());
        return "admin/authors";
    }

    @PostMapping("/new")
    public String createAuthor(@ModelAttribute Author author) {
//        authorService.save(author);
        return "redirect:/admin/authors";
    }
}
