package com.library.management.service.impl;

import com.library.management.dao.UserDAO;
import com.library.management.dao.UserRoleDAO;
import com.library.management.model.Role;
import com.library.management.model.User;
import com.library.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDAO userDao;
    private final UserRoleDAO userRoleDao;

    @Override
    public List<User> findAllUsers() {
        List<User> users = userDao.findAll();
        users.forEach(user -> user.setRoles(userRoleDao.findByUserId(user.getId())));
        return users;
    }

    @Override
    public User findById(long id) {
        User user = userDao.findById(id);
        user.setRoles(userRoleDao.findByUserId(user.getId()));
        return user;
    }

    @Override
    @Transactional
    public void updateUser(User user, Set<Long> newRoleIds) {
        Set<String> currentRoleIds = userRoleDao.findByUserId(user.getId())
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        if (currentRoleIds.contains("ADMIN")) {
            throw new IllegalStateException("Cannot modify admin users");
        }

        userDao.update(user);
        userRoleDao.removeAllRolesForUser(user.getId());

        for (Long rid : newRoleIds) {
            userRoleDao.addRoleForUser(user.getId(), rid);
        }
    }

}
