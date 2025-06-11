package com.library.management.dao.impl;

import com.library.management.dao.BookRequestDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.model.BookCopy;
import com.library.management.model.BookRequest;
import com.library.management.model.RequestStatus;
import com.library.management.model.RequestType;
import com.library.management.model.User;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.*;
        import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookRequestDAOImpl implements BookRequestDAO {
    private final DataSource dataSource;

    private static final String SELECT_ALL =
            "SELECT id, user_id, copy_id, type, status, request_date, issue_date, return_date " +
                    "FROM book_requests";

    private static final String SELECT_BY_ID = SELECT_ALL + " WHERE id = ?";

    private static final String UPDATE =
            "UPDATE book_requests SET status = ?, issue_date = ?, return_date = ? WHERE id = ?";

    private static final String INSERT = """
        INSERT INTO book_requests (user_id, copy_id, type, status, request_date, issue_date, return_date)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

    @Override
    public List<BookRequest> findAll() {
        List<BookRequest> list = new ArrayList<>();
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(SELECT_ALL);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching book requests", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return list;
    }

    @Override
    public Optional<BookRequest> findById(Long id) {
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
            throw new DataAccessException("Error fetching request by id", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void save(BookRequest request) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, request.getUser().getId());
            if (request.getCopy() != null) {
                ps.setLong(2, request.getCopy().getId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, request.getType().name());
            ps.setString(4, request.getStatus().name());
            ps.setTimestamp(5, Timestamp.valueOf(request.getRequestDate()));
            if (request.getIssueDate() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(request.getIssueDate()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }
            if (request.getReturnDate() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(request.getReturnDate()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }
            int rows = ps.executeUpdate();
            if (rows == 0) throw new DataAccessException("Creating request failed, no rows affected");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) request.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving book request", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }


    @Override
    public void update(BookRequest request) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(UPDATE);
            ps.setString(1, request.getStatus().name());
            ps.setTimestamp(2, request.getIssueDate() != null ? Timestamp.valueOf(request.getIssueDate()) : null);
            ps.setTimestamp(3, request.getReturnDate() != null ? Timestamp.valueOf(request.getReturnDate()) : null);
            ps.setLong(4, request.getId());
            int cnt = ps.executeUpdate();
            if (cnt == 0) {
                throw new DataAccessException("No request updated, id=" + request.getId());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating book request", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private BookRequest mapRow(ResultSet rs) throws SQLException {
        return BookRequest.builder()
                .id(rs.getLong("id"))
                .user(User.builder().id(rs.getLong("user_id")).build())
                .copy(BookCopy.builder().id(rs.getLong("copy_id")).build())
                .type(RequestType.valueOf(rs.getString("type")))
                .status(RequestStatus.valueOf(rs.getString("status")))
                .requestDate(rs.getTimestamp("request_date").toLocalDateTime())
                .issueDate(rs.getTimestamp("issue_date") != null ? rs.getTimestamp("issue_date").toLocalDateTime() : null)
                .returnDate(rs.getTimestamp("return_date") != null ? rs.getTimestamp("return_date").toLocalDateTime() : null)
                .build();
    }
}