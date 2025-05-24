package com.library.management.dao.impl;

import com.library.management.dao.RoleDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RoleDAOImpl implements RoleDAO {
    private final DataSource dataSource;

    private static final String SELECT_BY_NAME =
            "SELECT id, name FROM roles WHERE name = ?";

    private static final String SELECT_ALL =
            "SELECT id, name FROM roles";

    @Override
    public Role findByName(String name) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_NAME)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Role r = new Role();
                r.setId(rs.getLong("id"));
                r.setName(rs.getString("name"));
                return r;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error querying role by name: " + name, e);
        }
    }

    @Override
    public Set<Role> findAll() {
        Set<Role> roles = new HashSet<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Role r = new Role();
                r.setId(rs.getLong("id"));
                r.setName(rs.getString("name"));
                roles.add(r);
            }
            return roles;
        } catch (SQLException e) {
            throw new DataAccessException("Error querying all roles", e);
        }
    }
}
