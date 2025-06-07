package com.library.management.service.impl;

import com.library.management.dao.UserDAO;
import com.library.management.dao.UserRoleDAO;
import com.library.management.dto.RegistrationForm;
import com.library.management.exception.UserNotFoundException;
import com.library.management.model.Role;
import com.library.management.model.User;
import com.library.management.service.RegistrationService;
import com.library.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDAO userDao;
    private final UserRoleDAO userRoleDao;

    private final RegistrationService registrationService;

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userDao.findAll().stream().map(userMapping()).toList();
    }

    private Function<User, User> userMapping() {
        return user -> {
            user.setRoles(userRoleDao.findByUserId(user.getId()));
            return user;
        };
    }
    @Override
    @Transactional(readOnly = true)
    public User findById(long id) {
        return userDao.findById(id).map(userMapping())
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
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

    @Override
    @Transactional
    public void createUser(RegistrationForm form, Set<Long> roleIds) {
        registrationService.register(form);

        User user = userDao.findByUsername(form.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found after registration: " + form.getUsername()));
        long userId = user.getId();

        userRoleDao.removeAllRolesForUser(userId);
        roleIds.forEach(roleId ->
                userRoleDao.addRoleForUser(userId, roleId)
        );
    }
}
