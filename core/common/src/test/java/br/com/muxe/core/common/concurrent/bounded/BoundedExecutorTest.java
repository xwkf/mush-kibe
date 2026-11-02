package br.com.muxe.core.common.concurrent.bounded;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.core.common.metrics.MetricId;
import br.com.muxe.core.common.metrics.Tags;
import br.com.muxe.core.common.metrics.simple.SimpleMetricRegistry;
import br.com.muxe.core.common.metrics.snapshot.MetricsSnapshot;
import br.com.muxe.core.common.testing.ManualExecutor;
import br.com.muxe.core.common.testing.MutableClock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class BoundedExecutorTest {

    private static final Duration QUEUE_TIMEOUT = Duration.ofSeconds(5);

    private final ManualExecutor delegate = new ManualExecutor();
    private final MutableClock clock = MutableClock.startingAt("2026-01-01T00:00:00Z");
    private final SimpleMetricRegistry metrics = new SimpleMetricRegistry();

    @Test
    void startsUpToTheLimitAndQueuesTheRest() {
        BoundedExecutor executor = executor(2, 10);

        List<CompletableFuture<String>> futures = submitMany(executor, 3);

        assertThat(delegate.waiting()).isEqualTo(2);
        assertThat(executor.active()).isEqualTo(2);
        assertThat(executor.queued()).isEqualTo(1);
        assertThat(futures).noneMatch(CompletableFuture::isDone);
    }

    @Test
    void startsAQueuedTaskWhenARunningOneFinishes() {
        BoundedExecutor executor = executor(1, 10);
        CompletableFuture<String> first = executor.submit(() -> "first");
        CompletableFuture<String> second = executor.submit(() -> "second");

        delegate.runNext();

        assertThat(first).isCompletedWithValue("first");
        assertThat(second).isNotDone();
        assertThat(delegate.waiting()).isEqualTo(1);
        assertThat(executor.queued()).isZero();

        delegate.runNext();

        assertThat(second).isCompletedWithValue("second");
        assertThat(executor.active()).isZero();
    }

    @Test
    void neverRunsMoreTasksAtOnceThanTheLimit() {
        BoundedExecutor executor = executor(3, 100);
        AtomicInteger running = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        for (int index = 0; index < 20; index++) {
            executor.submit(() -> {
                peak.accumulateAndGet(executor.active(), Math::max);
                return running.incrementAndGet();
            });
        }

        while (delegate.waiting() > 0) {
            assertThat(delegate.waiting()).isLessThanOrEqualTo(3);
            delegate.runNext();
        }

        assertThat(running).hasValue(20);
        assertThat(peak).hasValue(3);
        assertThat(executor.active()).isZero();
    }

    @Test
    void rejectsRightAwayWhenTheQueueIsFull() {
        BoundedExecutor executor = executor(1, 1);
        AtomicInteger ran = new AtomicInteger();
        executor.submit(ran::incrementAndGet);
        executor.submit(ran::incrementAndGet);

        CompletableFuture<Integer> rejected = executor.submit(ran::incrementAndGet);

        assertRejected(rejected, RejectionReason.QUEUE_FULL);
        assertThat(rejections(RejectionReason.QUEUE_FULL)).isEqualTo(1);
        assertThat(executor.queued()).isEqualTo(1);
        assertThat(ran).hasValue(0);
    }

    @Test
    void failsFastWhenNoQueueIsAllowed() {
        BoundedExecutor executor = executor(1, 0);
        executor.submit(() -> "running");

        assertRejected(executor.submit(() -> "extra"), RejectionReason.QUEUE_FULL);
    }

    @Test
    void failsQueuedTasksThatWaitedLongerThanTheTimeout() {
        BoundedExecutor executor = executor(1, 10);
        CompletableFuture<String> running = executor.submit(() -> "running");
        CompletableFuture<String> stale = executor.submit(() -> "stale");

        clock.advance(QUEUE_TIMEOUT.plusMillis(1));
        delegate.runNext();

        assertThat(running).isCompletedWithValue("running");
        assertRejected(stale, RejectionReason.QUEUE_TIMEOUT);
        assertThat(rejections(RejectionReason.QUEUE_TIMEOUT)).isEqualTo(1);
        assertThat(delegate.waiting()).isZero();
        assertThat(executor.active()).isZero();
        assertThat(executor.queued()).isZero();
    }

    @Test
    void keepsQueuedTasksThatWaitedExactlyTheTimeout() {
        BoundedExecutor executor = executor(1, 10);
        executor.submit(() -> "running");
        CompletableFuture<String> queued = executor.submit(() -> "queued");

        clock.advance(QUEUE_TIMEOUT);
        delegate.runNext();
        delegate.runNext();

        assertThat(queued).isCompletedWithValue("queued");
    }

    @Test
    void failsStaleTasksWhenANewTaskArrives() {
        BoundedExecutor executor = executor(1, 1);
        executor.submit(() -> "running");
        CompletableFuture<String> stale = executor.submit(() -> "stale");

        clock.advance(QUEUE_TIMEOUT.plusSeconds(1));
        CompletableFuture<String> fresh = executor.submit(() -> "fresh");

        assertRejected(stale, RejectionReason.QUEUE_TIMEOUT);
        assertThat(fresh).isNotDone();
        assertThat(executor.queued()).isEqualTo(1);
    }

    @Test
    void releasesThePermitWhenTheTaskThrows() {
        BoundedExecutor executor = executor(1, 10);
        CompletableFuture<String> failing = executor.submit(() -> {
            throw new IllegalStateException("boom");
        });
        CompletableFuture<String> next = executor.submit(() -> "next");

        delegate.runNext();

        assertThatThrownBy(failing::join)
            .isInstanceOf(CompletionException.class)
            .hasCauseInstanceOf(IllegalStateException.class);
        delegate.runNext();
        assertThat(next).isCompletedWithValue("next");
        assertThat(executor.active()).isZero();
    }

    @Test
    void releasesThePermitWhenTheTaskThrowsACheckedException() {
        BoundedExecutor executor = executor(1, 10);
        CompletableFuture<String> failing = executor.submit(() -> {
            throw new Exception("checked");
        });

        delegate.runNext();

        assertThatThrownBy(failing::join).hasCauseInstanceOf(Exception.class).hasRootCauseMessage("checked");
        assertThat(executor.active()).isZero();
    }

    @Test
    void releasesThePermitWhenTheTaskThrowsAnError() {
        BoundedExecutor executor = executor(1, 10);
        CompletableFuture<String> failing = executor.submit(() -> {
            throw new StackOverflowError();
        });

        delegate.runNext();

        assertThatThrownBy(failing::join).hasCauseInstanceOf(StackOverflowError.class);
        assertThat(executor.active()).isZero();
    }

    @Test
    void failsTasksAndReleasesPermitsWhenTheDelegateIsShutDown() {
        BoundedExecutor executor = executor(1, 10);
        CompletableFuture<String> running = executor.submit(() -> "running");
        CompletableFuture<String> second = executor.submit(() -> "second");
        CompletableFuture<String> third = executor.submit(() -> "third");

        delegate.shutdown();
        delegate.runNext();

        assertThat(running).isCompletedWithValue("running");
        assertRejected(second, RejectionReason.SHUTDOWN);
        assertRejected(third, RejectionReason.SHUTDOWN);
        assertRejected(executor.submit(() -> "late"), RejectionReason.SHUTDOWN);
        assertThat(rejections(RejectionReason.SHUTDOWN)).isEqualTo(3);
        assertThat(executor.active()).isZero();
        assertThat(executor.queued()).isZero();
    }

    @Test
    void exposesActiveAndQueuedTasksAsGauges() {
        BoundedExecutor executor = executor(2, 10);
        submitMany(executor, 5);

        MetricsSnapshot snapshot = metrics.snapshot();

        assertThat(snapshot.gauges()).containsEntry(new MetricId("muxe_executor_tasks_active", Tags.of("executor", "test")), 2.0);
        assertThat(snapshot.gauges()).containsEntry(new MetricId("muxe_executor_tasks_queued", Tags.of("executor", "test")), 3.0);
    }

    @Test
    void recordsHowLongTasksWaitedInTheQueue() {
        BoundedExecutor executor = executor(1, 10);
        executor.submit(() -> "running");
        executor.submit(() -> "queued");

        clock.advance(Duration.ofSeconds(3));
        delegate.runNext();

        MetricId id = new MetricId("muxe_executor_queue_wait_seconds", Tags.of("executor", "test"));
        assertThat(metrics.snapshot().timers().get(id).count()).isEqualTo(2);
        assertThat(metrics.snapshot().timers().get(id).maxSeconds()).isEqualTo(3.0);
    }

    private BoundedExecutor executor(int maxConcurrent, int maxQueued) {
        return new BoundedExecutor("test", delegate, new ExecutorLimits(maxConcurrent, maxQueued, QUEUE_TIMEOUT), clock, metrics);
    }

    private static List<CompletableFuture<String>> submitMany(BoundedExecutor executor, int count) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            String value = "task-" + index;
            futures.add(executor.submit(() -> value));
        }
        return futures;
    }

    private long rejections(RejectionReason reason) {
        MetricId id = new MetricId("muxe_executor_tasks_rejected_total", Tags.of("executor", "test", "reason", reason.tag()));
        return metrics.snapshot().counters().get(id);
    }

    private static void assertRejected(CompletableFuture<?> future, RejectionReason reason) {
        assertThat(future).isCompletedExceptionally();
        assertThatThrownBy(future::join)
            .isInstanceOf(CompletionException.class)
            .cause()
            .isInstanceOfSatisfying(ExecutorSaturatedException.class, exception -> {
                assertThat(exception.reason()).isEqualTo(reason);
                assertThat(exception.executor()).isEqualTo("test");
            });
    }
}
