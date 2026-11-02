package br.com.muxe.core.common.concurrent.bounded;

import java.time.Duration;
import java.util.Objects;

public record ExecutorLimits(int maxConcurrent, int maxQueued, Duration queueTimeout) {

    public static final int DEFAULT_MAX_CONCURRENT = 32;
    public static final int DEFAULT_MAX_QUEUED = 1_000;
    public static final Duration DEFAULT_QUEUE_TIMEOUT = Duration.ofSeconds(5);

    public ExecutorLimits {
        Objects.requireNonNull(queueTimeout, "queueTimeout");
        if (maxConcurrent < 1) {
            throw new IllegalArgumentException("Max concurrent tasks must be positive, got: " + maxConcurrent);
        }
        if (maxQueued < 0) {
            throw new IllegalArgumentException("Max queued tasks must not be negative, got: " + maxQueued);
        }
        if (queueTimeout.isNegative() || queueTimeout.isZero()) {
            throw new IllegalArgumentException("Queue timeout must be positive, got: " + queueTimeout);
        }
    }

    public static ExecutorLimits defaults() {
        return new ExecutorLimits(DEFAULT_MAX_CONCURRENT, DEFAULT_MAX_QUEUED, DEFAULT_QUEUE_TIMEOUT);
    }
}
