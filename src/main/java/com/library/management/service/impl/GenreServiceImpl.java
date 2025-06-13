package com.library.management.service.impl;

import com.library.management.dao.BookDAO;
import com.library.management.dao.GenreDAO;
import com.library.management.exception.EntityInUseException;
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
    private final BookDAO bookDAO;

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
    public void deleteById(Long id) {
        Genre genre = genreDAO.findById(id)
                .orElseThrow(() -> new EntityInUseException("Genre not found with id: " + id));
        long inUse = bookDAO.countByGenreId(genre.getId());
        if (inUse > 0) {
            throw new EntityInUseException(
                    String.format("Cannot delete genre “%s”: it’s still used by %d book(s).",
                            genre.getName(), inUse)
            );
        }
        genreDAO.delete(genre);
    }
}
