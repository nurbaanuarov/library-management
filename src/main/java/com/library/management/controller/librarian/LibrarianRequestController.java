package com.library.management.controller.librarian;

import com.library.management.model.RequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/librarian/requests")
@RequiredArgsConstructor
public class LibrarianRequestController {
    @GetMapping
    public String listRequests(Model m) {
        return "";
    }

    @PostMapping("/{id}/status")
    public String changeRequestStatus(@PathVariable Long id, @RequestParam RequestStatus status) {
        return "";
    }
}
