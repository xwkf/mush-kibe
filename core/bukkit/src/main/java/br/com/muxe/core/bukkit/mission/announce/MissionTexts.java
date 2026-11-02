package br.com.muxe.core.bukkit.mission.announce;

import br.com.muxe.core.bukkit.credit.CreditFormat;
import br.com.muxe.core.bukkit.mission.config.MissionConfig;
import br.com.muxe.core.bukkit.text.Colors;
import br.com.muxe.core.common.mission.domain.Mission;
import br.com.muxe.core.common.mission.domain.MissionBoard;
import br.com.muxe.core.common.mission.domain.MissionCadence;
import br.com.muxe.core.common.mission.domain.MissionSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MissionTexts {

    private static final String PROGRESS = "{progress}";

    private MissionTexts() {
    }

    public static Completion completion(MissionConfig config, MissionBoard.Entry entry) {
        MissionConfig.Announce announce = config.announce();
        Mission mission = entry.mission();
        MissionConfig.Texts texts = config.texts(mission.id());
        String name = announce.name().replace("{name}", texts.name()).replace("{category}", config.label(mission.cadence()));
        String progress = announce.progress()
            .replace("{progress}", CreditFormat.format(entry.progress().progress()))
            .replace("{target}", CreditFormat.format(mission.target()));
        String description = texts.description().stream()
            .map(line -> Colors.strip(Colors.translate(line)).trim())
            .collect(Collectors.joining(" "))
            .replace(PROGRESS, progress);
        List<String> rewards = new ArrayList<>();
        if (mission.xp() > 0) {
            rewards.add(announce.xp().replace("{xp}", CreditFormat.format(mission.xp())));
        }
        if (mission.credits() > 0) {
            rewards.add(announce.credits().replace("{credits}", CreditFormat.format(mission.credits())));
        }
        return new Completion(announce.prefix(), name, List.of(name, "", announce.description().replace("{description}", description)), rewards);
    }

    public static List<String> summary(MissionConfig config, List<MissionSession.Change> changes) {
        if (changes.isEmpty()) {
            return List.of();
        }
        MissionConfig.Summary summary = config.summary();
        List<String> lines = new ArrayList<>();
        lines.add(summary.border());
        for (MissionCadence cadence : MissionCadence.values()) {
            List<MissionSession.Change> group = changes.stream().filter(change -> change.entry().mission().cadence() == cadence).toList();
            if (group.isEmpty()) {
                continue;
            }
            lines.add(summary.headers().get(cadence));
            lines.add("");
            for (MissionSession.Change change : group) {
                MissionBoard.Entry entry = change.entry();
                String line = summary.line()
                    .replace("{name}", config.texts(entry.mission().id()).name())
                    .replace("{progress}", CreditFormat.format(entry.progress().progress()))
                    .replace("{target}", CreditFormat.format(entry.mission().target()))
                    .replace("{added}", CreditFormat.format(change.added()));
                lines.add(entry.progress().completed() ? line + summary.completed() : line);
            }
            lines.add("");
        }
        lines.add(summary.border());
        return lines;
    }

    public record Completion(String prefix, String name, List<String> hover, List<String> rewards) {

        public Completion {
            hover = List.copyOf(hover);
            rewards = List.copyOf(rewards);
        }
    }
}
