package com.library.management.service.impl;

import com.library.management.dao.RoleDAO;
import com.library.management.dao.UserDAO;
import com.library.management.dao.UserRoleDAO;
import com.library.management.dto.RegistrationForm;
import com.library.management.exception.InputValidationException;
import com.library.management.exception.RoleNotFoundException;
import com.library.management.model.Role;
import com.library.management.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationServiceImplTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private UserRoleDAO userRoleDAO;

    @Mock
    private RoleDAO roleDAO;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSuccessfulRegistration() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("john");
        form.setEmail("john@example.com");
        form.setPassword("123456");
        form.setConfirmPassword("123456");

        when(userDAO.findByUsername("john")).thenReturn(Optional.empty());
        when(userDAO.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("123456")).thenReturn("hashed");
        when(roleDAO.findByName("READER")).thenReturn(Optional.of(new Role(1L, "READER")));

        // Capture the saved user
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(100L);
            return null;
        }).when(userDAO).save(userCaptor.capture());

        registrationService.register(form);

        User savedUser = userCaptor.getValue();
        assertEquals("john", savedUser.getUsername());
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals("hashed", savedUser.getPasswordHash());
        assertTrue(savedUser.isEnabled());

        verify(userRoleDAO).addRoleForUser(100L, 1L);
    }

    @Test
    void testRegistrationUsernameTaken() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("john");
        form.setEmail("john@example.com");
        form.setPassword("123");
        form.setConfirmPassword("123");

        when(userDAO.findByUsername("john")).thenReturn(Optional.of(new User()));

        InputValidationException ex = assertThrows(InputValidationException.class,
                () -> registrationService.register(form));
        assertEquals("Username already taken", ex.getMessage());
    }

    @Test
    void testRegistrationEmailTaken() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("john");
        form.setEmail("john@example.com");
        form.setPassword("123");
        form.setConfirmPassword("123");

        when(userDAO.findByUsername("john")).thenReturn(Optional.empty());
        when(userDAO.findByEmail("john@example.com")).thenReturn(Optional.of(new User()));

        InputValidationException ex = assertThrows(InputValidationException.class,
                () -> registrationService.register(form));
        assertEquals("Email already taken", ex.getMessage());
    }

    @Test
    void testRegistrationPasswordMismatch() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("john");
        form.setEmail("john@example.com");
        form.setPassword("123");
        form.setConfirmPassword("456");

        when(userDAO.findByUsername("john")).thenReturn(Optional.empty());
        when(userDAO.findByEmail("john@example.com")).thenReturn(Optional.empty());

        InputValidationException ex = assertThrows(InputValidationException.class,
                () -> registrationService.register(form));
        assertEquals("Passwords do not match", ex.getMessage());
    }

    @Test
    void testReaderRoleNotFound() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("john");
        form.setEmail("john@example.com");
        form.setPassword("123");
        form.setConfirmPassword("123");

        when(userDAO.findByUsername("john")).thenReturn(Optional.empty());
        when(userDAO.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("123")).thenReturn("hashed");
        when(roleDAO.findByName("READER")).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> registrationService.register(form));
    }
}
