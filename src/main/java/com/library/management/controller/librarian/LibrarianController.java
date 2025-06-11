package com.library.management.controller.librarian;

import com.library.management.model.BookCopy;
import com.library.management.model.CopyStatus;
import com.library.management.service.LibrarianCopyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/librarian/copies")
@RequiredArgsConstructor
public class LibrarianController {

    private final LibrarianCopyService copyService;

    @GetMapping
    public String listCopies(Model model) {
        model.addAttribute("copies", copyService.findAll());
        model.addAttribute("statuses", CopyStatus.values());
        return "librarian/book-copies";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam CopyStatus status) {
        copyService.changeStatus(id, status);
        return "redirect:/librarian/copies";
    }

    @PostMapping("/update")
    public String updateCopy(@ModelAttribute BookCopy copy) {
        copyService.update(copy);
        return "redirect:/librarian/copies";
    }
}