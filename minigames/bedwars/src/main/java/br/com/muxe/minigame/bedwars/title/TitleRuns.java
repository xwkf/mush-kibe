package br.com.muxe.minigame.bedwars.title;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class TitleRuns {

    private final Map<UUID, Runnable> running = new HashMap<>();

    void start(UUID player, Runnable stop) {
        Optional.ofNullable(running.put(player, stop)).ifPresent(Runnable::run);
    }

    void done(UUID player, Runnable stop) {
        running.remove(player, stop);
    }

    void stop(UUID player) {
        Optional.ofNullable(running.remove(player)).ifPresent(Runnable::run);
    }

    boolean running(UUID player) {
        return running.containsKey(player);
    }
}
