package com.library.management.dao.impl;

import com.library.management.exception.DataAccessException;
import com.library.management.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleDAOImplTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private RoleDAOImpl roleDAO;

    @BeforeEach
    void setup() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);

        roleDAO = new RoleDAOImpl(dataSource);
    }

    @Test
    void testFindByName_found() throws Exception {
        String roleName = "READER";

        when(connection.prepareStatement("SELECT id, name FROM roles WHERE name = ?"))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("name")).thenReturn(roleName);

        Optional<Role> roleOpt = roleDAO.findByName(roleName);

        assertTrue(roleOpt.isPresent());
        assertEquals(1L, roleOpt.get().getId());
        assertEquals("READER", roleOpt.get().getName());

        verify(preparedStatement).setString(1, roleName);
    }

    @Test
    void testFindByName_notFound() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<Role> result = roleDAO.findByName("NON_EXISTENT");

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAll_returnsRoles() throws Exception {
        when(connection.prepareStatement("SELECT id, name FROM roles")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("name")).thenReturn("READER", "ADMIN");

        Set<Role> roles = roleDAO.findAll();

        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(r -> r.getName().equals("READER")));
        assertTrue(roles.stream().anyMatch(r -> r.getName().equals("ADMIN")));
    }

    @Test
    void testFindByName_sqlException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        DataAccessException ex = assertThrows(DataAccessException.class, () -> roleDAO.findByName("READER"));

        assertTrue(ex.getMessage().contains("Error querying role by name"));
    }
}
