package com.library.management.service;

import com.library.management.model.User;

import javax.security.sasl.AuthenticationException;

public interface AuthService {
    /**
     * Проверить учетные данные пользователя
     * @param username логин
     * @param password пароль
     * @return объект User при успешной аутентификации
     */
    User authenticate(String username, String password) throws AuthenticationException;
}