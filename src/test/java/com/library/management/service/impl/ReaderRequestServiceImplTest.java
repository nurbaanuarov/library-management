package com.library.management.service.impl;

import com.library.management.dao.BookRequestDAO;
import com.library.management.exception.BookRequestNotFoundException;
import com.library.management.model.*;
import com.library.management.service.BookCopyService;
import com.library.management.service.BookService;
import com.library.management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReaderRequestServiceImplTest {

    @Mock UserService userService;
    @Mock BookService bookService;
    @Mock BookCopyService copyService;
    @Mock BookRequestDAO reqDao;

    @InjectMocks ReaderRequestServiceImpl service;

    @BeforeEach void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createRequest_happyPath() {
        // given
        Long uid = 7L, bid = 13L, cid = 99L;
        User user = User.builder().id(uid).username("u").build();
        Book book = Book.builder().id(bid).title("T").build();
        BookCopy copy = BookCopy.builder().id(cid).book(book).status(CopyStatus.AVAILABLE).build();

        when(userService.findById(uid)).thenReturn(user);
        when(bookService.findById(bid)).thenReturn(book);
        when(copyService.findFirstAvailableByBookId(bid)).thenReturn(copy);

        // when
        service.createRequest(uid, bid, RequestType.HOME);

        // then
        verify(copyService).changeStatus(cid, CopyStatus.RESERVED);
        ArgumentCaptor<BookRequest> cap = ArgumentCaptor.forClass(BookRequest.class);
        verify(reqDao).save(cap.capture());
        BookRequest saved = cap.getValue();

        assertEquals(uid, saved.getUser().getId());
        assertEquals(cid, saved.getCopy().getId());
        assertEquals(RequestStatus.PENDING, saved.getStatus());
        assertEquals(RequestType.HOME, saved.getType());
        assertNotNull(saved.getRequestDate());
    }

    @Test
    void createRequest_noCopyAvailable_throws() {
        Long uid = 1L, bid = 2L;
        when(userService.findById(uid)).thenReturn(User.builder().id(uid).build());
        when(bookService.findById(bid)).thenReturn(Book.builder().id(bid).build());
        when(copyService.findFirstAvailableByBookId(bid))
                .thenThrow(new RuntimeException("none"));

        assertThrows(RuntimeException.class,
                () -> service.createRequest(uid, bid, RequestType.HOME));
    }

    @Test
    void findByUser_filtersAndEnriches() {
        Long uid = 5L, cid = 11L, bid = 22L;
        // incoming raw request pointing only to IDs
        BookRequest raw = BookRequest.builder()
                .id(42L)
                .user(User.builder().id(uid).build())
                .copy(BookCopy.builder().id(cid).build())
                .type(RequestType.HOME)
                .status(RequestStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .build();

        BookCopy fetchedCopy = BookCopy.builder()
                .id(cid).book(Book.builder().id(bid).build())
                .inventoryNumber("X").status(CopyStatus.RESERVED).build();
        Book fullBook = Book.builder().id(bid).title("Full").build();
        User fullUser = User.builder().id(uid).username("usr").build();

        when(reqDao.findAll()).thenReturn(List.of(raw));
        when(copyService.findById(cid)).thenReturn(fetchedCopy);
        when(bookService.findById(bid)).thenReturn(fullBook);
        when(userService.findById(uid)).thenReturn(fullUser);

        List<BookRequest> out = service.findByUser(uid);
        assertEquals(1, out.size());
        BookRequest enriched = out.get(0);
        assertEquals("Full", enriched.getCopy().getBook().getTitle());
        assertEquals("usr", enriched.getUser().getUsername());
    }

    @Test
    void cancelRequest_notFound_throws() {
        when(reqDao.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BookRequestNotFoundException.class,
                () -> service.cancelRequest(99L, 1L));
    }

    @Test
    void cancelRequest_wrongUserOrStatus_throws() {
        Long rid = 10L, uid = 20L;
        BookRequest r = BookRequest.builder()
                .id(rid)
                .user(User.builder().id(999L).build())
                .status(RequestStatus.PENDING)
                .copy(BookCopy.builder().id(1L).build())
                .build();
        when(reqDao.findById(rid)).thenReturn(Optional.of(r));

        // wrong user id
        assertThrows(IllegalStateException.class,
                () -> service.cancelRequest(rid, uid));

        // correct user but wrong status
        r.setUser(User.builder().id(uid).build());
        r.setStatus(RequestStatus.ISSUED);
        assertThrows(IllegalStateException.class,
                () -> service.cancelRequest(rid, uid));
    }

    @Test
    void cancelRequest_happyPath() {
        Long rid = 7L, uid = 8L, cid = 33L;
        BookRequest r = BookRequest.builder()
                .id(rid)
                .user(User.builder().id(uid).build())
                .status(RequestStatus.PENDING)
                .copy(BookCopy.builder().id(cid).build())
                .build();

        when(reqDao.findById(rid)).thenReturn(Optional.of(r));

        service.cancelRequest(rid, uid);

        assertEquals(RequestStatus.CANCELED, r.getStatus());
        assertNotNull(r.getReturnDate());
        verify(reqDao).update(r);
        verify(copyService).changeStatus(cid, CopyStatus.AVAILABLE);
    }
}
