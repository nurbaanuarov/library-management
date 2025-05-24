package com.library.management.dao;

import com.library.management.model.Role;

import java.util.Set;

public interface UserRoleDAO {
    Set<Role> findByUserId(long userId);
    void addRoleForUser(long userId, long roleId);
}
