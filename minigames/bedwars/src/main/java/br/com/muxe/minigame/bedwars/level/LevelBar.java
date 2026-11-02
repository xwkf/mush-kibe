package br.com.muxe.minigame.bedwars.level;

import br.com.muxe.minigame.bedwars.game.Levels;
import org.bukkit.entity.Player;

public final class LevelBar {

    private LevelBar() {
    }

    public static Levels.Progress show(Player player, long totalXp) {
        Levels.Progress progress = Levels.progress(Math.max(0, totalXp));
        player.setLevel(progress.level());
        player.setExp(progress.fill());
        return progress;
    }
}
