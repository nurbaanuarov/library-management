package com.library.management.dao.impl;

import com.library.management.exception.DataAccessException;
import com.library.management.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRoleDAOImplTest {

    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private UserRoleDAOImpl userRoleDAO;

    @BeforeEach
    void setUp() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);

        userRoleDAO = new UserRoleDAOImpl(dataSource);
    }

    @Test
    void testFindByUserId_ReturnsRoles() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("name")).thenReturn("READER", "ADMIN");

        Set<Role> roles = userRoleDAO.findByUserId(1L);

        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(r -> r.getName().equals("READER")));
        assertTrue(roles.stream().anyMatch(r -> r.getName().equals("ADMIN")));

        verify(statement).setLong(1, 1L);
        verify(statement).executeQuery();
    }

    @Test
    void testAddRoleForUser_Success() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        userRoleDAO.addRoleForUser(1L, 2L);

        verify(statement).setLong(1, 1L);
        verify(statement).setLong(2, 2L);
        verify(statement).executeUpdate();
    }

    @Test
    void testRemoveAllRolesForUser_Success() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        userRoleDAO.removeAllRolesForUser(1L);

        verify(statement).setLong(1, 1L);
        verify(statement).executeUpdate();
    }

    @Test
    void testFindByUserId_ThrowsException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> userRoleDAO.findByUserId(1L));

        assertTrue(ex.getMessage().contains("user_id=1"));
    }

    @Test
    void testAddRoleForUser_ThrowsException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> userRoleDAO.addRoleForUser(1L, 2L));

        assertTrue(ex.getMessage().contains("role_id=2"));
    }

    @Test
    void testRemoveAllRolesForUser_ThrowsException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> userRoleDAO.removeAllRolesForUser(1L));

        assertTrue(ex.getMessage().contains("user_id=1"));
    }
}
