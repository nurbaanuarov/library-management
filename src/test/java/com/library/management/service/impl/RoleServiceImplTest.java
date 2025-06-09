package com.library.management.service.impl;

import com.library.management.dao.RoleDAO;
import com.library.management.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceImplTest {

    private RoleDAO roleDAO;
    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleDAO = mock(RoleDAO.class);
        roleService = new RoleServiceImpl(roleDAO);
    }

    @Test
    void testFindAllRoles_ReturnsRoles() {
        Set<Role> mockRoles = Set.of(new Role(1L, "USER"), new Role(2L, "ADMIN"));
        when(roleDAO.findAll()).thenReturn(mockRoles);

        Set<Role> result = roleService.findAllRoles();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(role -> role.getName().equals("USER")));
        verify(roleDAO, times(1)).findAll();
    }
}
