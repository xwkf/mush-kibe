package br.com.muxe.minigame.api.cosmetic.preview;

import br.com.muxe.minigame.api.cosmetic.catalog.Cosmetic;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public interface KitPlay {

    boolean covers(Cosmetic cosmetic);

    void launched(Player viewer, Cosmetic cosmetic, Projectile projectile);

    void ended(Player viewer);
}
