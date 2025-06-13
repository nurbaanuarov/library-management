package com.library.management.service.impl;

import com.library.management.dao.BookCopyDAO;
import com.library.management.dao.BookRequestDAO;
import com.library.management.dao.UserDAO;
import com.library.management.exception.BookCopyNotFoundException;
import com.library.management.exception.BookRequestNotFoundException;
import com.library.management.exception.UserNotFoundException;
import com.library.management.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LibrarianServiceImplTest {

    @Mock BookCopyDAO copyDao;
    @Mock BookRequestDAO reqDao;
    @Mock UserDAO userDao;
    @InjectMocks LibrarianServiceImpl service;

    @BeforeEach void init() { MockitoAnnotations.openMocks(this); }

    @Test
    void issueInLibrary_userNotFound() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
                () -> service.issueInLibrary(1L, 2L));
    }

    @Test
    void issueInLibrary_copyNotFound() {
        when(userDao.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(copyDao.findById(2L)).thenReturn(Optional.empty());
        assertThrows(BookCopyNotFoundException.class,
                () -> service.issueInLibrary(1L, 2L));
    }

    @Test
    void issueInLibrary_notAvailable_throws() {
        when(userDao.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(copyDao.findById(2L)).thenReturn(Optional.of(BookCopy.builder()
                .id(2L).status(CopyStatus.ISSUED).build()));
        assertThrows(IllegalStateException.class,
                () -> service.issueInLibrary(1L, 2L));
    }

    @Test
    void issueInLibrary_happyPath() {
        User u = User.builder().id(1L).build();
        BookCopy c = BookCopy.builder().id(2L).status(CopyStatus.AVAILABLE).build();
        when(userDao.findById(1L)).thenReturn(Optional.of(u));
        when(copyDao.findById(2L)).thenReturn(Optional.of(c));

        service.issueInLibrary(1L, 2L);

        verify(copyDao).updateStatus(2L, CopyStatus.ISSUED);
        ArgumentCaptor<BookRequest> cap = ArgumentCaptor.forClass(BookRequest.class);
        verify(reqDao).save(cap.capture());
        BookRequest created = cap.getValue();
        assertEquals(RequestType.IN_LIBRARY, created.getType());
        assertEquals(RequestStatus.ISSUED, created.getStatus());
        assertNotNull(created.getRequestDate());
        assertNotNull(created.getIssueDate());
    }

    @Test
    void giveReserved_notFound_throws() {
        when(reqDao.findByCopyAndStatus(3L, RequestStatus.PENDING))
                .thenReturn(Optional.empty());
        assertThrows(BookRequestNotFoundException.class,
                () -> service.giveReserved(3L));
    }

    @Test
    void giveReserved_wrongType_throws() {
        BookRequest r = BookRequest.builder().type(RequestType.IN_LIBRARY).status(RequestStatus.PENDING).user(User.builder().id(1L).build()).copy(BookCopy.builder().id(3L).build()).build();
        when(reqDao.findByCopyAndStatus(3L, RequestStatus.PENDING)).thenReturn(Optional.of(r));
        assertThrows(IllegalStateException.class, () -> service.giveReserved(3L));
    }

    @Test
    void giveReserved_happyPath() {
        BookRequest r0 = BookRequest.builder()
                .type(RequestType.HOME)
                .status(RequestStatus.PENDING)
                .user(User.builder().id(10L).build())
                .copy(BookCopy.builder().id(20L).book(Book.builder().id(30L).build()).build())
                .build();
        when(reqDao.findByCopyAndStatus(20L, RequestStatus.PENDING)).thenReturn(Optional.of(r0));
        when(userDao.findById(10L)).thenReturn(Optional.of(User.builder().id(10L).build()));
        when(copyDao.findById(20L)).thenReturn(Optional.of(BookCopy.builder().id(20L).build()));

        service.giveReserved(20L);

        verify(copyDao).updateStatus(20L, CopyStatus.ISSUED);
        assertEquals(RequestStatus.ISSUED, r0.getStatus());
        assertNotNull(r0.getIssueDate());
        verify(reqDao).update(r0);
    }

    @Test
    void cancelReservation_notFound() {
        when(reqDao.findByCopyAndStatus(4L, RequestStatus.PENDING)).thenReturn(Optional.empty());
        assertThrows(BookRequestNotFoundException.class,
                () -> service.cancelReservation(4L));
    }

    @Test
    void cancelReservation_wrongType() {
        BookRequest r = BookRequest.builder().type(RequestType.IN_LIBRARY).status(RequestStatus.PENDING).copy(BookCopy.builder().id(5L).build()).build();
        when(reqDao.findByCopyAndStatus(5L, RequestStatus.PENDING)).thenReturn(Optional.of(r));
        assertThrows(IllegalStateException.class,
                () -> service.cancelReservation(5L));
    }

    @Test
    void cancelReservation_happyPath() {
        BookRequest r = BookRequest.builder().type(RequestType.HOME).status(RequestStatus.PENDING).copy(BookCopy.builder().id(6L).build()).build();
        when(reqDao.findByCopyAndStatus(6L, RequestStatus.PENDING)).thenReturn(Optional.of(r));

        service.cancelReservation(6L);

        assertEquals(RequestStatus.CANCELED, r.getStatus());
        verify(copyDao).updateStatus(6L, CopyStatus.AVAILABLE);
        verify(reqDao).update(r);
    }

    @Test
    void returnCopy_notFound() {
        when(reqDao.findByCopyAndStatus(7L, RequestStatus.ISSUED)).thenReturn(Optional.empty());
        assertThrows(BookRequestNotFoundException.class,
                () -> service.returnCopy(7L));
    }

    @Test
    void returnCopy_happyPath() {
        BookRequest r = BookRequest.builder().status(RequestStatus.ISSUED).copy(BookCopy.builder().id(8L).build()).build();
        when(reqDao.findByCopyAndStatus(8L, RequestStatus.ISSUED)).thenReturn(Optional.of(r));

        service.returnCopy(8L);

        assertEquals(RequestStatus.RETURNED, r.getStatus());
        assertNotNull(r.getReturnDate());
        verify(copyDao).updateStatus(8L, CopyStatus.AVAILABLE);
        verify(reqDao).update(r);
    }
}
