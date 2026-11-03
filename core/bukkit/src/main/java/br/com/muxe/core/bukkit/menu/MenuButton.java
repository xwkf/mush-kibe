package br.com.muxe.core.bukkit.menu;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import java.util.List;
import java.util.Objects;

public record MenuButton(ItemBuilder icon, MenuClick click, List<ItemBuilder> frames, int period) {

    public MenuButton {
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(click, "click");
        frames = List.copyOf(frames);
    }

    public MenuButton(ItemBuilder icon, MenuClick click) {
        this(icon, click, List.of(), 0);
    }

    public static MenuButton of(ItemBuilder icon) {
        return new MenuButton(icon, MenuClick.NONE);
    }

    public static MenuButton of(ItemBuilder icon, MenuClick click) {
        return new MenuButton(icon, click);
    }

    public static MenuButton animated(List<ItemBuilder> frames, int period, MenuClick click) {
        if (frames.isEmpty()) {
            throw new IllegalArgumentException("An animated button needs at least one frame");
        }
        if (period < 1) {
            throw new IllegalArgumentException("An animated button needs a period of at least one tick, got " + period);
        }
        return frames.size() == 1 ? new MenuButton(frames.getFirst(), click) : new MenuButton(frames.getFirst(), click, frames, period);
    }

    public boolean animated() {
        return frames.size() > 1;
    }

    public ItemBuilder iconAt(int age) {
        return animated() ? frames.get(Math.floorMod(age / period, frames.size())) : icon;
    }

    public boolean changesAt(int age) {
        return animated() && age % period == 0;
    }
}
