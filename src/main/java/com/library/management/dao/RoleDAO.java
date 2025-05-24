package com.library.management.dao;

import com.library.management.model.Role;
import java.util.Set;

public interface RoleDAO {
    Role findByName(String name);

    Set<Role> findAll();
}
