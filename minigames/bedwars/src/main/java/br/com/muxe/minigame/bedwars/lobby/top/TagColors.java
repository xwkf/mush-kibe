package br.com.muxe.minigame.bedwars.lobby.top;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.common.permission.domain.Contexts;
import br.com.muxe.core.common.permission.domain.PermissionResolver;
import br.com.muxe.core.common.permission.domain.Tag;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TagColors {

    public static final String DEFAULT = "&7";

    private final MuxeCore core;
    private final Map<UUID, String> colors = new ConcurrentHashMap<>();
    private final Set<UUID> fetching = ConcurrentHashMap.newKeySet();
    private volatile Runnable onChange = () -> {
    };

    public TagColors(MuxeCore core) {
        this.core = core;
    }

    public void onChange(Runnable listener) {
        this.onChange = listener;
    }

    public String of(UUID playerId) {
        String color = colors.get(playerId);
        if (color == null) {
            fetch(playerId);
            return DEFAULT;
        }
        return color;
    }

    public void forgetAll() {
        colors.clear();
    }

    private void fetch(UUID playerId) {
        if (!fetching.add(playerId)) {
            return;
        }
        core.permissions().sync().fetch(playerId)
            .thenApply(user -> PermissionResolver.resolve(core.permissions().data(), user, Contexts.NONE, core.clock().instant()).tags().stream()
                .findFirst().map(Tag::color).orElse(DEFAULT))
            .whenCompleteAsync((color, error) -> {
                fetching.remove(playerId);
                colors.put(playerId, error == null ? color : DEFAULT);
                onChange.run();
            }, core.mainThread());
    }
}
