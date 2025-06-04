package com.library.management.service.impl;

import com.library.management.dao.RoleDAO;
import com.library.management.dao.UserDAO;
import com.library.management.dao.UserRoleDAO;
import com.library.management.dto.RegistrationForm;
import com.library.management.model.Role;
import com.library.management.model.User;
import com.library.management.service.RegistrationService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final UserDAO userDao;
    private final PasswordEncoder encoder;
    private final UserRoleDAO userRoleDao;
    private final RoleDAO roleDao;

    @Override
    @Transactional
    public void register(RegistrationForm form) {
        if (userDao.findByUsername(form.getUsername()) != null) {
            throw new ValidationException("Username already taken");
        }
        if (userDao.findByEmail(form.getEmail()) != null) {
            throw new ValidationException("Email already taken");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }
        User user = new User();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPasswordHash(encoder.encode(form.getPassword()));
        user.setEnabled(true);
        userDao.save(user);

        Role readerRole = roleDao.findByName("READER");
        userRoleDao.addRoleForUser(user.getId(), readerRole.getId());
    }
}