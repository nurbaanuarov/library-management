package com.library.management.dao.impl;

import com.library.management.exception.DataAccessException;
import com.library.management.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookRequestDAOImplTest {

    private Connection con;
    private PreparedStatement ps;
    private ResultSet rs;
    private BookRequestDAOImpl dao;

    @BeforeEach
    void setUp() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        con        = mock(Connection.class);
        ps         = mock(PreparedStatement.class);
        rs         = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(con);

        dao = new BookRequestDAOImpl(dataSource);
    }

    @Test
    void findAll_returnsList() throws Exception {
        when(con.prepareStatement(startsWith("SELECT id, user_id"))).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getLong("user_id")).thenReturn(10L, 20L);
        when(rs.getLong("copy_id")).thenReturn(100L, 200L);
        when(rs.getString("type"))
                .thenReturn(RequestType.HOME.name(), RequestType.IN_LIBRARY.name());
        when(rs.getString("status"))
                .thenReturn(RequestStatus.PENDING.name(), RequestStatus.ISSUED.name());
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        when(rs.getTimestamp("request_date"))
                .thenReturn(now, now);
        when(rs.getTimestamp("issue_date"))
                .thenReturn(now);
        when(rs.getTimestamp("return_date"))
                .thenReturn(null);

        List<BookRequest> list = dao.findAll();
        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals(RequestStatus.PENDING, list.get(0).getStatus());
        assertEquals(2L, list.get(1).getId());
        assertEquals(RequestStatus.ISSUED, list.get(1).getStatus());

        verify(ps).executeQuery();
    }


    @Test
    void findById_found() throws Exception {
        when(con.prepareStatement(startsWith("SELECT id, user_id"))).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong("id")).thenReturn(42L);
        when(rs.getLong("user_id")).thenReturn(7L);
        when(rs.getLong("copy_id")).thenReturn(8L);
        when(rs.getString("type")).thenReturn(RequestType.HOME.name());
        when(rs.getString("status")).thenReturn(RequestStatus.PENDING.name());
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        when(rs.getTimestamp("request_date")).thenReturn(now);
        when(rs.getTimestamp("issue_date")).thenReturn(null);
        when(rs.getTimestamp("return_date")).thenReturn(null);

        Optional<BookRequest> opt = dao.findById(42L);
        assertTrue(opt.isPresent());
        BookRequest req = opt.get();
        assertEquals(42L, req.getId());
        assertEquals(7L, req.getUser().getId());
    }

    @Test
    void findById_notFound() throws Exception {
        when(con.prepareStatement(contains("WHERE id"))).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        Optional<BookRequest> opt = dao.findById(99L);
        assertFalse(opt.isPresent());
    }

    @Test
    void findByCopyAndStatus_found() throws Exception {
        when(con.prepareStatement(startsWith("SELECT id, user_id"))).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong("id")).thenReturn(55L);
        when(rs.getLong("user_id")).thenReturn(11L);
        when(rs.getLong("copy_id")).thenReturn(22L);
        when(rs.getString("type")).thenReturn(RequestType.IN_LIBRARY.name());
        when(rs.getString("status")).thenReturn(RequestStatus.ISSUED.name());
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        when(rs.getTimestamp("request_date")).thenReturn(now);
        when(rs.getTimestamp("issue_date")).thenReturn(now);
        when(rs.getTimestamp("return_date")).thenReturn(null);

        Optional<BookRequest> opt = dao.findByCopyAndStatus(22L, RequestStatus.ISSUED);
        assertTrue(opt.isPresent());
        assertEquals(55L, opt.get().getId());

        verify(ps).setLong(1, 22L);
        verify(ps).setString(2, RequestStatus.ISSUED.name());
    }

    @Test
    void findByCopyAndStatus_notFound() throws Exception {
        when(con.prepareStatement(contains("WHERE copy_id"))).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        Optional<BookRequest> opt = dao.findByCopyAndStatus(33L, RequestStatus.PENDING);
        assertFalse(opt.isPresent());
    }

    @Test
    void save_success() throws Exception {
        when(con.prepareStatement(startsWith("INSERT"), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        BookRequest req = BookRequest.builder()
                .user(User.builder().id(5L).build())
                .copy(BookCopy.builder().id(6L).build())
                .type(RequestType.HOME)
                .status(RequestStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .build();

        assertDoesNotThrow(() -> dao.save(req));
        verify(ps).setLong(1, 5L);
        verify(ps).setLong(2, 6L);
    }

    @Test
    void save_noRowsAffected() throws Exception {
        when(con.prepareStatement(anyString(), anyInt())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        BookRequest req = BookRequest.builder()
                .user(User.builder().id(1L).build())
                .copy(BookCopy.builder().id(2L).build())
                .type(RequestType.HOME)
                .status(RequestStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .build();

        assertThrows(DataAccessException.class, () -> dao.save(req));
    }

    @Test
    void update_success() throws Exception {
        when(con.prepareStatement(startsWith("UPDATE book_requests"))).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        BookRequest req = BookRequest.builder()
                .id(99L)
                .status(RequestStatus.CANCELED)
                .issueDate(null)
                .returnDate(LocalDateTime.now())
                .build();

        assertDoesNotThrow(() -> dao.update(req));
        verify(ps).setString(1, RequestStatus.CANCELED.name());
        verify(ps).setLong(4, 99L);
    }

    @Test
    void update_noRowsAffected() throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        BookRequest req = BookRequest.builder().id(100L).status(RequestStatus.CANCELED).build();

        assertThrows(DataAccessException.class, () -> dao.update(req));
    }
}
