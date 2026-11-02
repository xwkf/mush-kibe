package br.com.muxe.core.common.cqrs.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.core.common.concurrent.bounded.BoundedExecutor;
import br.com.muxe.core.common.concurrent.bounded.ExecutorLimits;
import br.com.muxe.core.common.concurrent.bounded.ExecutorSaturatedException;
import br.com.muxe.core.common.cqrs.command.Command;
import br.com.muxe.core.common.cqrs.middleware.MetricsMiddleware;
import br.com.muxe.core.common.cqrs.query.Query;
import br.com.muxe.core.common.metrics.simple.SimpleMetricRegistry;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.core.common.testing.ManualExecutor;
import br.com.muxe.core.common.testing.MutableClock;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;

class BoundedBusTest {

    private final ManualExecutor delegate = new ManualExecutor();
    private final MutableClock clock = MutableClock.startingAt("2026-01-01T00:00:00Z");
    private final SimpleMetricRegistry metrics = new SimpleMetricRegistry();

    @Test
    void failsQueriesBeyondTheLimitWithoutThrowingToTheCaller() {
        SimpleQueryBus bus = new SimpleQueryBus(bounded("queries"), List.of(new MetricsMiddleware(metrics)));
        bus.register(EchoQuery.class, query -> query.value());

        CompletableFuture<String> running = bus.ask(new EchoQuery("a"));
        CompletableFuture<String> rejected = bus.ask(new EchoQuery("b"));

        assertThatThrownBy(rejected::join)
            .isInstanceOf(CompletionException.class)
            .hasCauseInstanceOf(ExecutorSaturatedException.class);
        delegate.runNext();
        assertThat(running).isCompletedWithValue("a");
    }

    @Test
    void releasesTheSlotWhenACommandHandlerThrows() {
        BoundedExecutor executor = bounded("commands");
        SimpleCommandBus bus = new SimpleCommandBus(executor, List.of(new MetricsMiddleware(metrics)));
        bus.register(FailCommand.class, command -> {
            throw new IllegalStateException("handler failed");
        });

        CompletableFuture<Result<Void>> failed = bus.dispatch(new FailCommand());
        delegate.runNext();

        assertThatThrownBy(failed::join).hasCauseInstanceOf(IllegalStateException.class);
        assertThat(executor.active()).isZero();
    }

    private BoundedExecutor bounded(String name) {
        return new BoundedExecutor(name, delegate, new ExecutorLimits(1, 0, Duration.ofSeconds(5)), clock, metrics);
    }

    record EchoQuery(String value) implements Query<String> {
    }

    record FailCommand() implements Command<Result<Void>> {
    }
}
