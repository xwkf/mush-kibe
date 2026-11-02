package br.com.muxe.core.common.testing;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public final class ManualExecutor implements Executor {

    private final Queue<Runnable> tasks = new ArrayDeque<>();
    private boolean shutdown;

    @Override
    public synchronized void execute(Runnable task) {
        if (shutdown) {
            throw new RejectedExecutionException("Executor is shut down");
        }
        tasks.add(task);
    }

    public void runNext() {
        Runnable task;
        synchronized (this) {
            task = tasks.poll();
        }
        if (task == null) {
            throw new IllegalStateException("No task waiting to run");
        }
        task.run();
    }

    public synchronized int waiting() {
        return tasks.size();
    }

    public synchronized void shutdown() {
        shutdown = true;
    }
}
