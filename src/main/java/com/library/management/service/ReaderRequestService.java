package com.library.management.service;

import com.library.management.model.BookRequest;
import com.library.management.model.RequestType;

import java.util.List;

public interface ReaderRequestService {
    void createRequest(Long userId, Long bookId, RequestType type);
    List<BookRequest> findByUser(Long userId);
    void cancelRequest(Long requestId, Long userId);
}
