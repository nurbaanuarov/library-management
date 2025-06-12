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

    private static final String SELECT_BY_COPY_AND_STATUS =
            """
            SELECT 
              br.id, br.user_id, br.copy_id, br.type, br.status,
              br.request_date, br.issue_date, br.return_date
            FROM book_requests br
            WHERE br.copy_id = ?
              AND br.status = ?
            """;

    private static final String UPDATE =
            "UPDATE book_requests SET status = ?, issue_date = ?, return_date = ? WHERE id = ?";

    private static final String INSERT =
            "INSERT INTO book_requests " +
                    "(user_id, copy_id, type, status, request_date, issue_date, return_date) " +
                    "VALUES (?, ?, ?::request_type, ?::request_status, ?, ?, ?)";

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
    public Optional<BookRequest> findByCopyAndStatus(Long copyId, RequestStatus status) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            try (PreparedStatement ps = con.prepareStatement(SELECT_BY_COPY_AND_STATUS)) {
                ps.setLong(1, copyId);
                ps.setString(2, status.name());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error querying request for copy=" + copyId + " status=" + status, e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void save(BookRequest req) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);

            ps.setLong(1, req.getUser().getId());
            ps.setLong(2, req.getCopy().getId());
            ps.setString(3, req.getType().name());      // now cast in SQL
            ps.setString(4, req.getStatus().name());    // now cast in SQL
            ps.setTimestamp(5, Timestamp.valueOf(req.getRequestDate()));
            ps.setTimestamp(6,
                    req.getIssueDate()   != null ? Timestamp.valueOf(req.getIssueDate())   : null);
            ps.setTimestamp(7,
                    req.getReturnDate()  != null ? Timestamp.valueOf(req.getReturnDate())  : null);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Creating request failed, no rows affected");
            }
            // handle generated key...
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
        User user = User.builder().id(rs.getLong("user_id")).build();
        BookCopy copy = BookCopy.builder().id(rs.getLong("copy_id")).build();

        return BookRequest.builder()
                .id(rs.getLong("id"))
                .user(user)
                .copy(copy)
                .type(RequestType.valueOf(rs.getString("type")))
                .status(RequestStatus.valueOf(rs.getString("status")))
                .requestDate(rs.getTimestamp("request_date").toLocalDateTime())
                .issueDate(rs.getTimestamp("issue_date") != null
                        ? rs.getTimestamp("issue_date").toLocalDateTime()
                        : null)
                .returnDate(rs.getTimestamp("return_date") != null
                        ? rs.getTimestamp("return_date").toLocalDateTime()
                        : null)
                .build();
    }
}