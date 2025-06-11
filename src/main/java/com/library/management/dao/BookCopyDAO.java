package com.library.management.dao;

import com.library.management.model.BookCopy;
import com.library.management.model.CopyStatus;

import java.util.List;
import java.util.Optional;

public interface BookCopyDAO {
    List<BookCopy> findAll();
    List<BookCopy> findByBookId(Long bookId);
    Optional<BookCopy> findById(Long id);
    void save(BookCopy copy);
    void updateStatus(Long copyId, CopyStatus newStatus);
    void update(BookCopy copy);
}
