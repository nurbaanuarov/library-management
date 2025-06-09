package com.library.management.service.impl;

import com.library.management.dao.UserDAO;
import com.library.management.dao.UserRoleDAO;
import com.library.management.dto.RegistrationForm;
import com.library.management.exception.UserNotFoundException;
import com.library.management.model.Role;
import com.library.management.model.User;
import com.library.management.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserDAO userDAO;
    private UserRoleDAO userRoleDAO;
    private RegistrationService registrationService;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userDAO = mock(UserDAO.class);
        userRoleDAO = mock(UserRoleDAO.class);
        registrationService = mock(RegistrationService.class);
        userService = new UserServiceImpl(userDAO, userRoleDAO, registrationService);
    }

    @Test
    void testFindAll_ReturnsUsersWithRoles() {
        User user1 = User.builder().id(1L).username("john").build();
        User user2 = User.builder().id(2L).username("jane").build();

        when(userDAO.findAll()).thenReturn(List.of(user1, user2));
        when(userRoleDAO.findByUserId(1L)).thenReturn(Set.of(Role.builder().id(1L).name("USER").build()));
        when(userRoleDAO.findByUserId(2L)).thenReturn(Set.of(Role.builder().id(2L).name("ADMIN").build()));

        List<User> result = userService.findAll();

        assertEquals(2, result.size());
        assertEquals("USER", result.get(0).getRoles().iterator().next().getName());
        assertEquals("ADMIN", result.get(1).getRoles().iterator().next().getName());
    }

    @Test
    void testFindById_ExistingUser_ReturnsUser() {
        User user = User.builder().id(1L).username("alice").build();
        when(userDAO.findById(1L)).thenReturn(Optional.of(user));
        when(userRoleDAO.findByUserId(1L)).thenReturn(Set.of(Role.builder().id(3L).name("MODERATOR").build()));

        User result = userService.findById(1L);

        assertEquals("alice", result.getUsername());
        assertEquals("MODERATOR", result.getRoles().iterator().next().getName());
    }

    @Test
    void testFindById_NonExistingUser_ThrowsException() {
        when(userDAO.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(99L));
    }

    @Test
    void testUpdateUser_WhenUserIsAdmin_ThrowsException() {
        User user = User.builder().id(1L).username("admin").build();
        when(userRoleDAO.findByUserId(1L)).thenReturn(Set.of(Role.builder().id(1L).name("ADMIN").build()));

        assertThrows(IllegalStateException.class, () -> userService.updateUser(user, Set.of(2L)));
    }

    @Test
    void testUpdateUser_UpdatesSuccessfully() {
        User user = User.builder().id(2L).username("bob").build();
        Set<Role> existingRoles = Set.of(Role.builder().id(2L).name("USER").build());
        when(userRoleDAO.findByUserId(2L)).thenReturn(existingRoles);

        userService.updateUser(user, Set.of(3L));

        verify(userDAO).update(user);
        verify(userRoleDAO).removeAllRolesForUser(2L);
        verify(userRoleDAO).addRoleForUser(2L, 3L);
    }

    @Test
    void testCreateUser_CreatesSuccessfully() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("newuser");
        form.setEmail("newuser@example.com");
        form.setPassword("password123");
        form.setConfirmPassword("password123");

        User createdUser = User.builder().id(10L).username("newuser").build();
        when(userDAO.findByUsername("newuser")).thenReturn(Optional.of(createdUser));

        userService.createUser(form, Set.of(5L, 6L));

        verify(registrationService).register(form);
        verify(userRoleDAO).removeAllRolesForUser(10L);
        verify(userRoleDAO).addRoleForUser(10L, 5L);
        verify(userRoleDAO).addRoleForUser(10L, 6L);
    }

    @Test
    void testCreateUser_UserNotFoundAfterRegister_ThrowsException() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("ghost");
        form.setEmail("ghost@nowhere.com");
        form.setPassword("pwd12345");
        form.setConfirmPassword("pwd12345");

        when(userDAO.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.createUser(form, Set.of(1L)));
    }
}
