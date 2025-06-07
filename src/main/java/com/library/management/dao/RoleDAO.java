package com.library.management.dao;

import com.library.management.model.Role;

import java.util.Optional;
import java.util.Set;

public interface RoleDAO {
    Optional<Role> findByName(String name);

    Set<Role> findAll();
}
