package com.library.management.dao;

import com.library.management.model.User;

import java.util.List;

public interface UserDAO {
    User findByUsername(String username);
    List<User> findAll();
    void save(User user);
}