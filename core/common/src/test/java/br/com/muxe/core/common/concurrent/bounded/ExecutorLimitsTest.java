package br.com.muxe.core.common.concurrent.bounded;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class ExecutorLimitsTest {

    @Test
    void hasSensibleDefaults() {
        ExecutorLimits limits = ExecutorLimits.defaults();

        assertThat(limits.maxConcurrent()).isEqualTo(32);
        assertThat(limits.maxQueued()).isEqualTo(1_000);
        assertThat(limits.queueTimeout()).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void allowsAnEmptyQueue() {
        assertThat(new ExecutorLimits(1, 0, Duration.ofSeconds(1)).maxQueued()).isZero();
    }

    @Test
    void refusesInvalidLimits() {
        assertThatThrownBy(() -> new ExecutorLimits(0, 10, Duration.ofSeconds(1))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExecutorLimits(1, -1, Duration.ofSeconds(1))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExecutorLimits(1, 10, Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExecutorLimits(1, 10, Duration.ofSeconds(-1))).isInstanceOf(IllegalArgumentException.class);
    }
}
