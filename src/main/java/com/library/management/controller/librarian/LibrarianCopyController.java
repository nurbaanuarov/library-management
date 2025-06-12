package com.library.management.controller.librarian;

import com.library.management.model.Book;
import com.library.management.model.BookCopy;
import com.library.management.model.User;
import com.library.management.service.BookService;
import com.library.management.service.BookCopyService;
import com.library.management.service.LibrarianService;
import com.library.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Controller
@RequestMapping("/librarian/copies")
@RequiredArgsConstructor
public class LibrarianCopyController {

    private final BookCopyService copyService;
    private final BookService     bookService;
    private final UserService     userService;
    private final LibrarianService librarianService;

    @GetMapping("/{copyId}")
    public String showDetail(@PathVariable("copyId") Long copyId, Model model) {
        BookCopy copy = copyService.findById(copyId);
        Book    book = bookService.findById(copy.getBook().getId());

        List<User> readers = userService.findAll().stream()
                .filter(u -> u.getRoles().stream()
                        .anyMatch(r -> "READER".equals(r.getName())))
                .toList();

        model.addAttribute("copy",   copy);
        model.addAttribute("book",   book);
        model.addAttribute("readers", readers);
        return "librarian/copy-details";
    }

    @PostMapping("/{copyId}/issue")
    public String issueInLibrary(@PathVariable("copyId") Long copyId,
                                 @RequestParam("username") String username) {
        User user = userService.findByUsername(username);
        librarianService.issueInLibrary(user.getId(), copyId);
        return "redirect:/librarian/copies/" + copyId;
    }

    @PostMapping("/{copyId}/return")
    public String returnCopy(@PathVariable("copyId") Long copyId) {
        librarianService.returnCopy(copyId);
        return "redirect:/librarian/copies/" + copyId;
    }

    @PostMapping("/{copyId}/giveReserved")
    public String giveReserved(@PathVariable("copyId") Long copyId) {
        librarianService.giveReserved(copyId);
        return "redirect:/librarian/copies/" + copyId;
    }

    @PostMapping("/{copyId}/cancelReservation")
    public String cancelReservation(@PathVariable("copyId") Long copyId) {
        librarianService.cancelReservation(copyId);
        return "redirect:/librarian/copies/" + copyId;
    }

    @PostMapping("/{copyId}/update")
    public String updateInventory(@PathVariable("copyId") Long copyId,
                                  @RequestParam("inventoryNumber") String inventoryNumber) {
        BookCopy copy = copyService.findById(copyId);
        copy.setInventoryNumber(inventoryNumber);
        copyService.update(copy);
        return "redirect:/librarian/copies/" + copyId;
    }
}
