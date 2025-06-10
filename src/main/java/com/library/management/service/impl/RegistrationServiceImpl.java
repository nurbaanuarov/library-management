package com.library.management.service.impl;

import com.library.management.dao.RoleDAO;
import com.library.management.dao.UserDAO;
import com.library.management.dao.UserRoleDAO;
import com.library.management.dto.RegistrationForm;
import com.library.management.exception.InputValidationException;
import com.library.management.exception.RoleNotFoundException;
import com.library.management.model.Role;
import com.library.management.model.User;
import com.library.management.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final UserDAO userDao;
    private final PasswordEncoder encoder;
    private final UserRoleDAO userRoleDao;
    private final RoleDAO roleDao;

    @Override
    public void register(RegistrationForm form) {
        if (userDao.findByUsername(form.getUsername()).isPresent()) {
            throw new InputValidationException("Username already taken");
        }
        if (userDao.findByEmail(form.getEmail()).isPresent()) {
            throw new InputValidationException("Email already taken");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new InputValidationException("Passwords do not match");
        }

        User user = User.builder()
                .username(form.getUsername())
                .email(form.getEmail())
                .passwordHash(encoder.encode(form.getPassword()))
                .enabled(true)
                .build();
        userDao.save(user);

        Role readerRole = roleDao.findByName("READER")
                .orElseThrow(() -> new RoleNotFoundException("Role READER not found"));
        userRoleDao.addRoleForUser(user.getId(), readerRole.getId());
    }
}