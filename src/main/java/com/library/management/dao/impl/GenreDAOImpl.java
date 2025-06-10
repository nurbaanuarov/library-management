package com.library.management.dao.impl;

import com.library.management.dao.GenreDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.model.Genre;
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
public class GenreDAOImpl implements GenreDAO {

    private final DataSource dataSource;

    private static final String SELECT_ALL = "SELECT id, name FROM genres";
    private static final String SELECT_BY_ID = "SELECT id, name FROM genres WHERE id = ?";
    private static final String SELECT_BY_NAME = "SELECT id, name FROM genres WHERE name = ?";
    private static final String INSERT = "INSERT INTO genres (name) VALUES (?)";
    private static final String DELETE = "DELETE FROM genres WHERE id = ?";

    @Override
    public List<Genre> findAll() {
        List<Genre> genres = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                genres.add(mapRow(rs));
            }
            return genres;
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching all genres", e);
        }
    }

    @Override
    public Optional<Genre> findById(Long id) {
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
            throw new DataAccessException("Error fetching genre by ID", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public Optional<Genre> findByName(String name) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(SELECT_BY_NAME);
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching genre by name", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void save(Genre genre) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, genre.getName());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("Creating genre failed, no rows affected");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    genre.setId(keys.getLong(1));
                } else {
                    throw new DataAccessException("Creating genre failed, no ID obtained");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving genre", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void delete(Genre genre) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(DELETE);
            ps.setLong(1, genre.getId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("Deleting genre failed, no rows affected");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting genre", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private Genre mapRow(ResultSet rs) throws SQLException {
        return Genre.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
    }
}
