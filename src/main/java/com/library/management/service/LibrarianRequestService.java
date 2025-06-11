package com.library.management.service;

import com.library.management.model.BookRequest;
import com.library.management.model.RequestStatus;

import java.util.List;

public interface LibrarianRequestService {
    List<BookRequest> findAll();
    void updateStatus(Long requestId, RequestStatus newStatus);
}