package dev.minipool;

import java.time.Duration;

public record PoolConfig(
        String jdbcUrl,
        String user,
        String password,
        int size,
        Duration borrowTimeout
) {
    public static PoolConfig forH2InMemory(int size) {
        return new PoolConfig(
                "jdbc:h2:mem:minipool;DB_CLOSE_DELAY=-1",
                "sa",
                "",
                size,
                Duration.ofSeconds(1));
    }
}