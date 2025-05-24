package com.library.management.dao.impl;

import com.library.management.dao.UserRoleDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserRoleDAOImpl implements UserRoleDAO {
    private final DataSource dataSource;

    @Override
    public Set<Role> findByUserId(long userId) {
        String SELECT_ROLES_BY_USER_ID =
                """
                SELECT r.id, r.name
                  FROM roles r
                  JOIN user_roles ur ON ur.role_id = r.id
                 WHERE ur.user_id = ?
                """;

        Set<Role> roles = new HashSet<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ROLES_BY_USER_ID)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Role r = new Role();
                    r.setId(rs.getLong("id"));
                    r.setName(rs.getString("name"));
                    roles.add(r);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error querying roles of user", e);
        }
        return roles;
    }

    @Override
    public void addRoleForUser(long userId, long roleId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                  INSERT INTO user_roles (user_id, role_id)
                       VALUES (?, ?)
                  """)) {
            ps.setLong(1, userId);
            ps.setLong(2, roleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Error inserting role " + roleId + " for user " + userId, e);
        }
    }
}

