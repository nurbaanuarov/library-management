package com.library.management.controller;

import com.library.management.model.Book;
import com.library.management.model.BookRequest;
import com.library.management.model.RequestType;
import com.library.management.service.BookService;
import com.library.management.service.ReaderRequestService;
import com.library.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/reader")
@RequiredArgsConstructor
public class ReaderController {

    private final BookService bookService;
    private final ReaderRequestService reqService;
    private final UserService userService;

    // Browse all books
    @GetMapping("/books")
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "reader/books";
    }

    // Book detail + request form
    @GetMapping("/books/{id}")
    public String showBook(@PathVariable Long id, Model m) {
        Book b = bookService.findById(id);
        m.addAttribute("book", b);
        m.addAttribute("types", RequestType.values());
        return "reader/book-detail";
    }

    // Submit a request
    @PostMapping("/books/{id}/request")
    public String placeRequest(@PathVariable Long id,
                               @RequestParam RequestType type,
                               Authentication auth) {
        Long userId = userService.findByUsername(auth.getName()).getId();
        reqService.createRequest(userId, id, type);
        return "redirect:/reader/requests";
    }

    // View & cancel own requests
    @GetMapping("/requests")
    public String myRequests(Model m, Authentication auth) {
        Long userId = userService.findByUsername(auth.getName()).getId();
        List<BookRequest> list = reqService.findByUser(userId);
        m.addAttribute("requests", list);
        return "reader/requests";
    }

    @PostMapping("/requests/{id}/cancel")
    public String cancel(@PathVariable Long id, Authentication auth) {
        Long userId = userService.findByUsername(auth.getName()).getId();
        reqService.cancelRequest(id, userId);
        return "redirect:/reader/requests";
    }
}
