package com.library.management.service.impl;

import com.library.management.dao.BookCopyDAO;
import com.library.management.dao.BookDAO;
import com.library.management.exception.BookNotFoundException;
import com.library.management.model.Book;
import com.library.management.model.BookCopy;
import com.library.management.model.CopyStatus;
import com.library.management.web.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.List;
import java.util.Optional;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceImplTest {

    @Mock BookDAO bookDAO;
    @Mock BookCopyDAO copyDAO;
    @InjectMocks BookServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAll_delegatesToDao() {
        List<Book> books = of(new Book(), new Book());
        when(bookDAO.findAll()).thenReturn(books);

        List<Book> result = service.findAll();
        assertSame(books, result);
        verify(bookDAO).findAll();
    }

    @Test
    void findPaginated_returnsCorrectPage() {
        Book b1 = new Book(); b1.setId(1L);
        Book b2 = new Book(); b2.setId(2L);
        when(bookDAO.findPaginated(0,2)).thenReturn(of(b1,b2));
        when(bookDAO.countAll()).thenReturn(5L);

        Page<Book> page = service.findPaginated(1, 2);
        assertEquals(1, page.getPageNumber());
        assertEquals(2, page.getPageSize());
        assertEquals(3, page.getTotalPages());
        assertEquals(5, page.getTotalElements());
        assertIterableEquals(of(b1,b2), page.getContent());

        verify(bookDAO).findPaginated(0,2);
        verify(bookDAO).countAll();
    }

    @Test
    void findById_found() {
        Book b = new Book(); b.setId(42L);
        when(bookDAO.findById(42L)).thenReturn(Optional.of(b));

        Book result = service.findById(42L);
        assertSame(b, result);
    }

    @Test
    void findById_notFound_throws() {
        when(bookDAO.findById(5L)).thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> service.findById(5L));
    }

    @Test
    void hasAvailableCopies_trueWhenAnyAvailable() {
        BookCopy c1 = new BookCopy(); c1.setStatus(CopyStatus.ISSUED);
        BookCopy c2 = new BookCopy(); c2.setStatus(CopyStatus.AVAILABLE);
        when(copyDAO.findByBookId(7L)).thenReturn(of(c1, c2));
        assertTrue(service.hasAvailableCopies(7L));
    }

    @Test
    void hasAvailableCopies_falseWhenNoneAvailable() {
        BookCopy c1 = new BookCopy(); c1.setStatus(CopyStatus.ISSUED);
        when(copyDAO.findByBookId(8L)).thenReturn(of(c1));
        assertFalse(service.hasAvailableCopies(8L));
    }

    @Test
    void save_delegatesToDao() {
        Book b = new Book();
        service.save(b);
        verify(bookDAO).save(b);
    }

    @Test
    void deleteById_delegatesToDao() {
        service.deleteById(99L);
        verify(bookDAO).deleteById(99L);
    }
}
