package com.library.management.controller.admin;

import com.library.management.model.Author;
import com.library.management.model.Book;
import com.library.management.model.Genre;
import com.library.management.service.AuthorService;
import com.library.management.service.BookService;
import com.library.management.service.GenreService;
import com.library.management.web.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/books")
@RequiredArgsConstructor
public class AdminBookController {

    private final BookService bookService;
    private final AuthorService authorService;
    private final GenreService genreService;

    @GetMapping
    public String listBooks(
            @RequestParam(value="page", defaultValue="1") int page,
            @RequestParam(value="size", defaultValue="5") int size,
            Model model
    ) {
        Page<Book> bookPage = bookService.findPaginated(page, size);

        model.addAttribute("books",       bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",  bookPage.getTotalPages());
        model.addAttribute("size",        size);

        // for your “Add new book” form
        model.addAttribute("book",    new Book());
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres",  genreService.findAll());

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
