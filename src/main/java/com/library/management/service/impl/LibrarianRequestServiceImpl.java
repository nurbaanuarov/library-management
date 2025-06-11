package com.library.management.service.impl;

import com.library.management.dao.BookRequestDAO;
import com.library.management.dao.BookCopyDAO;
import com.library.management.dao.BookDAO;
import com.library.management.dao.UserDAO;
import com.library.management.exception.BookCopyNotFoundException;
import com.library.management.exception.BookNotFoundException;
import com.library.management.exception.BookRequestNotFoundException;
import com.library.management.exception.UserNotFoundException;
import com.library.management.model.*;
import com.library.management.service.LibrarianRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LibrarianRequestServiceImpl implements LibrarianRequestService {
    private final BookRequestDAO requestDAO;
    private final UserDAO userDAO;
    private final BookCopyDAO copyDAO;
    private final BookDAO bookDAO;

    @Override
    @Transactional(readOnly = true)
    public List<BookRequest> findAll() {
        return requestDAO.findAll()
                .stream().map(bookRequestMapping())
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(Long requestId, RequestStatus newStatus) {
        BookRequest r = requestDAO.findById(requestId)
                .orElseThrow(() -> new BookRequestNotFoundException("Request not found"));

        Long copyId = r.getCopy().getId();
        if (newStatus == RequestStatus.ISSUED) {
            copyDAO.updateStatus(copyId, CopyStatus.ISSUED);
            r.setIssueDate(LocalDateTime.now());
        } else if (newStatus == RequestStatus.RETURNED || newStatus == RequestStatus.CANCELED) {
            copyDAO.updateStatus(copyId, CopyStatus.AVAILABLE);
            r.setReturnDate(LocalDateTime.now());
        }

        r.setStatus(newStatus);
        requestDAO.update(r);
    }

    private Function<BookRequest, BookRequest> bookRequestMapping() {
        return request -> {
            request.setUser(userDAO.findById(request.getUser().getId())
                    .orElseThrow(() -> new UserNotFoundException("User not found")));
            BookCopy copy = copyDAO.findById(request.getCopy().getId())
                    .orElseThrow(() -> new BookCopyNotFoundException("Copy not found"));
            Book book = bookDAO.findById(copy.getBook().getId())
                    .orElseThrow(() -> new BookNotFoundException("Book not found"));
            copy.setBook(book);
            request.setCopy(copy);
            return request;
        };
    }
}