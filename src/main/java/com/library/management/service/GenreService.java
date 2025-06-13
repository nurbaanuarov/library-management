package com.library.management.service;

import com.library.management.model.Genre;

import java.util.List;

public interface GenreService {
    List<Genre> findAll();
    void save(Genre genre);
    void deleteById(Long id);
}
