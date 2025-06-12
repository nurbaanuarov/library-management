package com.library.management.dao.impl;

import com.library.management.dao.BookCopyDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.model.Book;
import com.library.management.model.BookCopy;
import com.library.management.model.CopyStatus;
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
public class BookCopyDAOImpl implements BookCopyDAO {

    private final DataSource dataSource;

    private static final String SELECT_ALL =
            "SELECT id, book_id, inventory_number, status FROM book_copies";

    private static final String SELECT_BY_BOOK_ID =
            "SELECT id, book_id, inventory_number, status FROM book_copies WHERE book_id = ?";

    private static final String SELECT_BY_ID =
            "SELECT id, book_id, inventory_number, status FROM book_copies WHERE id = ?";

    private static final String SELECT_BY_INVENTORY =
            "SELECT id, book_id, inventory_number, status FROM book_copies WHERE inventory_number = ?";

    private static final String INSERT =
            "INSERT INTO book_copies (book_id, inventory_number, status) VALUES (?, ?, ?)";

    private static final String UPDATE_STATUS =
            "UPDATE book_copies SET status = ?::copy_status WHERE id = ?";

    private static final String UPDATE_COPY =
            """
            UPDATE book_copies
               SET inventory_number = ?,
                   status           = ?::copy_status
             WHERE id = ?
            """;

    @Override
    public List<BookCopy> findAll() {
        List<BookCopy> copies = new ArrayList<>();
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(SELECT_ALL);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                copies.add(mapRow(rs));
            }

            return copies;
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching all book copies", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public List<BookCopy> findByBookId(Long bookId) {
        List<BookCopy> copies = new ArrayList<>();
        Connection con = null;
        try {
            con = dataSource.getConnection();
            PreparedStatement ps = con.prepareStatement(SELECT_BY_BOOK_ID);

            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    copies.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching book copies for bookId=" + bookId, e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return copies;
    }

    @Override
    public Optional<BookCopy> findById(Long id) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(SELECT_BY_ID);
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching book copy by id", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void save(BookCopy copy) {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);

            ps.setLong(1, copy.getBook().getId());
            ps.setString(2, copy.getInventoryNumber());
            ps.setString(3, copy.getStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    copy.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving book copy", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void updateStatus(Long copyId, CopyStatus newStatus) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(UPDATE_STATUS);
            ps.setString(1, newStatus.name());
            ps.setLong(2, copyId);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Failed to update copy status, ID not found: " + copyId);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating book copy status", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void update(BookCopy copy) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(UPDATE_COPY);
            ps.setString(1, copy.getInventoryNumber());
            ps.setString(2, copy.getStatus().name());
            ps.setLong(3, copy.getId());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Updating book copy failed, no rows affected");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating book copy", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private BookCopy mapRow(ResultSet rs) throws SQLException {
        return BookCopy.builder()
                .id(rs.getLong("id"))
                .inventoryNumber(rs.getString("inventory_number"))
                .status(CopyStatus.valueOf(rs.getString("status")))
                .book(Book.builder().id(rs.getLong("book_id")).build())
                .build();
    }
}
