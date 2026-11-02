package br.com.muxe.minigame.bedwars.lobby.top;

import br.com.muxe.core.bukkit.credit.CreditFormat;
import br.com.muxe.minigame.bedwars.game.Levels;
import br.com.muxe.minigame.bedwars.lobby.format.LevelBadges;
import br.com.muxe.minigame.bedwars.stats.domain.PlayerStats;
import br.com.muxe.minigame.bedwars.stats.domain.StatsPeriod;
import br.com.muxe.minigame.bedwars.stats.domain.TopCategory;
import br.com.muxe.minigame.bedwars.stats.domain.TopEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public final class TopView {

    public static final int UPPER_LINES = 2 + TopState.PAGE_SIZE;

    private TopView() {
    }

    public static StatsPeriod period(TopBoardSettings board, TopState state) {
        return board.periods().get(state.period() % board.periods().size());
    }

    public static List<String> lines(TopBoardSettings board, TopTexts texts, TopState state, List<TopEntry> entries, Function<UUID, String> colors,
                                     LevelBadges badges, Function<String, String> modeTitle) {
        List<String> lines = new ArrayList<>();
        lines.add(texts.header().replace("{page}", String.valueOf(state.page() + 1)).replace("{pages}", String.valueOf(TopState.PAGES)));
        String mode = PlayerStats.GENERAL.equals(state.mode()) ? texts.general() : modeTitle.apply(state.mode());
        lines.add(board.title().replace("{mode}", mode));
        for (int index = 0; index < TopState.PAGE_SIZE; index++) {
            int position = state.page() * TopState.PAGE_SIZE + index + 1;
            if (position <= entries.size()) {
                TopEntry entry = entries.get(position - 1);
                String value = board.category() == TopCategory.LEVEL
                    ? badges.of(Levels.progress(Math.max(0, entry.totalXp())).level())
                    : CreditFormat.format(entry.value());
                lines.add(texts.entry().replace("{position}", String.valueOf(position)).replace("{name}", colors.apply(entry.playerId()) + entry.name()).replace("{value}", value));
            } else {
                lines.add(texts.empty().replace("{position}", String.valueOf(position)));
            }
        }
        lines.add(texts.click());
        StatsPeriod selected = period(board, state);
        List<String> periods = new ArrayList<>();
        for (StatsPeriod period : board.periods()) {
            String name = texts.periodNames().getOrDefault(period, period.id());
            periods.add((period == selected ? texts.periodSelected() : texts.periodOther()).replace("{period}", name));
        }
        lines.add(String.join(" ", periods));
        return lines;
    }
}
