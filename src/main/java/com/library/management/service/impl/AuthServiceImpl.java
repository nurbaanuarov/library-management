package com.library.management.service.impl;

import com.library.management.dao.UserDAO;
import com.library.management.exception.AuthenticationException;
import com.library.management.model.User;
import com.library.management.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserDAO userDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(UserDAO userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User authenticate(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password");
        }
        if (!user.isEnabled()) {
            throw new AuthenticationException("User is disabled");
        }
        return user;
    }
}