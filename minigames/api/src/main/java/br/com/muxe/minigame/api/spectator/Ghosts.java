package br.com.muxe.minigame.api.spectator;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.api.settings.HotbarItem;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class Ghosts {

    private Ghosts() {
    }

    public static void become(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.spigot().setCollidesWithEntities(false);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true), true);
    }

    public static boolean is(Player player) {
        return !player.spigot().getCollidesWithEntities();
    }

    public static void restore(Player player) {
        player.setFlying(false);
        player.setAllowFlight(false);
        player.spigot().setCollidesWithEntities(true);
    }

    public static void give(Player player, List<HotbarItem> entries) {
        for (HotbarItem entry : entries) {
            Material material = Material.matchMaterial(entry.material());
            ItemBuilder item = ItemBuilder.of(material == null ? Material.STONE : material).name(entry.name()).showFlags();
            player.getInventory().setItem(entry.slot(), (isTool(material) ? item.unbreakable() : item).build());
        }
        player.updateInventory();
    }

    public static void refresh(Collection<Player> players, Predicate<Player> spectator) {
        for (Player viewer : players) {
            boolean viewerIsSpectator = spectator.test(viewer);
            for (Player target : players) {
                if (viewer.equals(target)) {
                    continue;
                }
                if (SpectatorTargets.hidden(viewerIsSpectator, spectator.test(target))) {
                    viewer.hidePlayer(target);
                } else {
                    viewer.showPlayer(target);
                }
            }
        }
    }

    private static boolean isTool(Material material) {
        return material != null && material.getMaxDurability() > 0;
    }
}
