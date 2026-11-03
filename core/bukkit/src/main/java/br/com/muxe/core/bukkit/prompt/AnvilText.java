package br.com.muxe.core.bukkit.prompt;

import java.util.Optional;
import org.bukkit.ChatColor;

public final class AnvilText {

    private AnvilText() {
    }

    public static Optional<String> typed(String resultName, String placeholder) {
        if (resultName == null) {
            return Optional.empty();
        }
        String text = ChatColor.stripColor(resultName).trim();
        if (text.isEmpty() || text.equals(ChatColor.stripColor(placeholder).trim())) {
            return Optional.empty();
        }
        return Optional.of(text);
    }
}
