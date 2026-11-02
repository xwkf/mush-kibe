package br.com.muxe.minigame.bedwars.lobby.top;

import br.com.muxe.minigame.bedwars.stats.domain.PlayerStats;
import java.util.List;
import java.util.function.Function;

public final class TopSelectorView {

    private TopSelectorView() {
    }

    public static List<String> modes(TopSelectorSettings settings, List<String> order, String current, Function<String, String> names) {
        int index = Math.max(0, order.indexOf(current));
        int visible = Math.min(settings.visibleModes(), order.size());
        int start = Math.max(0, Math.min(index - visible + 1, order.size() - visible));
        start = index < visible ? 0 : start;
        return order.subList(start, start + visible).stream()
            .map(mode -> (mode.equals(current) ? settings.selected() : settings.other()).replace("{mode}", name(settings, mode, names)))
            .toList();
    }

    private static String name(TopSelectorSettings settings, String mode, Function<String, String> names) {
        return PlayerStats.GENERAL.equals(mode) ? settings.generalName() : names.apply(mode);
    }
}
