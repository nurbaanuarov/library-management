package com.library.management.controller.reader;

import com.library.management.model.Book;
import com.library.management.model.BookRequest;
import com.library.management.model.CopyStatus;
import com.library.management.model.RequestType;
import com.library.management.service.BookCopyService;
import com.library.management.service.BookService;
import com.library.management.service.ReaderRequestService;
import com.library.management.service.UserService;
import com.library.management.web.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reader")
@RequiredArgsConstructor
public class ReaderController {

    private final BookService bookService;
    private final ReaderRequestService reqService;
    private final UserService userService;
    private final BookCopyService bookCopyService;

    @GetMapping
    public String dashboard() {
        return "reader/dashboard";
    }

    @GetMapping("/books")
    public String listBooks(
            @RequestParam(value="page", defaultValue="1")    int page,
            @RequestParam(value="size", defaultValue="5")   int size,
            Model model
    ) {
        Page<Book> bookPage = bookService.findPaginated(page, size);
        // build your availability map exactly as before
        Map<Long, Boolean> availability = bookPage.getContent().stream()
                .collect(Collectors.toMap(
                        Book::getId,
                        b -> bookCopyService.findByBookId(b.getId())
                                .stream().anyMatch(c -> c.getStatus()==CopyStatus.AVAILABLE)
                ));

        model.addAttribute("books",        bookPage.getContent());
        model.addAttribute("availability", availability);
        model.addAttribute("currentPage",  bookPage.getPageNumber());
        model.addAttribute("totalPages",   bookPage.getTotalPages());
        model.addAttribute("pageSize",     bookPage.getPageSize());
        return "reader/books";
    }




    @GetMapping("/books/{id}")
    public String showBook(@PathVariable("id") Long id, Model m) {
        Book b = bookService.findById(id);
        boolean hasAvailable = bookService.hasAvailableCopies(id);

        m.addAttribute("book", b);
        m.addAttribute("types", RequestType.values());
        m.addAttribute("hasAvailable", hasAvailable);
        return "reader/book-detail";
    }

    @PostMapping("/books/{id}/request")
    public String placeRequest(@PathVariable("id") Long id,
                               @RequestParam("type") RequestType type,
                               Authentication auth) {
        Long userId = userService.findByUsername(auth.getName()).getId();
        reqService.createRequest(userId, id, type);
        return "redirect:/reader/requests";
    }

    @GetMapping("/requests")
    public String myRequests(Model m, Authentication auth) {
        Long userId = userService.findByUsername(auth.getName()).getId();
        List<BookRequest> list = reqService.findByUser(userId);
        m.addAttribute("requests", list);
        return "reader/requests";
    }

    @PostMapping("/requests/{id}/cancel")
    public String cancel(@PathVariable("id") Long id, Authentication auth) {
        Long userId = userService.findByUsername(auth.getName()).getId();
        reqService.cancelRequest(id, userId);
        return "redirect:/reader/requests";
    }
}
