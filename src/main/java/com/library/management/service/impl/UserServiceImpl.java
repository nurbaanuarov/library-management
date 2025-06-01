package com.library.management.service.impl;

import com.library.management.dao.UserDAO;
import com.library.management.dao.UserRoleDAO;
import com.library.management.model.User;
import com.library.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

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
        userDao.update(user);

        userRoleDao.removeAllRolesForUser(user.getId());

        for (Long rid : newRoleIds) {
            userRoleDao.addRoleForUser(user.getId(), rid);
        }
    }
}
