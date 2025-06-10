package com.library.management.service.impl;

import com.library.management.dao.GenreDAO;
import com.library.management.model.Genre;
import com.library.management.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreDAO genreDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Genre> findAll() {
        return genreDAO.findAll();
    }

    @Override
    public void save(Genre genre) {
        genreDAO.save(genre);
    }

    @Override
    public void delete(Genre genre) {
        genreDAO.delete(genre);
    }
}
