package com.library.management.controller.librarian;

import com.library.management.model.Book;
import com.library.management.model.BookCopy;
import com.library.management.service.BookCopyService;
import com.library.management.service.BookService;
import com.library.management.web.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/librarian/books")
@RequiredArgsConstructor
public class LibrarianBookController {

    private final BookService     bookService;
    private final BookCopyService copyService;

    @GetMapping
    public String listBooks(
            @RequestParam(value="page", defaultValue="1") int page,
            @RequestParam(value="size", defaultValue="5") int size,
            Model model
    ) {
        Page<Book> bookPage = bookService.findPaginated(page, size);

        model.addAttribute("books",       bookPage.getContent());
        model.addAttribute("currentPage", bookPage.getPageNumber());
        model.addAttribute("totalPages",  bookPage.getTotalPages());
        model.addAttribute("size",        bookPage.getPageSize());
        return "librarian/books";
    }

    @GetMapping("/{bookId}/copies")
    public String manageCopies(
            @PathVariable Long bookId,
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
            @PathVariable Long bookId,
            @ModelAttribute("newCopy") BookCopy copy
    ) {
        Book book = bookService.findById(bookId);
        copy.setBook(book);
        copy.setStatus(com.library.management.model.CopyStatus.AVAILABLE);
        copyService.create(copy);
        return "redirect:/librarian/books/" + bookId + "/copies";
    }
}
