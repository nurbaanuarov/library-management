package com.library.management.service.impl;

import com.library.management.dao.BookDAO;
import com.library.management.dao.GenreDAO;
import com.library.management.exception.EntityInUseException;
import com.library.management.exception.GenreNotFoundException;
import com.library.management.model.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenreServiceImplTest {

    @Mock GenreDAO genreDAO;
    @Mock BookDAO bookDAO;
    @InjectMocks GenreServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAll_delegatesToDao() {
        service.findAll();
        verify(genreDAO).findAll();
    }

    @Test
    void save_delegatesToDao() {
        Genre g = new Genre(); g.setName("X");
        service.save(g);
        verify(genreDAO).save(g);
    }

    @Test
    void deleteById_notFound_throws() {
        when(genreDAO.findById(5L)).thenReturn(Optional.empty());
        assertThrows(GenreNotFoundException.class, () -> service.deleteById(5L));
    }

    @Test
    void deleteById_inUse_throwsEntityInUse() {
        Genre g = new Genre(); g.setId(10L); g.setName("G");
        when(genreDAO.findById(10L)).thenReturn(Optional.of(g));
        when(bookDAO.countByGenreId(10L)).thenReturn(2L);

        EntityInUseException ex = assertThrows(
                EntityInUseException.class,
                () -> service.deleteById(10L)
        );
        assertTrue(ex.getMessage().contains("still used by 2 book(s)"));
    }

    @Test
    void deleteById_notInUse_deletes() {
        Genre g = new Genre(); g.setId(20L);
        when(genreDAO.findById(20L)).thenReturn(Optional.of(g));
        when(bookDAO.countByGenreId(20L)).thenReturn(0L);

        service.deleteById(20L);
        verify(genreDAO).delete(g);
    }
}
