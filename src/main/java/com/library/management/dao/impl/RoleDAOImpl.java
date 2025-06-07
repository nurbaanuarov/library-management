package com.library.management.dao.impl;

import com.library.management.dao.RoleDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RoleDAOImpl implements RoleDAO {
    private final DataSource dataSource;

    private static final String SELECT_BY_NAME = "SELECT id, name FROM roles WHERE name = ?";

    private static final String SELECT_ALL = "SELECT id, name FROM roles";

    @Override
    public Optional<Role> findByName(String name) {
        try {
            Connection conn = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = conn.prepareStatement(SELECT_BY_NAME);

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Role r = Role.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .build();
                return Optional.of(r);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error querying role by name: " + name, e);
        }
    }

    @Override
    public Set<Role> findAll() {
        Set<Role> roles = new HashSet<>();
        try {
            Connection conn = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Role r = Role.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .build();
                roles.add(r);
            }
            return roles;
        } catch (SQLException e) {
            throw new DataAccessException("Error querying all roles", e);
        }
    }
}
