package com.library.management.dao;

import com.library.management.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreDAO {
    List<Genre> findAll();
    Optional<Genre> findById(Long id);
    Optional<Genre> findByName(String name);
    void save(Genre genre);
    void delete(Genre genre);
}
