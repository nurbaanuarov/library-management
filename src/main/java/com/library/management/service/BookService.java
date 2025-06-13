package com.library.management.service;

import com.library.management.model.Book;
import com.library.management.web.Page;

import java.util.List;

public interface BookService {
    List<Book> findAll(int offset, int limit);
    Page<Book> findPaginated(int page, int size);
    Book findById(Long id);
    long count();
    boolean hasAvailableCopies(Long bookId);
    void save(Book book);
    void deleteById(Long id);
}
