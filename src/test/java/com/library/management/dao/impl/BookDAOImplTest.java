package com.library.management.dao.impl;

import com.library.management.model.Author;
import com.library.management.model.Book;
import com.library.management.model.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookDAOImplTest {

    private DataSource ds;
    private Connection con;
    private PreparedStatement ps;
    private ResultSet rs;
    private BookDAOImpl dao;

    @BeforeEach
    void setUp() throws Exception {
        ds  = mock(DataSource.class);
        con = mock(Connection.class);
        ps  = mock(PreparedStatement.class);
        rs  = mock(ResultSet.class);

        when(ds.getConnection()).thenReturn(con);
        dao = new BookDAOImpl(ds);
    }

    @Test
    void findAll_returnsAllBooks() throws Exception {
        when(con.prepareStatement(startsWith("SELECT"))).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);

        // first row
        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getString("title")).thenReturn("A","B");
        when(rs.getString("description")).thenReturn("d1","d2");
        when(rs.getInt("total_copies")).thenReturn(3,4);
        when(rs.getLong("author_id")).thenReturn(10L, 11L);
        when(rs.getString("first_name")).thenReturn("F1","F2");
        when(rs.getString("last_name")).thenReturn("L1","L2");
        when(rs.getLong("genre_id")).thenReturn(20L,21L);
        when(rs.getString("genre_name")).thenReturn("G1","G2");

        List<Book> list = dao.findAll();
        assertEquals(2, list.size());
        assertEquals("A", list.get(0).getTitle());
        assertEquals(11L, list.get(1).getAuthor().getId());
    }

    @Test
    void findById_found() throws Exception {
        when(con.prepareStatement(startsWith("SELECT"))).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getLong("id")).thenReturn(99L);
        when(rs.getString("title")).thenReturn("T");
        when(rs.getString("description")).thenReturn("D");
        when(rs.getInt("total_copies")).thenReturn(5);
        when(rs.getLong("author_id")).thenReturn(7L);
        when(rs.getString("first_name")).thenReturn("X");
        when(rs.getString("last_name")).thenReturn("Y");
        when(rs.getLong("genre_id")).thenReturn(8L);
        when(rs.getString("genre_name")).thenReturn("Z");

        Optional<Book> b = dao.findById(99L);
        assertTrue(b.isPresent());
        assertEquals("T", b.get().getTitle());
        verify(ps).setLong(1,99L);
    }

    @Test
    void findById_notFound() throws Exception {
        when(con.prepareStatement(startsWith("SELECT"))).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertTrue(dao.findById(1L).isEmpty());
    }

    @Test
    void findPaginated_andCountAll() throws Exception {
        // test paged
        PreparedStatement pagedPs = mock(PreparedStatement.class);
        when(con.prepareStatement(contains("LIMIT"))).thenReturn(pagedPs);
        when(pagedPs.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        when(rs.getLong("id")).thenReturn(3L);
        when(rs.getString("title")).thenReturn("P");
        when(rs.getString("description")).thenReturn("Dp");
        when(rs.getInt("total_copies")).thenReturn(1);
        when(rs.getLong("author_id")).thenReturn(2L);
        when(rs.getString("first_name")).thenReturn("A");
        when(rs.getString("last_name")).thenReturn("B");
        when(rs.getLong("genre_id")).thenReturn(4L);
        when(rs.getString("genre_name")).thenReturn("G");

        // test count
        PreparedStatement countPs = mock(PreparedStatement.class);
        ResultSet countRs         = mock(ResultSet.class);
        when(con.prepareStatement(startsWith("SELECT count"))).thenReturn(countPs);
        when(countPs.executeQuery()).thenReturn(countRs);
        when(countRs.next()).thenReturn(true);
        when(countRs.getLong(1)).thenReturn(42L);

        List<Book> page = dao.findPaginated(5, 10);
        assertEquals(1, page.size());
        assertEquals(3L, page.get(0).getId());

        long total = dao.countAll();
        assertEquals(42L, total);

        verify(pagedPs).setInt(1,10);
        verify(pagedPs).setInt(2,5);
    }

    @Test
    void countByGenreAndAuthor() throws Exception {
        PreparedStatement byGenrePs = mock(PreparedStatement.class);
        PreparedStatement byAuthorPs = mock(PreparedStatement.class);
        ResultSet gRs = mock(ResultSet.class);
        ResultSet aRs = mock(ResultSet.class);

        when(con.prepareStatement(startsWith("SELECT count"))).thenReturn(byGenrePs, byAuthorPs);
        when(byGenrePs.executeQuery()).thenReturn(gRs);
        when(byAuthorPs.executeQuery()).thenReturn(aRs);
        when(gRs.next()).thenReturn(true);
        when(aRs.next()).thenReturn(true);
        when(gRs.getLong(1)).thenReturn(5L);
        when(aRs.getLong(1)).thenReturn(6L);

        assertEquals(5L, dao.countByGenreId(7L));
        assertEquals(6L, dao.countByAuthorId(8L));

        verify(byGenrePs).setLong(1,7L);
        verify(byAuthorPs).setLong(1,8L);
    }

    @Test
    void save_andDeleteById() throws Exception {
        // save
        PreparedStatement ins = mock(PreparedStatement.class);
        when(con.prepareStatement(startsWith("INSERT"), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(ins);
        when(ins.executeUpdate()).thenReturn(1);
        ResultSet keys = mock(ResultSet.class);
        when(ins.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(123L);

        Book b = Book.builder()
                .title("X").description("Y")
                .author(new Author(1L,null,null))
                .genre(new Genre(2L,null))
                .totalCopies(7)
                .build();

        dao.save(b);
        assertEquals(123L, b.getId());

        // delete
        PreparedStatement del = mock(PreparedStatement.class);
        when(con.prepareStatement(startsWith("DELETE"))).thenReturn(del);
        when(del.executeUpdate()).thenReturn(1);
        assertDoesNotThrow(() -> dao.deleteById(123L));
        verify(del).setLong(1,123L);
    }
}
