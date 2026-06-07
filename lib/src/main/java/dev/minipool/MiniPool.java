package dev.minipool;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public final class MiniPool implements DataSource, AutoCloseable {
    private final PoolConfig config;
    
    public MiniPool(PoolConfig config) {
        this.config = config;
    }

    @Override
    public Connection getConnection() throws SQLException {
        throw new UnsupportedOperationException("step 3");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("step 3");
    }

    // DataSource methods we deliberately don't implement, every JDBC pool stubs most of these and we do the same
    @Override public Connection getConnection(String u, String p) throws SQLException {
        throw new SQLFeatureNotSupportedException("use getConnection()");
    }
    @Override public PrintWriter getLogWriter() { throw new UnsupportedOperationException(); }
    @Override public void setLogWriter(PrintWriter out) { throw new UnsupportedOperationException(); }
    @Override public void setLoginTimeout(int seconds) { throw new UnsupportedOperationException(); }
    @Override public int getLoginTimeout() { return 0; }
    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("not a wrapper");
    }
    @Override public boolean isWrapperFor(Class<?> iface) { return false; }
}