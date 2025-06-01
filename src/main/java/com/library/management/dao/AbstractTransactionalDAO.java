package com.library.management.dao;

import com.library.management.exception.DataAccessException;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RequiredArgsConstructor
public abstract class AbstractTransactionalDAO {
    protected final DataSource dataSource;

    protected void executeInTransaction(ThrowingConsumer<Connection> action) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                action.accept(conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new DataAccessException("Transaction failed, rolled back", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not get connection", e);
        }
    }

    @FunctionalInterface
    protected interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }
}
