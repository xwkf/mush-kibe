package br.com.muxe.core.bukkit.scoreboard;

import java.util.List;
import java.util.Objects;

public record SidebarContent(String title, List<String> lines) {

    public SidebarContent {
        Objects.requireNonNull(title, "title");
        lines = List.copyOf(lines);
    }
}
