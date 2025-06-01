package com.library.management.service;

import com.library.management.model.Role;

import java.util.Set;

public interface RoleService {
    Set<Role> findAllRoles();
}
