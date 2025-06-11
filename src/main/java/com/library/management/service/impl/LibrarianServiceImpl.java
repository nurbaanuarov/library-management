package com.library.management.service.impl;

import com.library.management.dao.BookCopyDAO;
import com.library.management.dao.BookRequestDAO;
import com.library.management.dao.UserDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.exception.UserNotFoundException;
import com.library.management.model.*;
import com.library.management.service.LibrarianService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class LibrarianServiceImpl implements LibrarianService {

    private final UserDAO userDAO;
    private final BookCopyDAO bookCopyDAO;
    private final BookRequestDAO bookRequestDAO;

    @Override
    public void issueCopyManually(Long userId, Long copyId, RequestType type, LocalDate returnDate) {
        // Step 1: Fetch full User and Copy objects
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id=" + userId));
        BookCopy copy = bookCopyDAO.findById(copyId)
                .orElseThrow(() -> new DataAccessException("Copy not found with id=" + copyId));

        // Step 2: Update copy status
        bookCopyDAO.updateStatus(copyId, CopyStatus.ISSUED);

        // Step 3: Create and save BookRequest
        BookRequest request = BookRequest.builder()
                .user(user)
                .copy(copy)
                .type(type)
                .status(RequestStatus.ISSUED)
                .requestDate(LocalDateTime.now())  // Optional for manual issue, but good to fill
                .issueDate(LocalDateTime.now())
                .returnDate(returnDate.atStartOfDay())
                .build();

        bookRequestDAO.save(request);
    }

}
