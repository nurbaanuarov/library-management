package com.library.management.controller.librarian;

import com.library.management.model.RequestStatus;
import com.library.management.service.LibrarianRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/librarian/requests")
@RequiredArgsConstructor
public class LibrarianRequestController {
    private final LibrarianRequestService requestService;

    @GetMapping
    public String listRequests(Model model) {
        model.addAttribute("requests", requestService.findAll());
        model.addAttribute("statuses", RequestStatus.values());
        return "librarian/requests";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam RequestStatus status) {
        requestService.updateStatus(id, status);
        return "redirect:/librarian/requests";
    }
}