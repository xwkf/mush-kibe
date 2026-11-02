package br.com.muxe.core.bukkit.scoreboard;

import java.util.Optional;
import java.util.function.Function;
import org.bukkit.entity.Player;

public final class SidebarOverrides {

    private volatile Function<Player, Optional<SidebarContent>> provider = player -> Optional.empty();

    public void provide(Function<Player, Optional<SidebarContent>> provider) {
        this.provider = provider;
    }

    public void clear() {
        this.provider = player -> Optional.empty();
    }

    public Optional<SidebarContent> contentFor(Player player) {
        return provider.apply(player);
    }
}
