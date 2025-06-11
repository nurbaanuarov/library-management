package com.library.management.service.impl;

import com.library.management.dao.BookRequestDAO;
import com.library.management.dao.BookDAO;
import com.library.management.dao.UserDAO;
import com.library.management.exception.BookNotFoundException;
import com.library.management.exception.BookRequestNotFoundException;
import com.library.management.exception.UserNotFoundException;
import com.library.management.model.Book;
import com.library.management.model.BookRequest;
import com.library.management.model.RequestStatus;
import com.library.management.model.RequestType;
import com.library.management.model.User;
import com.library.management.service.ReaderRequestService;
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

    private final BookRequestDAO requestDAO;
    private final UserDAO userDAO;
    private final BookDAO bookDAO;

    @Override
    public void createRequest(Long userId, Long bookId, RequestType type) {
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Book book = bookDAO.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found"));

        BookRequest req = BookRequest.builder()
                .user(user)
                .copy(null)
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
                .collect(Collectors.toList());
    }

    @Override
    public void cancelRequest(Long requestId, Long userId) {
        BookRequest r = requestDAO.findById(requestId)
                .orElseThrow(() -> new BookRequestNotFoundException("Request not found"));
        if (!r.getUser().getId().equals(userId) || r.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel this request");
        }
        r.setStatus(RequestStatus.CANCELED);
        r.setReturnDate(LocalDateTime.now());
        requestDAO.update(r);
    }
}
