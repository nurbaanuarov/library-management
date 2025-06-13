package com.library.management.dao;

import com.library.management.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookDAO {
    List<Book> findAll();
    Optional<Book> findById(Long id);
    long countByAuthorId(Long authorId);
    long countByGenreId(Long genreId);
    void save(Book book);
    void deleteById(Long id);
}
