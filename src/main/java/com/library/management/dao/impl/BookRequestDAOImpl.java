package com.library.management.dao.impl;

import com.library.management.dao.BookRequestDAO;
import com.library.management.exception.DataAccessException;
import com.library.management.model.BookRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
@RequiredArgsConstructor
public class BookRequestDAOImpl implements BookRequestDAO {

    private final DataSource dataSource;

    private static final String INSERT = """
        INSERT INTO book_requests (user_id, copy_id, type, status, request_date, issue_date, return_date)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

    @Override
    public void save(BookRequest request) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, request.getUser().getId());
            ps.setLong(2, request.getCopy().getId());
            ps.setString(3, request.getType().name());
            ps.setString(4, request.getStatus().name());
            ps.setTimestamp(5, Timestamp.valueOf(request.getRequestDate()));
            ps.setTimestamp(6, Timestamp.valueOf(request.getIssueDate()));
            ps.setTimestamp(7, Timestamp.valueOf(request.getReturnDate()));

            int rows = ps.executeUpdate();
            if (rows == 0) throw new DataAccessException("Inserting request failed");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    request.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving book request", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }
}
