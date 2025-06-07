package com.library.management.dao;

import com.library.management.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void save(User user);
    void update(User user);
}