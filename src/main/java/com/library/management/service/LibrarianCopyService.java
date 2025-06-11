package com.library.management.service;

import com.library.management.model.BookCopy;
import com.library.management.model.CopyStatus;

import java.util.List;

public interface LibrarianCopyService {
    List<BookCopy> findAll();
    List<BookCopy> findByBookId(Long bookId);
    BookCopy findById(Long id);
    void create(BookCopy copy);
    void changeStatus(Long copyId, CopyStatus newStatus);
    void update(BookCopy copy);
}
