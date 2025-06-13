package com.library.management.dao.impl;

import com.library.management.exception.DataAccessException;
import com.library.management.model.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenreDAOImplTest {

    private Connection con;
    private PreparedStatement ps;
    private ResultSet rs;
    private GenreDAOImpl dao;

    @BeforeEach
    void setUp() throws Exception {
        DataSource ds = mock(DataSource.class);
        con = mock(Connection.class);
        ps  = mock(PreparedStatement.class);
        rs  = mock(ResultSet.class);

        when(ds.getConnection()).thenReturn(con);
        dao = new GenreDAOImpl(ds);
    }

    @Test
    void findAll_returnsList() throws Exception {
        when(con.prepareStatement(startsWith("SELECT id, name FROM genres"))).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);

        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getString("name")).thenReturn("G1","G2");

        List<Genre> list = dao.findAll();
        assertEquals(2, list.size());
        assertEquals("G1", list.get(0).getName());
        assertEquals(2L, list.get(1).getId());
    }

    @Test
    void findById_found() throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getLong("id")).thenReturn(77L);
        when(rs.getString("name")).thenReturn("Mystery");

        Optional<Genre> g = dao.findById(77L);
        assertTrue(g.isPresent());
        assertEquals("Mystery", g.get().getName());
        verify(ps).setLong(1,77L);
    }

    @Test
    void findById_notFound() throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertTrue(dao.findById(5L).isEmpty());
    }

    @Test
    void findByName_found() throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getLong("id")).thenReturn(88L);
        when(rs.getString("name")).thenReturn("Fantasy");

        Optional<Genre> g = dao.findByName("Fantasy");
        assertTrue(g.isPresent());
        assertEquals(88L, g.get().getId());
        verify(ps).setString(1,"Fantasy");
    }

    @Test
    void findByName_notFound() throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertTrue(dao.findByName("X").isEmpty());
    }

    @Test
    void save_setsId() throws Exception {
        PreparedStatement ins = mock(PreparedStatement.class);
        when(con.prepareStatement(startsWith("INSERT"), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(ins);
        when(ins.executeUpdate()).thenReturn(1);
        ResultSet keys = mock(ResultSet.class);
        when(ins.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(55L);

        Genre g = Genre.builder().name("New").build();
        dao.save(g);
        assertEquals(55L, g.getId());
    }

    @Test
    void save_noRowsAffected_throws() throws Exception {
        PreparedStatement ins = mock(PreparedStatement.class);
        when(con.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(ins);
        when(ins.executeUpdate()).thenReturn(0);

        Genre g = Genre.builder().name("Fail").build();
        assertThrows(DataAccessException.class, () -> dao.save(g));
    }

    @Test
    void delete_success() throws Exception {
        when(con.prepareStatement(startsWith("DELETE"))).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        Genre g = Genre.builder().id(3L).name("X").build();
        assertDoesNotThrow(() -> dao.delete(g));
        verify(ps).setLong(1,3L);
    }

    @Test
    void delete_noRow_throws() throws Exception {
        when(con.prepareStatement(startsWith("DELETE"))).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);
        Genre g = Genre.builder().id(3L).build();
        assertThrows(DataAccessException.class, () -> dao.delete(g));
    }
}
