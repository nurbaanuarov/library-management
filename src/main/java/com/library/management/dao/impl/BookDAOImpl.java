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

    private static final String SELECT_ALL = """
        SELECT 
          b.id, b.title, b.description, b.total_copies,
          a.id   AS author_id,    a.first_name,    a.last_name,
          g.id   AS genre_id,     g.name          AS genre_name
        FROM books b
        JOIN authors a ON b.author_id = a.id
        JOIN genres  g ON b.genre_id  = g.id
        """;

    private static final String SELECT_BY_ID = """
        SELECT 
          b.id, b.title, b.description, b.total_copies,
          a.id   AS author_id,    a.first_name,    a.last_name,
          g.id   AS genre_id,     g.name          AS genre_name
        FROM books b
        JOIN authors a ON b.author_id = a.id
        JOIN genres  g ON b.genre_id  = g.id
        WHERE b.id = ?
        """;

    private static final String INSERT = """
        INSERT INTO books (title, description, author_id, genre_id, total_copies) 
        VALUES (?, ?, ?, ?, ?)
        """;

    private static final String DELETE = "DELETE FROM books WHERE id = ?";

    private static final String COUNT_BY_GENRE =
            "SELECT count(*) FROM books WHERE genre_id = ?";

    private static final String COUNT_BY_AUTHOR =
            "SELECT count(*) FROM books WHERE author_id = ?";


    @Override
    public List<Book> findAll() {
        List<Book> list = new ArrayList<>();
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            try (PreparedStatement ps = con.prepareStatement(SELECT_ALL);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching all books", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return list;
    }

    @Override
    public Optional<Book> findById(Long id) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            try (PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next()
                            ? Optional.of(mapRow(rs))
                            : Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching book by ID=" + id, e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public long countByGenreId(Long genreId) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(COUNT_BY_GENRE);
            ps.setLong(1, genreId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error counting books by genre", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public long countByAuthorId(Long authorId) {
        Connection con = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = con.prepareStatement(COUNT_BY_AUTHOR)) {
            ps.setLong(1, authorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Error counting books for authorId=" + authorId, e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void save(Book book) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            try (PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
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
            try (PreparedStatement ps = con.prepareStatement(DELETE)) {
                ps.setLong(1, id);
                if (ps.executeUpdate() == 0) {
                    throw new DataAccessException("No book deleted, ID not found: " + id);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting book id=" + id, e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Author author = Author.builder()
                .id(rs.getLong("author_id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .build();

        Genre genre = Genre.builder()
                .id(rs.getLong("genre_id"))
                .name(rs.getString("genre_name"))
                .build();

        return Book.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .totalCopies(rs.getInt("total_copies"))
                .author(author)
                .genre(genre)
                .build();
    }
}
