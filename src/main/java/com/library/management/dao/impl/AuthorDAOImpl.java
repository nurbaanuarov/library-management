package com.library.management.dao.impl;

import com.library.management.dao.AuthorDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.model.Author;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AuthorDAOImpl implements AuthorDAO {

    private final DataSource dataSource;

    private static final String SELECT_ALL = "SELECT id, first_name, last_name FROM authors";
    private static final String SELECT_BY_ID = "SELECT id, first_name, last_name FROM authors WHERE id = ?";
    private static final String SELECT_BY_FIRSTNAME_AND_LASTNAME = "SELECT id, first_name, last_name FROM authors WHERE first_name = ? AND last_name = ?";
    private static final String INSERT = "INSERT INTO authors (first_name, last_name) VALUES (?, ?)";
    private static final String DELETE = "DELETE FROM authors WHERE id = ?";

    @Override
    public List<Author> findAll() {
        List<Author> authors = new ArrayList<>();
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(SELECT_ALL);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                authors.add(mapRow(rs));
            }
            return authors;
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching all authors", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public Optional<Author> findById(Long id) {
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
            throw new DataAccessException("Error fetching author by ID", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public Optional<Author> findByFirstNameAndLastName(String firstName, String lastName) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(SELECT_BY_FIRSTNAME_AND_LASTNAME);
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching author by name and surname", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void save(Author author) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, author.getFirstName());
            ps.setString(2, author.getLastName());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("Creating author failed, no rows affected");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    author.setId(keys.getLong(1));
                } else {
                    throw new DataAccessException("Creating author failed, no ID obtained");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving author", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void delete(Author author) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(DELETE);
            ps.setLong(1, author.getId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("Deleting author failed, no rows affected");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting author", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private Author mapRow(ResultSet rs) throws SQLException {
        return Author.builder()
                .id(rs.getLong("id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .build();
    }
}
