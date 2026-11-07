package br.com.muxe.minigame.api.view;

import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;

public record TitleSweep(String word, String accent, String highlight, String base) {

    public static final int SWEEP_STEP = 2;
    public static final int FIRST_BLINK = 24;
    public static final int BLINK = 7;
    public static final int LAST_FRAME = 115;
    private static final String BOLD = "&l";

    public static TitleSweep from(ConfigurationSection section, String defaultWord, String defaultAccent) {
        return new TitleSweep(
            section == null ? defaultWord : section.getString("word", defaultWord),
            section == null ? defaultAccent : section.getString("accent", defaultAccent),
            section == null ? "&e" : section.getString("highlight", "&e"),
            section == null ? "&f" : section.getString("base", "&f")
        );
    }

    public Optional<String> frame(int tick) {
        if (tick < 0 || tick > LAST_FRAME) {
            return Optional.empty();
        }
        int letters = word.length();
        int sweepEnd = SWEEP_STEP * (letters + 1);
        if (tick < sweepEnd) {
            int index = tick / SWEEP_STEP;
            if (index == 0) {
                return Optional.of(base + BOLD + word);
            }
            StringBuilder text = new StringBuilder();
            if (index > 1) {
                text.append(accent).append(BOLD).append(word, 0, index - 1);
            }
            text.append(highlight).append(BOLD).append(word.charAt(index - 1));
            if (index < letters) {
                text.append(base).append(BOLD).append(word.substring(index));
            }
            return Optional.of(text.toString());
        }
        if (tick < FIRST_BLINK) {
            return Optional.of(accent + BOLD + word);
        }
        boolean lit = (tick - FIRST_BLINK) / BLINK % 2 == 1;
        return Optional.of((lit ? accent : base) + BOLD + word);
    }
}
