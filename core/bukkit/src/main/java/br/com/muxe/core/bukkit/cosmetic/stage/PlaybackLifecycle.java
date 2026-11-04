package br.com.muxe.core.bukkit.cosmetic.stage;

final class PlaybackLifecycle {

    enum Step {
        PLAY,
        FINISH,
        HOLD,
        CLOSE,
        DONE
    }

    private final int length;
    private int tick;
    private boolean finished;
    private boolean stopped;
    private boolean closed;

    PlaybackLifecycle(int length) {
        this.length = length;
    }

    Step next(boolean watched) {
        if (closed) {
            return Step.DONE;
        }
        if (!watched) {
            closed = true;
            return Step.CLOSE;
        }
        if (stopped || finished) {
            return Step.HOLD;
        }
        if (tick <= length) {
            tick++;
            return Step.PLAY;
        }
        finished = true;
        return Step.FINISH;
    }

    void stop() {
        stopped = true;
    }

    void close() {
        closed = true;
    }

    int tick() {
        return tick;
    }

    boolean stopped() {
        return stopped;
    }

    boolean finished() {
        return finished;
    }

    boolean closed() {
        return closed;
    }
}
