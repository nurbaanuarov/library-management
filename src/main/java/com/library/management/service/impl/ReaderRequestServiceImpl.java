package com.library.management.service.impl;

import com.library.management.dao.BookRequestDAO;
import com.library.management.exception.BookRequestNotFoundException;
import com.library.management.model.*;
import com.library.management.service.BookCopyService;
import com.library.management.service.BookService;
import com.library.management.service.ReaderRequestService;
import com.library.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReaderRequestServiceImpl implements ReaderRequestService {

    private final UserService        userService;
    private final BookRequestDAO     requestDAO;
    private final BookCopyService    copyService;
    private final BookService        bookService;

    @Override
    public void createRequest(Long userId, Long bookId, RequestType type) {
        User user = userService.findById(userId);
        bookService.findById(bookId);
        BookCopy copy = copyService.findFirstAvailableByBookId(bookId);
        copyService.changeStatus(copy.getId(), CopyStatus.RESERVED);

        BookRequest req = BookRequest.builder()
                .user(user)
                .copy(copy)
                .type(type)
                .status(RequestStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .build();
        requestDAO.save(req);
    }


    @Override
    @Transactional(readOnly = true)
    public List<BookRequest> findByUser(Long userId) {
        return requestDAO.findAll().stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .map(r -> {
                    BookCopy copy = copyService.findById(r.getCopy().getId());
                    Book book = bookService.findById(copy.getBook().getId());
                    copy.setBook(book);
                    r.setCopy(copy);
                    r.setUser(userService.findById(userId));
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void cancelRequest(Long requestId, Long userId) {
        BookRequest r = requestDAO.findById(requestId)
                .orElseThrow(() -> new BookRequestNotFoundException("Request not found, id=" + requestId));
        if (!r.getUser().getId().equals(userId) ||
                r.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel this request");
        }
        r.setStatus(RequestStatus.CANCELED);
        r.setReturnDate(LocalDateTime.now());
        requestDAO.update(r);

        copyService.changeStatus(r.getCopy().getId(), CopyStatus.AVAILABLE);
    }
}
