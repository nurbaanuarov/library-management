package com.library.management.service;

import com.library.management.model.Author;

import java.util.List;

public interface AuthorService {
    List<Author> findAll();
    void save(Author author);
    void deleteById(Long id);
}
