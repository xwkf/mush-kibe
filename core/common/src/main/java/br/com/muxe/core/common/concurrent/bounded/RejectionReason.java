package br.com.muxe.core.common.concurrent.bounded;

public enum RejectionReason {

    QUEUE_FULL("queue_full"),
    QUEUE_TIMEOUT("queue_timeout"),
    SHUTDOWN("shutdown");

    private final String tag;

    RejectionReason(String tag) {
        this.tag = tag;
    }

    public String tag() {
        return tag;
    }
}
