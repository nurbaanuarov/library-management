package com.library.management.dao.impl;

import com.library.management.exception.DataAccessException;
import com.library.management.model.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorDAOImplTest {

    private Connection connection;
    private PreparedStatement ps;
    private ResultSet rs;
    private AuthorDAOImpl dao;

    @BeforeEach
    void setUp() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        ps         = mock(PreparedStatement.class);
        rs         = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        dao = new AuthorDAOImpl(dataSource);
    }

    @Test
    void findAll_returnsList() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        // two rows then end
        when(rs.next()).thenReturn(true, true, false);

        // first row
        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getString("first_name"))
                .thenReturn("Jane")
                .thenReturn("Mark");
        when(rs.getString("last_name"))
                .thenReturn("Austen")
                .thenReturn("Twain");

        List<Author> list = dao.findAll();
        assertEquals(2, list.size());
        assertEquals("Jane", list.get(0).getFirstName());
        assertEquals("Twain", list.get(1).getLastName());
    }

    @Test
    void findById_found() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong("id")).thenReturn(42L);
        when(rs.getString("first_name")).thenReturn("Emily");
        when(rs.getString("last_name")).thenReturn("Bronte");

        Optional<Author> a = dao.findById(42L);
        assertTrue(a.isPresent());
        assertEquals(42L, a.get().getId());
        verify(ps).setLong(1, 42L);
    }

    @Test
    void findById_notFound() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertTrue(dao.findById(99L).isEmpty());
    }

    @Test
    void findByFirstNameAndLastName_found() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong("id")).thenReturn(7L);
        when(rs.getString("first_name")).thenReturn("Leo");
        when(rs.getString("last_name")).thenReturn("Tolstoy");

        Optional<Author> a = dao.findByFirstNameAndLastName("Leo", "Tolstoy");
        assertTrue(a.isPresent());
        assertEquals("Leo", a.get().getFirstName());
        verify(ps).setString(1, "Leo");
        verify(ps).setString(2, "Tolstoy");
    }

    @Test
    void findByFirstNameAndLastName_notFound() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertTrue(dao.findByFirstNameAndLastName("X","Y").isEmpty());
    }

    @Test
    void save_success_setsId() throws Exception {
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        ResultSet keys = mock(ResultSet.class);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(123L);

        Author auth = Author.builder()
                .firstName("Virginia")
                .lastName("Woolf")
                .build();

        dao.save(auth);
        assertEquals(123L, auth.getId());
    }

    @Test
    void save_noRowsAffected_throws() throws Exception {
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        Author auth = Author.builder().firstName("X").lastName("Y").build();
        assertThrows(DataAccessException.class, () -> dao.save(auth));
    }

    @Test
    void save_noGeneratedKey_throws() throws Exception {
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        ResultSet keys = mock(ResultSet.class);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(false);

        Author auth = Author.builder().firstName("X").lastName("Y").build();
        assertThrows(DataAccessException.class, () -> dao.save(auth));
    }

    @Test
    void delete_success() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        Author a = Author.builder().id(5L).build();
        assertDoesNotThrow(() -> dao.delete(a));
        verify(ps).setLong(1,5L);
    }

    @Test
    void delete_noRow_throws() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        Author a = Author.builder().id(5L).build();
        assertThrows(DataAccessException.class, () -> dao.delete(a));
    }
}
