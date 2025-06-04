package com.library.management.service.impl;

import com.library.management.dao.RoleDAO;
import com.library.management.model.Role;
import com.library.management.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleDAO roleDAO;

    @Override
    @Transactional(readOnly = true)
    public Set<Role> findAllRoles() {
        return roleDAO.findAll();
    }
}
