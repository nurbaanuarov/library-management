package com.library.management.dao.impl;

import com.library.management.dao.UserDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserDAOImpl implements UserDAO {

    private final DataSource dataSource;

    private User getUser(String param, String sql) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return getUser(rs);
            }
        }
    }

    private User getUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEnabled(rs.getBoolean("enabled"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setLastModified(rs.getTimestamp("last_modified"));

        return user;
    }

    @Override
    public User findById(Long id) {
        String sql = "SELECT id, username, email, password_hash, enabled, created_at, last_modified " +
                "FROM users WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return getUser(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error querying user by id", e);
        }
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT id, username, email, password_hash, enabled, created_at, last_modified " +
                "FROM users WHERE username = ?";
        try {
            return getUser(username, sql);
        } catch (SQLException e) {
            throw new DataAccessException("Error querying user by username", e);
        }
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT id, username, email, password_hash, enabled, created_at, last_modified " +
                "FROM users WHERE email = ?";
        try {
            return getUser(email, sql);
        } catch (SQLException e) {
            throw new DataAccessException("Error querying user by email", e);
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
                   created_at,
                   last_modified
              FROM users
            """;

        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(getUser(rs));
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
                username, email, password_hash, enabled, created_at, last_modified
            ) VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setBoolean(4, user.isEnabled());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

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

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET email = ?, password_hash = ?, enabled = ?, last_modified = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setBoolean(3, user.isEnabled());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(5, user.getId());

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new DataAccessException("Updating user failed, no rows affected");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating user", e);
        }
    }
}