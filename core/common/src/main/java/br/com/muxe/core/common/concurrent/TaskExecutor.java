package br.com.muxe.core.common.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public interface TaskExecutor {

    <T> CompletableFuture<T> submit(Callable<T> task);

    static TaskExecutor of(Executor executor) {
        return new TaskExecutor() {
            @Override
            public <T> CompletableFuture<T> submit(Callable<T> task) {
                CompletableFuture<T> future = new CompletableFuture<>();
                try {
                    executor.execute(() -> complete(future, task));
                } catch (RejectedExecutionException exception) {
                    fail(future, exception);
                }
                return future;
            }
        };
    }

    static <T> void complete(CompletableFuture<T> future, Callable<T> task) {
        try {
            future.complete(task.call());
        } catch (Throwable throwable) {
            fail(future, throwable);
        }
    }

    static void fail(CompletableFuture<?> future, Throwable cause) {
        future.completeExceptionally(cause instanceof CompletionException ? cause : new CompletionException(cause));
    }
}
