package br.com.muxe.core.common.persistence.database;

import java.time.Duration;
import java.util.Objects;

public record DatabaseConfig(
    String url,
    String username,
    String password,
    int poolSize,
    Duration connectionTimeout,
    boolean runMigrations,
    Duration slowStatementThreshold
) {

    public static final Duration DEFAULT_SLOW_STATEMENT_THRESHOLD = Duration.ofMillis(250);

    public DatabaseConfig(String url, String username, String password, int poolSize, Duration connectionTimeout, boolean runMigrations) {
        this(url, username, password, poolSize, connectionTimeout, runMigrations, DEFAULT_SLOW_STATEMENT_THRESHOLD);
    }

    public DatabaseConfig {
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
        Objects.requireNonNull(connectionTimeout, "connectionTimeout");
        Objects.requireNonNull(slowStatementThreshold, "slowStatementThreshold");
        if (!url.startsWith("jdbc:postgresql://")) {
            throw new IllegalArgumentException("Only PostgreSQL JDBC urls are supported");
        }
        if (poolSize < 1) {
            throw new IllegalArgumentException("Database pool size must be positive");
        }
        if (slowStatementThreshold.isNegative() || slowStatementThreshold.isZero()) {
            throw new IllegalArgumentException("The slow statement threshold must be positive");
        }
    }

    @Override
    public String toString() {
        return "DatabaseConfig[" + url + ", user=" + username + ", pool=" + poolSize + "]";
    }
}
