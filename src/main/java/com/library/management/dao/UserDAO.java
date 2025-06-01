package com.library.management.dao;

import com.library.management.model.User;

import java.util.List;

public interface UserDAO {
    User findById(Long id);
    User findByUsername(String username);
    User findByEmail(String email);
    List<User> findAll();
    void save(User user);
    void update(User user);
}