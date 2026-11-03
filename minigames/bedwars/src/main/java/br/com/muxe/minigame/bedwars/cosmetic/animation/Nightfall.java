package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;

final class Nightfall {

    static final long MIDDAY = 6000;
    static final long NIGHT = 18000;
    static final int TICKS = 30;

    private Nightfall() {
    }

    static long dayTime(int tick) {
        int clamped = Math.min(Math.max(tick, 0), TICKS);
        return Math.round(MIDDAY + (NIGHT - MIDDAY) / 2.0 * (1 - Math.cos(Math.PI * clamped / TICKS)));
    }

    static void schedule(Stage stage, Timeline timeline) {
        timeline.repeat(0, TICKS, 1, tick -> stage.time(dayTime(tick)));
    }
}
