package com.library.management.dao;

import com.library.management.model.BookRequest;
import java.util.List;
import java.util.Optional;

public interface BookRequestDAO {
    List<BookRequest> findAll();
    Optional<BookRequest> findById(Long id);
    void save(BookRequest request);
    void update(BookRequest request);
}