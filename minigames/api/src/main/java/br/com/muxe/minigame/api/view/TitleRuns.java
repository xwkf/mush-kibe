package br.com.muxe.minigame.api.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TitleRuns {

    private final Map<UUID, Runnable> running = new HashMap<>();

    public void start(UUID player, Runnable stop) {
        Optional.ofNullable(running.put(player, stop)).ifPresent(Runnable::run);
    }

    public void done(UUID player, Runnable stop) {
        running.remove(player, stop);
    }

    public void stop(UUID player) {
        Optional.ofNullable(running.remove(player)).ifPresent(Runnable::run);
    }

    public boolean running(UUID player) {
        return running.containsKey(player);
    }
}
