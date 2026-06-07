package dev.minipool;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

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
}

