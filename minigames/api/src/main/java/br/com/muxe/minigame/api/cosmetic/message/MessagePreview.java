package br.com.muxe.minigame.api.cosmetic.message;

import br.com.muxe.core.bukkit.text.Colors;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import org.bukkit.configuration.ConfigurationSection;

public record MessagePreview(String title, String victim, String teamColor, String team, Map<MessageKind, Long> counts) {

    public MessagePreview {
        counts = Map.copyOf(new EnumMap<>(counts));
    }

    public static MessagePreview from(ConfigurationSection section) {
        ConfigurationSection counted = Objects.requireNonNull(section.getConfigurationSection("counts"), "Missing configuration section " + section.getCurrentPath() + ".counts");
        Map<MessageKind, Long> counts = new EnumMap<>(MessageKind.class);
        for (String key : counted.getKeys(false)) {
            MessageKind kind = MessageKind.fromKey(key).orElseThrow(() -> new IllegalArgumentException("Unknown preview counter " + key));
            counts.put(kind, counted.getLong(key));
        }
        return new MessagePreview(
            Colors.translate(section.getString("title", "")),
            Colors.translate(section.getString("victim", "")),
            Colors.translate(section.getString("team-color", "")),
            section.getString("team", ""),
            counts
        );
    }

    public List<String> lines(MessageSet set, String viewerName) {
        List<String> lines = new ArrayList<>();
        lines.add(title);
        for (MessageKind kind : set.preview()) {
            lines.addAll(set.previewLines(kind, values(kind, viewerName)));
        }
        return lines;
    }

    public MessageValues values(MessageKind kind, String viewerName) {
        OptionalLong count = count(kind);
        return switch (kind) {
            case KILL, FINAL_KILL, VOID_KILL, VOID_FINAL_KILL -> MessageValues.kill(victim, viewerName, count);
            case DEATH, FINAL_DEATH, VOID_DEATH, VOID_FINAL_DEATH -> MessageValues.death(viewerName, count);
            case BED -> MessageValues.bed(viewerName, teamColor, team, count);
        };
    }

    private OptionalLong count(MessageKind kind) {
        Long own = counts.get(kind);
        if (own != null) {
            return OptionalLong.of(own);
        }
        return kind.fallback().map(this::count).orElse(OptionalLong.empty());
    }
}
