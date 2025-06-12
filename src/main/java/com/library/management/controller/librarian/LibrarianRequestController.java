package com.library.management.controller.librarian;

import com.library.management.model.RequestStatus;
import com.library.management.service.LibrarianRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/librarian/requests")
@RequiredArgsConstructor
public class LibrarianRequestController {
    private final LibrarianRequestService requestService;

    @GetMapping
    public String listRequests(Model model) {
        model.addAttribute("requests",    requestService.findAll());
        model.addAttribute("allStatuses", RequestStatus.values());
        model.addAttribute("openStatuses",
                List.of(RequestStatus.PENDING, RequestStatus.ISSUED));
        return "librarian/requests";
    }


    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") RequestStatus status) {
        requestService.updateStatus(id, status);
        return "redirect:/librarian/requests";
    }
}