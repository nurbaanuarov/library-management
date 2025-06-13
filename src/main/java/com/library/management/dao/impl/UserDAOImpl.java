package com.library.management.dao.impl;

import com.library.management.dao.UserDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.ILoggerFactory;
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
@Slf4j
public class UserDAOImpl implements UserDAO {
    private final DataSource dataSource;

    private static final String SELECT = "SELECT id, username, email, password_hash, enabled, created_at, last_modified FROM users";
    private static final String SELECT_BY_USERNAME = "SELECT id, username, email, password_hash, enabled, created_at, last_modified FROM users WHERE username = ?";
    private static final String SELECT_BY_EMAIL = "SELECT id, username, email, password_hash, enabled, created_at, last_modified FROM users WHERE email = ?";
    private static final String SELECT_BY_ID = "SELECT id, username, email, password_hash, enabled, created_at, last_modified FROM users WHERE id = ?";

    private static final String INSERT = "INSERT INTO users (username, email, password_hash, enabled, created_at, last_modified) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE = "UPDATE users SET email = ?, password_hash = ?, enabled = ?, last_modified = ? WHERE id = ?";

    @Override
    public Optional<User> findById(Long id) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(SELECT_BY_ID);
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error querying user by id", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try {
            return getByParameter(username, SELECT_BY_USERNAME);
        } catch (SQLException e) {
            throw new DataAccessException("Error querying user by username", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            return getByParameter(email, SELECT_BY_EMAIL);
        } catch (SQLException e) {
            throw new DataAccessException("Error querying user by email", e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        Connection con = null;

        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(SELECT);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                users.add(mapRow(rs));
            }

            log.info("Found {} users", users.size());
            return users;
        } catch (SQLException e) {
            throw new DataAccessException("Error querying all users", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void save(User user) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
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
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void update(User user) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
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
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private Optional<User> getByParameter(String param, String sql) throws SQLException {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error querying user by parameter", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
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