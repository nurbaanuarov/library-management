package com.library.management.service.impl;

import com.library.management.exception.DataAccessException;
import com.library.management.model.Role;
import com.library.management.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final DataSource dataSource;

    @Override
    public Set<Role> findAllRoles() {
        String sql = """
            SELECT r.id, r.name
              FROM roles r
            """;

        Set<Role> roles = new HashSet<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Role r = new Role();
                    r.setId(rs.getLong("id"));
                    r.setName(rs.getString("name"));
                    roles.add(r);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error querying roles", e);
        }
        return roles;
    }
}
