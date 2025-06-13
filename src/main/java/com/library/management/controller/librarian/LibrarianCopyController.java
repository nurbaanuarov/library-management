package com.library.management.controller.librarian;

import com.library.management.model.Book;
import com.library.management.model.BookCopy;
import com.library.management.model.CopyStatus;
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

    @GetMapping
    public String listCopies(Model model) {
        model.addAttribute("copies",   copyService.findAll());
        model.addAttribute("statuses", CopyStatus.values());
        return "librarian/copies";
    }

    @GetMapping("/{id}")
    public String showDetail(@PathVariable("id") Long id, Model model) {
        BookCopy copy = copyService.findById(id);
        Book     book = bookService.findById(copy.getBook().getId());

        List<User> readers = userService.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> "READER".equals(r.getName())))
                .toList();

        model.addAttribute("copy",    copy);
        model.addAttribute("book",    book);
        model.addAttribute("readers", readers);
        return "librarian/copy-details";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable("id") Long id,
                               @RequestParam("status") CopyStatus status) {
        copyService.changeStatus(id, status);
        return "redirect:/librarian/copies";
    }

    @PostMapping("/{copyId}/update")
    public String update(
            @PathVariable("copyId") Long copyId,
            @RequestParam("inventoryNumber") String inventoryNumber,
            @RequestParam("status") CopyStatus status
    ) {
        BookCopy copy = copyService.findById(copyId);
        copy.setInventoryNumber(inventoryNumber);
        copy.setStatus(status);
        copyService.update(copy);
        return "redirect:/librarian/copies/" + copyId;
    }


    @PostMapping("/{id}/issue")
    public String issueInLibrary(@PathVariable("id") Long id,
                                 @RequestParam("userId") Long userId) {
        librarianService.issueInLibrary(userId, id);
        return "redirect:/librarian/copies/" + id;
    }

    @PostMapping("/{id}/giveReserved")
    public String giveReserved(@PathVariable("id") Long id) {
        librarianService.giveReserved(id);
        return "redirect:/librarian/copies/" + id;
    }

    @PostMapping("/{id}/cancelReservation")
    public String cancelReservation(@PathVariable("id") Long id) {
        librarianService.cancelReservation(id);
        return "redirect:/librarian/copies/" + id;
    }

    @PostMapping("/{id}/return")
    public String returnCopy(@PathVariable("id") Long id) {
        librarianService.returnCopy(id);
        return "redirect:/librarian/copies/" + id;
    }
}
