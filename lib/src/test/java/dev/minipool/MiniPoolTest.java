package dev.minipool;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLTimeoutException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;


class MiniPoolTest {
    @Test
    void canBorrowUpToPoolSize() throws Exception {
        var config = PoolConfig.forH2InMemory(2);
        try (var pool = new MiniPool(config)) {
            try (Connection c1 = pool.getConnection();
                 Connection c2 = pool.getConnection()) {
                assertThat(c1).isNotNull();
                assertThat(c2).isNotNull();
                assertThat(c2).isNotSameAs(c1);
            }
        }
    }

    @Test
    void exhaustionTimesOut() throws Exception {
        var config = PoolConfig.forH2InMemory(1);
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            try (var pool = new MiniPool(config)) {
                try (Connection c1 = pool.getConnection()) {
                    assertThatThrownBy(() -> pool.getConnection()).isInstanceOf(SQLTimeoutException.class);
                }
            }
        });
    }

    @Test
    void closingReturnsToPool() throws Exception {
        var config = PoolConfig.forH2InMemory(1);
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            try (var pool = new MiniPool(config)) {
                Connection c1 = pool.getConnection();
                c1.close();
                try (Connection c2 = pool.getConnection()) {
                    assertThat(c2).isNotNull();
                }
            }
        });
    }
}