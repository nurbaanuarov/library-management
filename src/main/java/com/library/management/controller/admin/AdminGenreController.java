package com.library.management.controller.admin;

import com.library.management.model.Genre;
import com.library.management.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/genres")
@RequiredArgsConstructor
public class AdminGenreController {

    private final GenreService genreService;

    @GetMapping
    public String listGenres(Model model) {
        model.addAttribute("genres", genreService.findAll());
        model.addAttribute("genre", new Genre());
        return "admin/genres";
    }

    @PostMapping("/new")
    public String createGenre(@ModelAttribute Genre genre) {
        genreService.save(genre);
        return "redirect:/admin/genres";
    }

    @PostMapping("/delete")
    public String deleteGenre(@RequestParam("id") Long id) {
        Genre genre = Genre.builder().id(id).build();
        genreService.delete(genre);
        return "redirect:/admin/genres";
    }
}
