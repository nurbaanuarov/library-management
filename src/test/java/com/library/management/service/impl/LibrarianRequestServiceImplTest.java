package com.library.management.service.impl;

import com.library.management.dao.BookCopyDAO;
import com.library.management.dao.BookDAO;
import com.library.management.dao.BookRequestDAO;
import com.library.management.dao.UserDAO;
import com.library.management.exception.BookCopyNotFoundException;
import com.library.management.exception.BookNotFoundException;
import com.library.management.exception.BookRequestNotFoundException;
import com.library.management.exception.UserNotFoundException;
import com.library.management.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LibrarianRequestServiceImplTest {

    @Mock BookRequestDAO reqDao;
    @Mock UserDAO userDao;
    @Mock BookCopyDAO copyDao;
    @Mock BookDAO bookDao;
    @InjectMocks LibrarianRequestServiceImpl service;

    @BeforeEach void init() { MockitoAnnotations.openMocks(this); }

    @Test
    void findAll_mapsNestedEntities() {
        // prepare a raw request with only IDs
        BookRequest raw = BookRequest.builder()
                .id(5L)
                .user(User.builder().id(11L).build())
                .copy(BookCopy.builder()
                        .id(22L)
                        .book(Book.builder().id(33L).build())
                        .build())
                .type(RequestType.HOME)
                .status(RequestStatus.PENDING)
                .build();
        when(reqDao.findAll()).thenReturn(List.of(raw));
        when(userDao.findById(11L)).thenReturn(Optional.of(
                User.builder().id(11L).username("u").build()));
        when(copyDao.findById(22L)).thenReturn(Optional.of(
                BookCopy.builder().id(22L)
                        .book(Book.builder().id(33L).build())
                        .inventoryNumber("X")
                        .status(CopyStatus.AVAILABLE)
                        .build()));
        when(bookDao.findById(33L)).thenReturn(Optional.of(
                Book.builder().id(33L).title("T").build()));

        List<BookRequest> out = service.findAll();
        assertEquals(1, out.size());
        BookRequest mapped = out.get(0);
        assertEquals("u", mapped.getUser().getUsername());
        assertEquals("T", mapped.getCopy().getBook().getTitle());
    }

    @Test
    void updateStatus_notFound_throws() {
        when(reqDao.findById(7L)).thenReturn(Optional.empty());
        assertThrows(BookRequestNotFoundException.class,
                () -> service.updateStatus(7L, RequestStatus.ISSUED));
    }

    @Test
    void updateStatus_toIssued_updatesCopyAndRequest() {
        BookCopy copy = BookCopy.builder().id(99L).build();
        BookRequest r = BookRequest.builder()
                .id(1L).copy(copy).status(RequestStatus.PENDING).build();
        when(reqDao.findById(1L)).thenReturn(Optional.of(r));

        service.updateStatus(1L, RequestStatus.ISSUED);

        verify(copyDao).updateStatus(99L, CopyStatus.ISSUED);
        assertNotNull(r.getIssueDate());
        assertEquals(RequestStatus.ISSUED, r.getStatus());
        verify(reqDao).update(r);
    }

    @Test
    void updateStatus_toReturned_updatesCopyAndRequest() {
        BookCopy copy = BookCopy.builder().id(55L).build();
        BookRequest r = BookRequest.builder()
                .id(2L).copy(copy).status(RequestStatus.ISSUED).build();
        when(reqDao.findById(2L)).thenReturn(Optional.of(r));

        service.updateStatus(2L, RequestStatus.RETURNED);

        verify(copyDao).updateStatus(55L, CopyStatus.AVAILABLE);
        assertNotNull(r.getReturnDate());
        assertEquals(RequestStatus.RETURNED, r.getStatus());
        verify(reqDao).update(r);
    }

    @Test
    void updateStatus_toCanceled_updatesCopyAndRequest() {
        BookCopy copy = BookCopy.builder().id(77L).build();
        BookRequest r = BookRequest.builder()
                .id(3L).copy(copy).status(RequestStatus.PENDING).build();
        when(reqDao.findById(3L)).thenReturn(Optional.of(r));

        service.updateStatus(3L, RequestStatus.CANCELED);

        verify(copyDao).updateStatus(77L, CopyStatus.AVAILABLE);
        assertNotNull(r.getReturnDate());
        assertEquals(RequestStatus.CANCELED, r.getStatus());
        verify(reqDao).update(r);
    }

    @Test
    void mapping_userNotFound_throws() {
        BookRequest raw = BookRequest.builder()
                .id(5L)
                .user(User.builder().id(666L).build())
                .copy(BookCopy.builder().id(1L).build())
                .build();
        when(reqDao.findAll()).thenReturn(List.of(raw));
        when(userDao.findById(666L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.findAll());
    }

    @Test
    void mapping_copyNotFound_throws() {
        BookRequest raw = BookRequest.builder()
                .id(5L)
                .user(User.builder().id(1L).build())
                .copy(BookCopy.builder().id(666L).build())
                .build();
        when(reqDao.findAll()).thenReturn(List.of(raw));
        when(userDao.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(copyDao.findById(666L)).thenReturn(Optional.empty());

        assertThrows(BookCopyNotFoundException.class, () -> service.findAll());
    }

    @Test
    void mapping_bookNotFound_throws() {
        BookRequest raw = BookRequest.builder()
                .id(5L)
                .user(User.builder().id(1L).build())
                .copy(BookCopy.builder().id(2L).book(Book.builder().id(999L).build()).build())
                .build();
        when(reqDao.findAll()).thenReturn(List.of(raw));
        when(userDao.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(copyDao.findById(2L)).thenReturn(Optional.of(BookCopy.builder().id(2L).book(Book.builder().id(999L).build()).build()));
        when(bookDao.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> service.findAll());
    }
}
