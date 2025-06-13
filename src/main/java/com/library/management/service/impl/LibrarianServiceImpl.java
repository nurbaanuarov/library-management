package com.library.management.service.impl;

import com.library.management.dao.BookCopyDAO;
import com.library.management.dao.BookRequestDAO;
import com.library.management.dao.UserDAO;
import com.library.management.exception.BookCopyNotFoundException;
import com.library.management.exception.BookRequestNotFoundException;
import com.library.management.exception.UserNotFoundException;
import com.library.management.model.*;
import com.library.management.service.LibrarianService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class LibrarianServiceImpl implements LibrarianService {
    private final BookCopyDAO copyDao;
    private final BookRequestDAO requestDao;
    private final UserDAO userDao;

    @Override
    public void issueInLibrary(Long userId, Long copyId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id=" + userId));

        BookCopy copy = copyDao.findById(copyId)
                .orElseThrow(() -> new BookCopyNotFoundException("Book copy not found with id=" + copyId));

        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new IllegalStateException("Copy not available for in-library issue");
        }

        copyDao.updateStatus(copyId, CopyStatus.ISSUED);
        BookRequest req = BookRequest.builder()
                .user(user)
                .copy(copy)
                .type(RequestType.IN_LIBRARY)
                .status(RequestStatus.ISSUED)
                .requestDate(LocalDateTime.now())
                .issueDate(LocalDateTime.now())
                .build();
        requestDao.save(req);
    }

    @Override
    public void giveReserved(Long copyId) {
        BookRequest req = requestDao
                .findByCopyAndStatus(copyId, RequestStatus.PENDING)
                .orElseThrow(() -> new BookRequestNotFoundException("No PENDING reservation for copy id=" + copyId));

        if (req.getType() != RequestType.HOME) {
            throw new IllegalStateException("Request is not a HOME reservation");
        }

        User fullUser = userDao.findById(req.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id=" + req.getUser().getId()));

        BookCopy fullCopy = copyDao.findById(copyId)
                .orElseThrow(() -> new BookCopyNotFoundException("Book copy not found with id=" + copyId));

        copyDao.updateStatus(copyId, CopyStatus.ISSUED);

        req.setUser(fullUser);
        req.setCopy(fullCopy);
        req.setStatus(RequestStatus.ISSUED);
        req.setIssueDate(LocalDateTime.now());
        requestDao.update(req);
    }

    @Override
    public void cancelReservation(Long copyId) {
        BookRequest req = requestDao
                .findByCopyAndStatus(copyId, RequestStatus.PENDING)
                .orElseThrow(() -> new BookRequestNotFoundException("No PENDING reservation for copy id=" + copyId));

        if (req.getType() != RequestType.HOME) {
            throw new IllegalStateException("Request is not a HOME reservation");
        }

        req.setStatus(RequestStatus.CANCELED);
        requestDao.update(req);

        copyDao.updateStatus(copyId, CopyStatus.AVAILABLE);
    }

    @Override
    public void returnCopy(Long copyId) {
        BookRequest req = requestDao
                .findByCopyAndStatus(copyId, RequestStatus.ISSUED)
                .orElseThrow(() -> new BookRequestNotFoundException("No ISSUED request for copy id=" + copyId));

        req.setStatus(RequestStatus.RETURNED);
        req.setReturnDate(LocalDateTime.now());
        requestDao.update(req);

        copyDao.updateStatus(copyId, CopyStatus.AVAILABLE);
    }
}
