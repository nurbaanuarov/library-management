package com.library.management.dao.impl;

import com.library.management.exception.DataAccessException;
import com.library.management.model.Book;
import com.library.management.model.BookCopy;
import com.library.management.model.CopyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookCopyDAOImplTest {

    private DataSource ds;
    private Connection con;
    private PreparedStatement ps;
    private ResultSet rs;
    private BookCopyDAOImpl dao;

    @BeforeEach
    void setUp() throws Exception {
        ds  = mock(DataSource.class);
        con = mock(Connection.class);
        ps  = mock(PreparedStatement.class);
        rs  = mock(ResultSet.class);

        when(ds.getConnection()).thenReturn(con);
        dao = new BookCopyDAOImpl(ds);
    }

    @Test
    void findAll_returnsCopies() throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        // two rows then end
        when(rs.next()).thenReturn(true, true, false);

        // row1
        when(rs.getLong("copy_id")).thenReturn(1L, 2L);
        when(rs.getString("inventory_number"))
                .thenReturn("C-1").thenReturn("C-2");
        when(rs.getString("status"))
                .thenReturn("AVAILABLE").thenReturn("ISSUED");
        when(rs.getLong("book_id")).thenReturn(10L, 10L);
        when(rs.getString("book_title"))
                .thenReturn("B1").thenReturn("B1");
        when(rs.getString("author_first"))
                .thenReturn("A").thenReturn("A");
        when(rs.getString("author_last"))
                .thenReturn("Z").thenReturn("Z");

        List<BookCopy> list = dao.findAll();
        assertEquals(2, list.size());

        BookCopy first = list.get(0);
        assertEquals(1L, first.getId());
        assertEquals("C-1", first.getInventoryNumber());
        assertEquals(CopyStatus.AVAILABLE, first.getStatus());
        assertEquals(10L, first.getBook().getId());
        assertEquals("A", first.getBook().getAuthor().getFirstName());
    }

    @Test
    void findByBookId_filtersByBook() throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        when(rs.getLong("copy_id")).thenReturn(5L);
        when(rs.getString("inventory_number")).thenReturn("X");
        when(rs.getString("status")).thenReturn("AVAILABLE");
        when(rs.getLong("book_id")).thenReturn(20L);
        when(rs.getString("book_title")).thenReturn("T");
        when(rs.getString("author_first")).thenReturn("F");
        when(rs.getString("author_last")).thenReturn("L");

        List<BookCopy> copies = dao.findByBookId(20L);
        assertEquals(1, copies.size());
        assertEquals(20L, copies.get(0).getBook().getId());
        verify(ps).setLong(1,20L);
    }

    @Test
    void findById_found() throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getLong("copy_id")).thenReturn(33L);
        when(rs.getString("inventory_number")).thenReturn("INV");
        when(rs.getString("status")).thenReturn("ISSUED");
        when(rs.getLong("book_id")).thenReturn(3L);
        when(rs.getString("book_title")).thenReturn("T3");
        when(rs.getString("author_first")).thenReturn("X");
        when(rs.getString("author_last")).thenReturn("Y");

        Optional<BookCopy> opt = dao.findById(33L);
        assertTrue(opt.isPresent());
        assertEquals(33L, opt.get().getId());
        verify(ps).setLong(1,33L);
    }

    @Test
    void findById_notFound() throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertTrue(dao.findById(99L).isEmpty());
    }

    @Test
    void findFirstAvailableByBookId_returnsAvailable() throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getLong("copy_id")).thenReturn(7L);
        when(rs.getString("inventory_number")).thenReturn("I7");
        when(rs.getString("status")).thenReturn("AVAILABLE");
        when(rs.getLong("book_id")).thenReturn(2L);
        when(rs.getString("book_title")).thenReturn("BK");
        when(rs.getString("author_first")).thenReturn("F");
        when(rs.getString("author_last")).thenReturn("L");

        Optional<BookCopy> opt = dao.findFirstAvailableByBookId(2L);
        assertTrue(opt.isPresent());
        assertEquals(CopyStatus.AVAILABLE, opt.get().getStatus());
        verify(ps).setLong(1,2L);
        verify(ps).setString(2,"AVAILABLE");
    }

    @Test
    void save_setsId() throws Exception {
        when(ds.getConnection()).thenReturn(con);
        when(con.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        ResultSet keys = mock(ResultSet.class);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(77L);

        BookCopy bc = BookCopy.builder()
                .book(Book.builder().id(2L).build())
                .inventoryNumber("XX")
                .status(CopyStatus.AVAILABLE)
                .build();

        dao.save(bc);
        assertEquals(77L, bc.getId());
    }

    @Test
    void updateStatus_success() throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> dao.updateStatus(8L, CopyStatus.ISSUED));
        verify(ps).setString(1,"ISSUED");
        verify(ps).setLong(2,8L);
    }

    @Test
    void updateStatus_noRow_throws() throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        assertThrows(DataAccessException.class, () -> dao.updateStatus(8L, CopyStatus.ISSUED));
    }

    @Test
    void update_changesBoth() throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        BookCopy bc = BookCopy.builder()
                .id(9L)
                .inventoryNumber("OLD")
                .status(CopyStatus.AVAILABLE)
                .build();

        bc.setInventoryNumber("NEW");
        bc.setStatus(CopyStatus.ISSUED);
        assertDoesNotThrow(() -> dao.update(bc));
        verify(ps).setString(1,"NEW");
        verify(ps).setString(2,"ISSUED");
        verify(ps).setLong(3,9L);
    }
}
