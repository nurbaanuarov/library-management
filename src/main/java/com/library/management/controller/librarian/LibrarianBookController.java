package com.library.management.controller.librarian;

import com.library.management.model.Book;
import com.library.management.model.BookCopy;
import com.library.management.service.BookService;
import com.library.management.service.BookCopyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/librarian/books")
@RequiredArgsConstructor
public class LibrarianBookController {

    private final BookService bookService;
    private final BookCopyService copyService;

    @GetMapping
    public String listBooks(Model model) {
        List<Book> books = bookService.findAll(0, Integer.MAX_VALUE);
        model.addAttribute("books", books);
        return "librarian/books";
    }

    @GetMapping("/{bookId}/copies")
    public String manageCopies(
            @PathVariable("bookId") Long bookId,
            Model model
    ) {
        Book book = bookService.findById(bookId);
        List<BookCopy> copies = copyService.findByBookId(bookId);

        model.addAttribute("book", book);
        model.addAttribute("copies", copies);
        model.addAttribute("newCopy", new BookCopy());
        return "librarian/book-copies";
    }

    @PostMapping("/{bookId}/copies")
    public String createCopy(
            @PathVariable("bookId") Long bookId,
            @ModelAttribute("newCopy") BookCopy copy
    ) {
        Book book = bookService.findById(bookId);
        copy.setBook(book);
        copy.setStatus(com.library.management.model.CopyStatus.AVAILABLE);
        copyService.create(copy);
        return "redirect:/librarian/books/" + bookId + "/copies";
    }
}
