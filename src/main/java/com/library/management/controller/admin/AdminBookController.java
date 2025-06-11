package com.library.management.controller.admin;

import com.library.management.model.Author;
import com.library.management.model.Book;
import com.library.management.model.Genre;
import com.library.management.service.AuthorService;
import com.library.management.service.BookService;
import com.library.management.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/books")
@RequiredArgsConstructor
public class AdminBookController {

    private final BookService bookService;
    private final AuthorService authorService;
    private final GenreService genreService;

    @GetMapping
    public String listBooks(Model model) {
        List<Book> books = bookService.findAll();
        List<Author> authors = authorService.findAll();
        List<Genre> genres = genreService.findAll();

        model.addAttribute("books", books);
        model.addAttribute("book", new Book());
        model.addAttribute("authors", authors);
        model.addAttribute("genres", genres);
        return "admin/books";
    }

    @PostMapping("/new")
    public String createBook(@ModelAttribute Book book) {
        bookService.save(book);
        return "redirect:/admin/books";
    }

    @PostMapping("/delete")
    public String deleteBook(@RequestParam("id") Long id) {
        bookService.deleteById(id);
        return "redirect:/admin/books";
    }
}
