package dev.minipool;

import javax.sql.DataSource;
import javax.sql.PooledConnection;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLTimeoutException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Logger;
import java.time.Duration;

public final class MiniPool implements DataSource, AutoCloseable {

    private final PoolConfig config;
    private final Deque<Connection> free;
    private boolean closed = false;

    public MiniPool(PoolConfig config) throws SQLException {
        this.config = config;
        this.free = new ArrayDeque<>(config.size());
        for (int i = 0; i < config.size(); i++) {
            free.addLast(DriverManager.getConnection(
                    config.jdbcUrl(), config.user(), config.password()));
        }
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        if (closed) throw new SQLException("pool is closed");
        long deadline = config.borrowTimeout().toNanos() + System.nanoTime();
        while (free.isEmpty()) {
            long remainingMillis = (deadline - System.nanoTime()) / 1_000_000;
            if (remainingMillis <= 0) {
                throw new SQLTimeoutException("timed out");
            }
            try {
                wait(remainingMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();;
                throw new SQLException("interrupted while waiting for connection");
            }
            if (closed) throw new SQLException("pool is closed");
        }
        Connection real = free.removeFirst();
        return PooledConnectionHandler.wrap(real, this);
    }

    /** Called by the proxy's close() to return a connection to the pool. */
    synchronized void returnToPool(Connection real) {
        if (closed) {
            try { real.close(); } catch (SQLException ignored) {}
            return;
        }
        free.addLast(real);
        notify(); // wake one waiter in getConnection()
    }

    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        for (Connection c : free) {
            try { c.close(); } catch (SQLException ignored) {}
        }
        free.clear();
        notifyAll(); // any waiters wake, see closed, throw cleanly
    }

    // --- DataSource stubs (unchanged) -------------------------------------
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