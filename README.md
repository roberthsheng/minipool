# minipool

A JDBC connection pool, built from scratch in Java, to understand what HikariCP actually does. Not for production use.

## Why
Every JDBC app has a pool, most engineers can't explain what happens inside one under contention, this is me figuring that out the long way.

## Status

In progress. Build order:
- [ ] v0 — naive single-lock pool
- [ ] v0 — borrowing stress test that exposes contention
- [ ] v1 — measured improvement (BlockingQueue handoff)
- [ ] v2 — leak detection, liveness/validation, metrics
- [ ] teardown — read HikariCP, explain `ConcurrentBag` / `FastList` / proxy

## Build & test

```bash
./gradlew test
```