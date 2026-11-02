package br.com.muxe.core.common.concurrent.bounded;

import br.com.muxe.core.common.concurrent.TaskExecutor;
import br.com.muxe.core.common.metrics.MetricRegistry;
import br.com.muxe.core.common.metrics.Tags;
import br.com.muxe.core.common.metrics.instrument.Counter;
import br.com.muxe.core.common.metrics.instrument.Timer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.ReentrantLock;

public final class BoundedExecutor implements TaskExecutor {

    private final String name;
    private final Executor delegate;
    private final ExecutorLimits limits;
    private final Clock clock;
    private final Timer queueWait;
    private final Map<RejectionReason, Counter> rejections = new EnumMap<>(RejectionReason.class);
    private final ReentrantLock lock = new ReentrantLock();
    private final Queue<Pending<?>> queue = new ArrayDeque<>();
    private volatile int active;
    private volatile int queued;

    public BoundedExecutor(String name, Executor delegate, ExecutorLimits limits, Clock clock, MetricRegistry metrics) {
        this.name = name;
        this.delegate = delegate;
        this.limits = limits;
        this.clock = clock;
        Tags tags = Tags.of("executor", name);
        this.queueWait = metrics.timer("muxe_executor_queue_wait_seconds", tags);
        for (RejectionReason reason : RejectionReason.values()) {
            rejections.put(reason, metrics.counter("muxe_executor_tasks_rejected_total", tags.and("reason", reason.tag())));
        }
        metrics.gauge("muxe_executor_tasks_active", tags, () -> active);
        metrics.gauge("muxe_executor_tasks_queued", tags, () -> queued);
    }

    @Override
    public <T> CompletableFuture<T> submit(Callable<T> task) {
        Pending<T> pending = new Pending<>(task, new CompletableFuture<>(), clock.instant());
        List<Pending<?>> expired = new ArrayList<>();
        boolean start = false;
        boolean full = false;
        lock.lock();
        try {
            removeExpired(pending.enqueuedAt(), expired);
            if (active < limits.maxConcurrent()) {
                active++;
                start = true;
            } else if (queue.size() < limits.maxQueued()) {
                queue.add(pending);
            } else {
                full = true;
            }
            queued = queue.size();
        } finally {
            lock.unlock();
        }
        expired.forEach(stale -> reject(stale, RejectionReason.QUEUE_TIMEOUT));
        if (full) {
            reject(pending, RejectionReason.QUEUE_FULL);
        } else if (start) {
            launchFrom(pending);
        }
        return pending.future();
    }

    public int active() {
        return active;
    }

    public int queued() {
        return queued;
    }

    private void launchFrom(Pending<?> first) {
        Pending<?> next = first;
        while (next != null) {
            if (launch(next)) {
                return;
            }
            next = handOff();
        }
    }

    private boolean launch(Pending<?> pending) {
        queueWait.record(Duration.between(pending.enqueuedAt(), clock.instant()));
        try {
            delegate.execute(() -> run(pending));
            return true;
        } catch (RejectedExecutionException exception) {
            ExecutorSaturatedException rejection = new ExecutorSaturatedException(name, RejectionReason.SHUTDOWN);
            rejection.initCause(exception);
            rejections.get(RejectionReason.SHUTDOWN).increment();
            TaskExecutor.fail(pending.future(), rejection);
            return false;
        }
    }

    private <T> void run(Pending<T> pending) {
        try {
            TaskExecutor.complete(pending.future(), pending.task());
        } finally {
            launchFrom(handOff());
        }
    }

    private Pending<?> handOff() {
        List<Pending<?>> expired = new ArrayList<>();
        Pending<?> next;
        lock.lock();
        try {
            removeExpired(clock.instant(), expired);
            next = queue.poll();
            if (next == null) {
                active--;
            }
            queued = queue.size();
        } finally {
            lock.unlock();
        }
        expired.forEach(pending -> reject(pending, RejectionReason.QUEUE_TIMEOUT));
        return next;
    }

    private void removeExpired(Instant now, List<Pending<?>> expired) {
        Pending<?> head = queue.peek();
        while (head != null && Duration.between(head.enqueuedAt(), now).compareTo(limits.queueTimeout()) > 0) {
            expired.add(queue.poll());
            head = queue.peek();
        }
    }

    private void reject(Pending<?> pending, RejectionReason reason) {
        rejections.get(reason).increment();
        TaskExecutor.fail(pending.future(), new ExecutorSaturatedException(name, reason));
    }

    private record Pending<T>(Callable<T> task, CompletableFuture<T> future, Instant enqueuedAt) {
    }
}
