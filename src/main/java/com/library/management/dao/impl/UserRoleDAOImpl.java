package com.library.management.dao.impl;

import com.library.management.dao.UserRoleDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserRoleDAOImpl implements UserRoleDAO {
    private final DataSource dataSource;

    private static final String SELECT_BY_USER_ID = """
                SELECT r.id, r.name
                  FROM roles r
                  JOIN user_roles ur ON ur.role_id = r.id
                 WHERE ur.user_id = ?
                """;
    private static final String INSERT = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
    private static final String DELETE = "DELETE FROM user_roles WHERE user_id = ?";

    @Override
    public Set<Role> findByUserId(long userId) {
        Set<Role> roles = new HashSet<>();
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(SELECT_BY_USER_ID);
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
            throw new DataAccessException("Error querying roles for user_id=" + userId, e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return roles;
    }

    @Override
    public void addRoleForUser(long userId, long roleId) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(INSERT);
            ps.setLong(1, userId);
            ps.setLong(2, roleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Error adding role_id=" + roleId + " for user_id=" + userId, e
            );
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void removeAllRolesForUser(long userId) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(DELETE);
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Error removing all roles for user_id=" + userId, e
            );
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }
}