package com.library.management.service;

import com.library.management.dto.RegistrationForm;
import com.library.management.model.User;
import java.util.List;
import java.util.Set;

public interface UserService {
    List<User> findAll();
    User findById(long id);
    void updateUser(User user, Set<Long> newRoleIds);
    void createUser(RegistrationForm user, Set<Long> newRoleIds);
}
