package com.library.management.dao.impl;

import com.library.management.exception.DataAccessException;
import com.library.management.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDAOImplTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private UserDAOImpl userDAO;

    @BeforeEach
    void setUp() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);

        userDAO = new UserDAOImpl(dataSource);
    }

    @Test
    void testFindByUsername_found() throws Exception {
        // Given
        String username = "testuser";

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("username")).thenReturn(username);
        when(resultSet.getString("email")).thenReturn("test@example.com");
        when(resultSet.getString("password_hash")).thenReturn("hashed");
        when(resultSet.getBoolean("enabled")).thenReturn(true);
        when(resultSet.getTimestamp("created_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(resultSet.getTimestamp("last_modified")).thenReturn(new Timestamp(System.currentTimeMillis()));

        // When
        Optional<User> result = userDAO.findByUsername(username);

        // Then
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());

        verify(preparedStatement).setString(1, username);
    }

    @Test
    void testFindByUsername_notFound() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        Optional<User> result = userDAO.findByUsername("unknown");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByUsername_sqlException() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        // When / Then
        assertThrows(DataAccessException.class, () -> userDAO.findByUsername("error"));
    }

    @Test
    void testFindById_found() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("username")).thenReturn("john");
        when(resultSet.getString("email")).thenReturn("john@example.com");
        when(resultSet.getString("password_hash")).thenReturn("hash");
        when(resultSet.getBoolean("enabled")).thenReturn(true);
        when(resultSet.getTimestamp("created_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(resultSet.getTimestamp("last_modified")).thenReturn(new Timestamp(System.currentTimeMillis()));

        Optional<User> result = userDAO.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("john", result.get().getUsername());
    }

    @Test
    void testFindById_notFound() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<User> result = userDAO.findById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll_usersExist() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false); // only 1 user

        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("username")).thenReturn("user1");
        when(resultSet.getString("email")).thenReturn("user1@example.com");
        when(resultSet.getString("password_hash")).thenReturn("hash");
        when(resultSet.getBoolean("enabled")).thenReturn(true);
        when(resultSet.getTimestamp("created_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(resultSet.getTimestamp("last_modified")).thenReturn(new Timestamp(System.currentTimeMillis()));

        List<User> result = userDAO.findAll();

        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getUsername());
    }

    @Test
    void testSave_success() throws Exception {
        when(connection.prepareStatement(anyString(), Statement.RETURN_GENERATED_KEYS))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeUpdate()).thenReturn(1);

        ResultSet keys = mock(ResultSet.class);
        when(preparedStatement.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(99L);

        User user = User.builder()
                .username("newuser")
                .email("new@example.com")
                .passwordHash("hash")
                .enabled(true)
                .build();

        userDAO.save(user);

        assertEquals(99L, user.getId());
    }

    @Test
    void testSave_noRowsAffected() throws Exception {
        when(connection.prepareStatement(anyString(), Statement.RETURN_GENERATED_KEYS))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        User user = User.builder().username("fail").email("fail@example.com").build();

        assertThrows(DataAccessException.class, () -> userDAO.save(user));
    }

    @Test
    void testSave_noGeneratedKeys() throws Exception {
        when(connection.prepareStatement(anyString(), Statement.RETURN_GENERATED_KEYS))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        ResultSet keys = mock(ResultSet.class);
        when(preparedStatement.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(false); // no key

        User user = User.builder().username("fail").email("fail@example.com").build();

        assertThrows(DataAccessException.class, () -> userDAO.save(user));
    }

    @Test
    void testUpdate_success() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        User user = User.builder()
                .id(1L)
                .email("updated@example.com")
                .passwordHash("newHash")
                .enabled(true)
                .build();

        assertDoesNotThrow(() -> userDAO.update(user));
    }

    @Test
    void testUpdate_noRowsAffected() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        User user = User.builder()
                .id(1L)
                .email("fail@example.com")
                .build();

        assertThrows(DataAccessException.class, () -> userDAO.update(user));
    }
}
