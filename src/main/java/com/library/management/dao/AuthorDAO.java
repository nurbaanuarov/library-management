package com.library.management.dao;

import com.library.management.model.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorDAO {
    List<Author> findAll();
    Optional<Author> findById(Long id);
    Optional<Author> findByFirstNameAndLastName(String firstName, String lastName);
    void save(Author author);
    void delete(Author author);
}
