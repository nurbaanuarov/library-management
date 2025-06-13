package com.library.management.service.impl;

import com.library.management.dao.BookCopyDAO;
import com.library.management.exception.BookCopyNotFoundException;
import com.library.management.model.BookCopy;
import com.library.management.model.CopyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookCopyServiceImplTest {
    private BookCopyDAO dao;
    private BookCopyServiceImpl service;

    @BeforeEach
    void setUp() {
        dao     = mock(BookCopyDAO.class);
        service = new BookCopyServiceImpl(dao);
    }

    @Test
    void findAll_delegates() {
        List<BookCopy> dummy = asList(new BookCopy(), new BookCopy());
        when(dao.findAll()).thenReturn(dummy);

        assertSame(dummy, service.findAll());
        verify(dao).findAll();
    }

    @Test
    void findByBookId_delegates() {
        List<BookCopy> dummy = asList(new BookCopy());
        when(dao.findByBookId(123L)).thenReturn(dummy);

        assertSame(dummy, service.findByBookId(123L));
        verify(dao).findByBookId(123L);
    }

    @Test
    void findById_found() {
        BookCopy c = new BookCopy();
        when(dao.findById(5L)).thenReturn(Optional.of(c));
        assertSame(c, service.findById(5L));
    }

    @Test
    void findById_notFound_throws() {
        when(dao.findById(6L)).thenReturn(Optional.empty());
        assertThrows(BookCopyNotFoundException.class, () -> service.findById(6L));
    }

    @Test
    void findFirstAvailable_found() {
        BookCopy c = new BookCopy();
        when(dao.findFirstAvailableByBookId(7L)).thenReturn(Optional.of(c));
        assertSame(c, service.findFirstAvailableByBookId(7L));
    }

    @Test
    void findFirstAvailable_notFound_throws() {
        when(dao.findFirstAvailableByBookId(8L)).thenReturn(Optional.empty());
        assertThrows(BookCopyNotFoundException.class,
                () -> service.findFirstAvailableByBookId(8L));
    }

    @Test
    void create_delegates() {
        BookCopy c = new BookCopy();
        service.create(c);
        verify(dao).save(c);
    }

    @Test
    void changeStatus_delegates() {
        service.changeStatus(11L, CopyStatus.RESERVED);
        verify(dao).updateStatus(11L, CopyStatus.RESERVED);
    }

    @Test
    void update_delegates() {
        BookCopy c = new BookCopy();
        service.update(c);
        verify(dao).update(c);
    }
}
