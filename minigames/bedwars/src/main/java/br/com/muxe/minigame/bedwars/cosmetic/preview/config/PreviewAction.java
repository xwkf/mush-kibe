package br.com.muxe.minigame.bedwars.cosmetic.preview.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum PreviewAction {

    SPAWN("spawn"),
    BED("cama"),
    DISTANCE("distancia"),
    TIMEOUT("tempo");

    private final String word;

    PreviewAction(String word) {
        this.word = word;
    }

    public static Optional<PreviewAction> parse(String word) {
        String typed = word.toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(action -> action.word.equals(typed)).findFirst();
    }

    public static List<String> words() {
        return Arrays.stream(values()).map(PreviewAction::word).toList();
    }

    public String word() {
        return word;
    }
}
