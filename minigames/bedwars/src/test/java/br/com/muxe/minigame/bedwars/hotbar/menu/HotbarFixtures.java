package br.com.muxe.minigame.bedwars.hotbar.menu;

import br.com.muxe.core.bukkit.menu.ClickContext;
import br.com.muxe.core.bukkit.menu.Menu;
import br.com.muxe.core.bukkit.menu.MenuButton;
import br.com.muxe.minigame.bedwars.hotbar.HotbarStore;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarLayout;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

final class HotbarFixtures {

    static final UUID PLAYER = new UUID(0, 7);

    private HotbarFixtures() {
    }

    static Player player(List<String> messages) {
        return (Player) Proxy.newProxyInstance(HotbarFixtures.class.getClassLoader(), new Class<?>[] {Player.class}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getUniqueId" -> {
                    return PLAYER;
                }
                case "sendMessage" -> {
                    if (args[0] instanceof String text) {
                        messages.add(text);
                    }
                    return null;
                }
                case "hashCode" -> {
                    return PLAYER.hashCode();
                }
                case "equals" -> {
                    return proxy == args[0];
                }
                case "toString" -> {
                    return "FakePlayer";
                }
                default -> throw new UnsupportedOperationException(method.getName());
            }
        });
    }

    static void click(Menu menu, int slot, ClickType type, Player player) {
        MenuButton button = menu.button(slot).orElseThrow(() -> new AssertionError("No button at slot " + slot));
        button.click().click(new ClickContext(player, type, slot, null));
    }

    static final class MemoryStore implements HotbarStore {

        private final Map<UUID, HotbarLayout> layouts = new HashMap<>();
        private final List<HotbarLayout> saves = new ArrayList<>();

        @Override
        public HotbarLayout of(UUID playerId) {
            return layouts.getOrDefault(playerId, HotbarLayout.empty());
        }

        @Override
        public void change(UUID playerId, HotbarLayout layout) {
            layouts.put(playerId, layout);
            saves.add(layout);
        }

        List<HotbarLayout> saves() {
            return saves;
        }
    }
}
