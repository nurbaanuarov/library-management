package com.library.management.service.impl;

import com.library.management.dao.AuthorDAO;
import com.library.management.dao.BookDAO;
import com.library.management.exception.AuthorNotFoundException;
import com.library.management.exception.EntityInUseException;
import com.library.management.model.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorServiceImplTest {
    private AuthorDAO authorDAO;
    private BookDAO bookDAO;
    private AuthorServiceImpl service;

    @BeforeEach
    void setUp() {
        authorDAO = mock(AuthorDAO.class);
        bookDAO   = mock(BookDAO.class);
        service   = new AuthorServiceImpl(authorDAO, bookDAO);
    }

    @Test
    void findAll_delegates() {
        List<Author> dummy = asList(new Author(), new Author());
        when(authorDAO.findAll()).thenReturn(dummy);

        List<Author> result = service.findAll();
        assertSame(dummy, result);
        verify(authorDAO).findAll();
    }

    @Test
    void save_delegates() {
        Author a = new Author();
        service.save(a);
        verify(authorDAO).save(a);
    }

    @Test
    void deleteById_notFound() {
        when(authorDAO.findById(42L)).thenReturn(Optional.empty());
        assertThrows(AuthorNotFoundException.class, () -> service.deleteById(42L));
    }

    @Test
    void deleteById_inUse_throwsEntityInUse() {
        Author a = new Author(); a.setId(7L); a.setFirstName("Jon"); a.setLastName("Doe");
        when(authorDAO.findById(7L)).thenReturn(Optional.of(a));
        when(bookDAO.countByAuthorId(7L)).thenReturn(5L);

        EntityInUseException ex = assertThrows(EntityInUseException.class, () -> service.deleteById(7L));
        assertTrue(ex.getMessage().contains("still referenced by 5 book(s)"));
    }

    @Test
    void deleteById_notInUse_deletes() {
        Author a = new Author(); a.setId(9L); a.setFirstName("A"); a.setLastName("B");
        when(authorDAO.findById(9L)).thenReturn(Optional.of(a));
        when(bookDAO.countByAuthorId(9L)).thenReturn(0L);

        service.deleteById(9L);

        verify(authorDAO).delete(a);
    }
}
