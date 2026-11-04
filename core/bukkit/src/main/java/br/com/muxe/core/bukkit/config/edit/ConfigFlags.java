package br.com.muxe.core.bukkit.config.edit;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record ConfigFlags(Set<String> present, String rest) {

    public static final String NEW = "--novo";
    public static final String CENTER = "--centro";
    public static final String WORLD = "--mundo";

    public ConfigFlags {
        present = Set.copyOf(present);
    }

    public static ConfigFlags parse(Optional<String> raw, Set<String> known) {
        return parse(raw.orElse(""), known);
    }

    public static ConfigFlags parse(String raw, Set<String> known) {
        List<String> words = new ArrayList<>(List.of(raw.trim().split("\\s+")));
        if (words.size() == 1 && words.getFirst().isEmpty()) {
            words.clear();
        }
        Set<String> found = new LinkedHashSet<>();
        while (!words.isEmpty() && known.contains(words.getFirst())) {
            found.add(words.removeFirst());
        }
        while (!words.isEmpty() && known.contains(words.getLast())) {
            found.add(words.removeLast());
        }
        return new ConfigFlags(found, String.join(" ", words));
    }

    public boolean has(String flag) {
        return present.contains(flag);
    }
}
