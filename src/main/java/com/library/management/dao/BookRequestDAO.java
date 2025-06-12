package com.library.management.dao;

import com.library.management.model.BookRequest;
import com.library.management.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface BookRequestDAO {
    List<BookRequest> findAll();
    Optional<BookRequest> findById(Long id);
    Optional<BookRequest> findByCopyAndStatus(Long copyId, RequestStatus status);
    void save(BookRequest request);
    void update(BookRequest request);
}