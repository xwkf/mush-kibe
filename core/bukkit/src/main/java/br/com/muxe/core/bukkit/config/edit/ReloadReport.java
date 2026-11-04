package br.com.muxe.core.bukkit.config.edit;

import java.util.List;
import java.util.Map;

public record ReloadReport(List<String> applied, Map<String, String> failed) {

    public static final ReloadReport NONE = new ReloadReport(List.of(), Map.of());

    public ReloadReport {
        applied = List.copyOf(applied);
        failed = Map.copyOf(failed);
    }

    public boolean covered() {
        return !applied.isEmpty() || !failed.isEmpty();
    }

    public boolean live() {
        return !applied.isEmpty() && failed.isEmpty();
    }
}
