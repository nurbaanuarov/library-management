package com.library.management.util;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * Простой потокобезопасный пул JDBC-соединений.
 */
public class ConnectionPool implements DataSource {
    private final String url;
    private final String username;
    private final String password;
    private final BlockingQueue<Connection> pool;

    public ConnectionPool(String url, String username, String password, int poolSize) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.pool = new LinkedBlockingQueue<>(poolSize);
        initPool(poolSize);
    }

    private void initPool(int size) {
        try {
            // ensure the driver is registered
            Class.forName("org.postgresql.Driver");

            for (int i = 0; i < size; i++) {
                Connection conn = DriverManager.getConnection(url, username, password);
                pool.add(conn);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found", e);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing connection pool", e);
        }
    }


    @Override
    public Connection getConnection() throws SQLException {
        try {
            Connection realConn = pool.take();
            return new PooledConnection(realConn, this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a database connection", e);
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    void releaseConnection(Connection conn) {
        pool.add(conn);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException();
    }
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException();
    }
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException();
    }
    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException(); }
    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    /**
     * Обёртка над реальным Connection: вместо close() возвращает соединение в пул.
     */
    private record PooledConnection(Connection real, ConnectionPool pool) implements Connection {

        @Override
        public void close() throws SQLException {
            if (!real.isClosed()) {
                pool.releaseConnection(real);
            }
        }

        // Прямое делегирование всех остальных методов Connection
        @Override
        public Statement createStatement() throws SQLException {
            return real.createStatement();
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return real.prepareStatement(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            return real.prepareCall(sql);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return real.nativeSQL(sql);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            real.setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return real.getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {
            real.commit();
        }

        @Override
        public void rollback() throws SQLException {
            real.rollback();
        }

        @Override
        public boolean isClosed() throws SQLException {
            return real.isClosed();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return real.getMetaData();
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            real.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return real.isReadOnly();
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            real.setCatalog(catalog);
        }

        @Override
        public String getCatalog() throws SQLException {
            return real.getCatalog();
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            real.setTransactionIsolation(level);
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return real.getTransactionIsolation();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return real.getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException {
            real.clearWarnings();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return real.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return real.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return real.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return real.getTypeMap();
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            real.setTypeMap(map);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            real.setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {
            return real.getHoldability();
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return real.setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            return real.setSavepoint(name);
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            real.rollback(savepoint);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            real.releaseSavepoint(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return real.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return real.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return real.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return real.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return real.prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return real.prepareStatement(sql, columnNames);
        }

        @Override
        public Clob createClob() throws SQLException {
            return real.createClob();
        }

        @Override
        public Blob createBlob() throws SQLException {
            return real.createBlob();
        }

        @Override
        public NClob createNClob() throws SQLException {
            return real.createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return real.createSQLXML();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return real.isValid(timeout);
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            real.setClientInfo(name, value);
        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            real.setClientInfo(properties);
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return real.getClientInfo(name);
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return real.getClientInfo();
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return real.createArrayOf(typeName, elements);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return real.createStruct(typeName, attributes);
        }

        @Override
        public void setSchema(String schema) throws SQLException {
            real.setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException {
            return real.getSchema();
        }

        @Override
        public void abort(Executor executor) throws SQLException {
            real.abort(executor);
        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            real.setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return real.getNetworkTimeout();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return real.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return real.isWrapperFor(iface);
        }
    }
}