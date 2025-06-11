package com.library.management.dao.impl;

import com.library.management.dao.BookDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.model.Author;
import com.library.management.model.Book;
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
public class BookDAOImpl implements BookDAO {

    private final DataSource dataSource;

    private static final String SELECT_ALL = "SELECT id, title, description, author_id, genre_id, total_copies FROM books";
    private static final String SELECT_BY_ID = "SELECT * FROM books WHERE id = ?";
    private static final String INSERT = "INSERT INTO books (title, description, author_id, genre_id, total_copies) VALUES (?, ?, ?, ?, ?)";
    private static final String DELETE = "DELETE FROM books WHERE id = ?";


    @Override
    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(SELECT_ALL);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                books.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching books", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return books;
    }

    @Override
    public Optional<Book> findById(Long id) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(SELECT_BY_ID);
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching book by ID", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void save(Book book) {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getDescription());
            ps.setLong(3, book.getAuthor().getId());
            ps.setLong(4, book.getGenre().getId());
            ps.setInt(5, book.getTotalCopies());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    book.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving book", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void deleteById(Long id) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(DELETE);

            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("No book deleted, ID not found: " + id);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting book", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }


    private Book mapRow(ResultSet rs) throws SQLException {
        return Book.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .totalCopies(rs.getInt("total_copies"))
                .author(Author.builder().id(rs.getLong("author_id")).build())
                .genre(Genre.builder().id(rs.getLong("genre_id")).build())
                .build();
    }
}
