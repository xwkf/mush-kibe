package br.com.muxe.minigame.bedwars.hotbar.domain;

import java.util.Optional;
import java.util.Set;

public record HotbarEntry(Kind kind, String id) {

    public static final int MAX_ID_LENGTH = 64;
    private static final char SEPARATOR = ':';

    public HotbarEntry {
        if (kind == null || id == null || id.isBlank() || id.length() > MAX_ID_LENGTH || id.indexOf(SEPARATOR) >= 0 || id.indexOf(',') >= 0) {
            throw new IllegalArgumentException("Invalid hotbar entry: " + kind + " " + id);
        }
    }

    public static HotbarEntry category(String id) {
        return new HotbarEntry(Kind.CATEGORY, id);
    }

    public static HotbarEntry item(String id) {
        return new HotbarEntry(Kind.ITEM, id);
    }

    public static Optional<HotbarEntry> decode(String text) {
        int separator = text.indexOf(SEPARATOR);
        if (separator < 1) {
            return Optional.empty();
        }
        String id = text.substring(separator + 1);
        if (id.isBlank() || id.length() > MAX_ID_LENGTH || id.indexOf(SEPARATOR) >= 0) {
            return Optional.empty();
        }
        return Kind.ofCode(text.substring(0, separator)).map(kind -> new HotbarEntry(kind, id));
    }

    public String encoded() {
        return kind.code + SEPARATOR + id;
    }

    public boolean matches(String itemId, Set<String> categoryIds) {
        return kind == Kind.ITEM ? id.equals(itemId) : categoryIds.contains(id);
    }

    public enum Kind {
        CATEGORY("c"),
        ITEM("i");

        private final String code;

        Kind(String code) {
            this.code = code;
        }

        static Optional<Kind> ofCode(String code) {
            for (Kind kind : values()) {
                if (kind.code.equals(code)) {
                    return Optional.of(kind);
                }
            }
            return Optional.empty();
        }
    }
}
