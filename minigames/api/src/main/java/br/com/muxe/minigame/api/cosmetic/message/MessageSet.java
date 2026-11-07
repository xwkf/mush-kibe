package br.com.muxe.minigame.api.cosmetic.message;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record MessageSet(String id, String lineColor, Map<MessageKind, String> templates, List<MessageKind> preview) {

    public static final String BREAK = "{br}";
    public static final String COUNT = "{count}";

    public MessageSet {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(lineColor, "lineColor");
        templates = Map.copyOf(new EnumMap<>(templates));
        preview = List.copyOf(preview);
    }

    public Optional<String> template(MessageKind kind) {
        String own = templates.get(kind);
        if (own != null) {
            return Optional.of(own);
        }
        return kind.fallback().flatMap(this::template);
    }

    public boolean defines(MessageKind kind) {
        return template(kind).isPresent();
    }

    public boolean needsCount(MessageKind kind) {
        return template(kind).filter(text -> text.contains(COUNT)).isPresent();
    }

    public String render(MessageKind kind, MessageValues values) {
        return lineColor + filled(kind, values).replace(BREAK, "");
    }

    public List<String> previewLines(MessageKind kind, MessageValues values) {
        String filled = filled(kind, values);
        int split = filled.indexOf(BREAK);
        if (split < 0) {
            return List.of(lineColor + filled);
        }
        List<String> lines = new ArrayList<>();
        lines.add(lineColor + filled.substring(0, split).stripTrailing());
        lines.add(lineColor + filled.substring(split + BREAK.length()).stripLeading());
        return lines;
    }

    private String filled(MessageKind kind, MessageValues values) {
        String template = template(kind).orElseThrow(() -> new IllegalArgumentException("The set " + id + " has no " + kind.key() + " message"));
        if (template.contains(COUNT) && values.count().isEmpty()) {
            throw new IllegalArgumentException("The " + kind.key() + " message of " + id + " needs a counter");
        }
        return template
            .replace("{victim}", values.victim())
            .replace("{killer}", values.killer())
            .replace("{player}", values.player())
            .replace("{team-color}", values.teamColor())
            .replace("{team}", values.team())
            .replace(COUNT, values.count().isPresent() ? Ordinal.format(values.count().getAsLong()) : "");
    }
}
