package com.library.management.dao.impl;

import com.library.management.dao.UserDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserDAOImpl implements UserDAO {

    private final DataSource dataSource;

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT id, username, email, password_hash, enabled, created_at " +
                "FROM users WHERE username = ?";
        return getUser(username, sql);
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT id, username, email, password_hash, enabled, created_at " +
                "FROM users WHERE email = ?";
        return getUser(email, sql);
    }

    private User getUser(String param, String sql) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setEnabled(rs.getBoolean("enabled"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return user;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error querying user by username", e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = """
            SELECT id,
                   username,
                   email,
                   password_hash,
                   enabled,
                   created_at
              FROM users
            """;

        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                long id = rs.getLong("id");

                user.setId(id);
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setEnabled(rs.getBoolean("enabled"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                users.add(user);
            }

            return users;
        } catch (SQLException e) {
            throw new DataAccessException("Error querying all users", e);
        }
    }

    @Override
    public void save(User user) {
        String sql = """
            INSERT INTO users (
                username, email, password_hash, enabled
            ) VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setBoolean(4, user.isEnabled());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Creating user failed, no rows affected");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                } else {
                    throw new DataAccessException("Creating user failed, no ID obtained");
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error saving new user", e);
        }
    }
}