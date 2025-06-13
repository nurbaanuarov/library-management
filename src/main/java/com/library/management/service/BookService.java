package com.library.management.service;

import com.library.management.model.Book;
import com.library.management.web.Page;

import java.util.List;

public interface BookService {
    List<Book> findAll();
    Book      findById(Long id);
    boolean   hasAvailableCopies(Long bookId);
    void      save(Book book);
    void      deleteById(Long id);
    Page<Book> findPaginated(int page, int size);
}
