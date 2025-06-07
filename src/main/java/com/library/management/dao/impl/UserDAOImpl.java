package com.library.management.dao.impl;

import com.library.management.dao.UserDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDAOImpl implements UserDAO {
    private final DataSource dataSource;

    private static final String SELECT = "SELECT id, username, email, password_hash, enabled, created_at, last_modified FROM users";
    private static final String BY_ID = "WHERE id = ?";
    private static final String BY_USERNAME = "WHERE username = ?";
    private static final String BY_EMAIL = "WHERE email = ?";

    private static final String INSERT = "INSERT INTO users (username, email, password_hash, enabled, created_at, last_modified) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE = "UPDATE users SET email = ?, password_hash = ?, enabled = ?, last_modified = ? WHERE id = ?";

    @Override
    public Optional<User> findById(Long id) {
        try {
            Connection conn = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = conn.prepareStatement(SELECT + BY_ID);
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(getUser(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error querying user by id", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try {
            return getUserByParameter(username, SELECT + BY_USERNAME);
        } catch (SQLException e) {
            throw new DataAccessException("Error querying user by username", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            return getUserByParameter(email, SELECT + BY_EMAIL);
        } catch (SQLException e) {
            throw new DataAccessException("Error querying user by email", e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT);
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
        try {
            Connection con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);

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
            throw new DataAccessException("Error saving user", e);
        }
    }

    @Override
    public void update(User user) {
        try {
            Connection con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(UPDATE);
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

    private Optional<User> getUserByParameter(String param, String sql) throws SQLException {
        try {
            Connection conn = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(getUser(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error querying user by parameter", e);
        }
    }

    private User getUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .passwordHash(rs.getString("password_hash"))
                .enabled(rs.getBoolean("enabled"))
                .createdAt(rs.getTimestamp("created_at"))
                .lastModified(rs.getTimestamp("last_modified"))
                .build();
    }
}