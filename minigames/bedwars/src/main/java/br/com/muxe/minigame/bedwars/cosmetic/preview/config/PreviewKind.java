package br.com.muxe.minigame.bedwars.cosmetic.preview.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum PreviewKind {

    BED("cama", "preview", true, false),
    VICTORY("vitoria", "victory-preview", false, false),
    FINAL_KILL("kill-final", "final-kill-preview", false, true),
    AURA("aura", "aura-preview", false, false);

    private final String word;
    private final String section;
    private final boolean hasBed;
    private final boolean hasTarget;

    PreviewKind(String word, String section, boolean hasBed, boolean hasTarget) {
        this.word = word;
        this.section = section;
        this.hasBed = hasBed;
        this.hasTarget = hasTarget;
    }

    public static Optional<PreviewKind> parse(String word) {
        String typed = word.toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(kind -> kind.word.equals(typed)).findFirst();
    }

    public static List<String> words() {
        return Arrays.stream(values()).map(PreviewKind::word).toList();
    }

    public String word() {
        return word;
    }

    public String section() {
        return section;
    }

    public boolean hasBed() {
        return hasBed;
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    public String path(String key) {
        return section + "." + key;
    }
}
