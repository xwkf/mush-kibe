package br.com.muxe.minigame.bedwars.spectator;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.core.bukkit.menu.Menu;
import java.util.List;
import java.util.function.Function;
import org.bukkit.entity.Player;

public final class TeleporterMenu {

    private static final int ROW = 9;
    private static final int MAX_ROWS = 6;

    private TeleporterMenu() {
    }

    public static void open(Player viewer, Teleporter settings, List<Player> targets, Function<Player, String> name) {
        int rows = Math.clamp((targets.size() + ROW - 1) / ROW, 1, MAX_ROWS);
        Menu.Builder builder = Menu.builder(settings.title()).rows(rows);
        for (int index = 0; index < Math.min(targets.size(), rows * ROW); index++) {
            Player target = targets.get(index);
            builder.button(index, ItemBuilder.skull().skullOwner(target.getName()).name(name.apply(target)).lore(settings.entryLore()), context -> {
                context.close();
                if (target.isOnline() && target.getWorld().equals(viewer.getWorld())) {
                    viewer.teleport(target.getLocation());
                }
            });
        }
        builder.build().open(viewer);
    }

    public record Teleporter(String title, List<String> entryLore) {
    }
}
