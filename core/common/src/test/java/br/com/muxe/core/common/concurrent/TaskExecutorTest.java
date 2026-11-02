package br.com.muxe.core.common.concurrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.RejectedExecutionException;
import org.junit.jupiter.api.Test;

class TaskExecutorTest {

    @Test
    void runsTheTaskOnTheGivenExecutor() {
        TaskExecutor executor = TaskExecutor.of(Runnable::run);

        assertThat(executor.submit(() -> "done")).isCompletedWithValue("done");
    }

    @Test
    void failsTheFutureWhenTheTaskThrows() {
        TaskExecutor executor = TaskExecutor.of(Runnable::run);

        CompletableFuture<String> future = executor.submit(() -> {
            throw new IllegalStateException("boom");
        });

        assertThatThrownBy(future::join).isInstanceOf(CompletionException.class).hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void failsTheFutureInsteadOfThrowingWhenTheExecutorRejects() {
        TaskExecutor executor = TaskExecutor.of(task -> {
            throw new RejectedExecutionException("shut down");
        });

        CompletableFuture<String> future = executor.submit(() -> "never");

        assertThatThrownBy(future::join).isInstanceOf(CompletionException.class).hasCauseInstanceOf(RejectedExecutionException.class);
    }
}
