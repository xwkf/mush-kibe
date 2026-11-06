package br.com.muxe.minigame.bedwars.cosmetic.preview;

import br.com.muxe.minigame.bedwars.cosmetic.catalog.BedCosmetic;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public interface KitPlay {

    boolean covers(BedCosmetic cosmetic);

    void launched(Player viewer, BedCosmetic cosmetic, Projectile projectile);

    void ended(Player viewer);
}
