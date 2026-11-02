package br.com.muxe.core.common.concurrent.bounded;

import java.util.concurrent.RejectedExecutionException;

public final class ExecutorSaturatedException extends RejectedExecutionException {

    private final String executor;
    private final RejectionReason reason;

    public ExecutorSaturatedException(String executor, RejectionReason reason) {
        super("Executor '" + executor + "' rejected a task: " + reason.tag());
        this.executor = executor;
        this.reason = reason;
    }

    public String executor() {
        return executor;
    }

    public RejectionReason reason() {
        return reason;
    }
}
